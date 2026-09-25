package dev.derwa.openspec

data class ActionButton(val action: Action, val target: String?, val enabled: Boolean)

data class ChangeRow(val name: String, val progress: String, val actions: List<ActionButton>)

data class FollowUpRow(
    val id: String,
    val title: String,
    val detail: String,
    val promote: ActionButton?,
    val unreadable: Boolean = false,
)

sealed interface ChangesSection {
    data class Rows(val rows: List<ChangeRow>) : ChangesSection
    data class Message(val text: String) : ChangesSection
}

/** What the panel shows: a null [followUps] means the project has no follow-ups section at all. */
data class PanelModel(
    val projectActions: List<ActionButton>,
    val changes: ChangesSection,
    val followUps: List<FollowUpRow>?,
)

private const val TAB_TARGET_LENGTH = 32

fun panelModel(setup: ProjectSetup, changes: ChangesResult, followUps: List<FollowUp>?): PanelModel {
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
                            actions = listOf(Action.APPLY, Action.VERIFY, Action.ARCHIVE).map { button(it, change.name) },
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
                unreadable = followUp.unreadable,
            )
        }
    }

    return PanelModel(projectActions, changesSection, followUpRows)
}

/** A terminal tab's name: the action, and its target's first line shortened. */
fun tabName(action: Action, target: String?): String {
    val line = target?.lineSequence()?.firstOrNull()?.trim().orEmpty()
    if (line.isEmpty()) return action.label
    val shortened = if (line.length > TAB_TARGET_LENGTH) line.take(TAB_TARGET_LENGTH).trimEnd() + "…" else line
    return "${action.label}: $shortened"
}
