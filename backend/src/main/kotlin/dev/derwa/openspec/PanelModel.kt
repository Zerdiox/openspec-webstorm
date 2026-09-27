package dev.derwa.openspec

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

internal data class ActionButton(val action: Action, val target: String?, val enabled: Boolean)

/** A change or follow-up the panel lists; its [key] stays the same across refreshes. */
internal sealed interface PanelItem {
    val key: String
}

internal data class ChangeRow(
    val name: String,
    val progress: String,
    val actions: List<ActionButton>,
    val folder: Path,
) : PanelItem {
    override val key get() = "change:$name"
}

internal data class FollowUpRow(
    val id: String,
    val title: String,
    val detail: String,
    val promote: ActionButton?,
    val file: Path,
    val type: String? = null,
    val capability: String? = null,
    val unreadable: Boolean = false,
) : PanelItem {
    // The file name, since an unreadable follow-up may have no ID.
    override val key get() = "followup:${file.fileName}"
}

internal sealed interface ChangesSection {
    data class Rows(val rows: List<ChangeRow>) : ChangesSection
    data class Message(val text: String) : ChangesSection
}

/** What the panel shows: a null [followUps] means the project has no follow-ups section at all. */
internal data class PanelModel(
    val projectActions: List<ActionButton>,
    val changes: ChangesSection,
    val followUps: List<FollowUpRow>?,
)

private const val TAB_TARGET_LENGTH = 32

internal fun panelModel(setup: ProjectSetup, projectRoot: Path, changes: ChangesResult, followUps: List<FollowUp>?): PanelModel {
    val resolver = CommandResolver(setup)
    fun button(action: Action, target: String?) =
        ActionButton(action, target, enabled = resolver.command(action, target) != null)

    val projectActions = buildList {
        add(button(Action.EXPLORE, null))
        add(button(Action.PROPOSE, null))
        if (setup.hasBacklog) add(button(Action.BACKLOG_REVIEW, null))
    }

    val changesSection = when (changes) {
        is ChangesResult.Unreadable -> ChangesSection.Message("Couldn't read changes: ${changes.reason}")
        is ChangesResult.Loaded ->
            if (changes.changes.isEmpty()) {
                ChangesSection.Message("No changes in flight.")
            } else {
                ChangesSection.Rows(
                    changes.changes.map { change ->
                        ChangeRow(
                            name = change.name,
                            progress = "${change.completedTasks}/${change.totalTasks}",
                            actions = changeActions(change).map { button(it, change.name) },
                            folder = projectRoot.resolve("openspec/changes").resolve(change.name),
                        )
                    },
                )
            }
    }

    val followUpRows = if (!setup.hasBacklog) {
        null
    } else {
        followUps.orEmpty().map { followUp ->
            FollowUpRow(
                id = followUp.id ?: followUp.file,
                title = if (followUp.unreadable) followUp.file else followUp.title.orEmpty(),
                detail = if (followUp.unreadable) {
                    "unreadable"
                } else {
                    listOfNotNull(followUp.type, followUp.capability).joinToString(" · ")
                },
                promote = followUp.id?.let { button(Action.PROMOTE, it) },
                file = projectRoot.resolve(FOLLOW_UPS_FOLDER).resolve(followUp.file),
                type = followUp.type,
                capability = followUp.capability,
                unreadable = followUp.unreadable,
            )
        }
    }

    return PanelModel(projectActions, changesSection, followUpRows)
}

/** A change's actions, its likely next step first. Archive is never first: it's the one that's hard to undo. */
private fun changeActions(change: Change): List<Action> = when {
    change.totalTasks == 0 -> listOf(Action.EXPLORE, Action.APPLY, Action.VERIFY, Action.ARCHIVE)
    change.completedTasks >= change.totalTasks -> listOf(Action.VERIFY, Action.ARCHIVE, Action.APPLY, Action.EXPLORE)
    else -> listOf(Action.APPLY, Action.VERIFY, Action.ARCHIVE, Action.EXPLORE)
}

/** A terminal tab's name: the action, and its target's first line shortened. */
internal fun tabName(action: Action, target: String?): String {
    val line = target?.lineSequence()?.firstOrNull()?.trim().orEmpty()
    if (line.isEmpty()) return action.label
    val shortened = if (line.length > TAB_TARGET_LENGTH) line.take(TAB_TARGET_LENGTH).trimEnd() + "…" else line
    return "${action.label}: $shortened"
}

/** What opening a change shows: its proposal, or its folder until it has one. */
internal fun changeOpenTarget(folder: Path): Path =
    folder.resolve("proposal.md").takeIf { Files.isRegularFile(it) } ?: folder

/** A spec a change adds or modifies: its capability, with any domain (`identity/user-auth`), and its file. */
internal data class DeltaSpec(val capability: String, val file: Path)

/** The files a change has of its proposal, design, tasks and specs; null or empty where it has none yet. */
internal data class ChangeArtifacts(val proposal: Path?, val design: Path?, val tasks: Path?, val specs: List<DeltaSpec>)

internal fun changeArtifacts(folder: Path): ChangeArtifacts {
    fun file(name: String) = folder.resolve(name).takeIf { Files.isRegularFile(it) }
    val specsFolder = folder.resolve("specs")
    val specs = mutableListOf<DeltaSpec>()
    if (Files.isDirectory(specsFolder)) {
        Files.walkFileTree(specsFolder, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (file.fileName.toString() == "spec.md" && attrs.isRegularFile && file.parent != specsFolder) {
                    specs += DeltaSpec(specsFolder.relativize(file.parent).joinToString("/"), file)
                }
                return FileVisitResult.CONTINUE
            }

            // A folder that can't be read is skipped rather than failing the whole menu.
            override fun visitFileFailed(file: Path, exc: IOException) = FileVisitResult.CONTINUE
        })
    }
    return ChangeArtifacts(file("proposal.md"), file("design.md"), file("tasks.md"), specs.sortedBy { it.capability })
}
