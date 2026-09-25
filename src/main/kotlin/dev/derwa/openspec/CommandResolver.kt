package dev.derwa.openspec

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

enum class Action(val label: String) {
    EXPLORE("explore"),
    PROPOSE("propose"),
    APPLY("apply"),
    VERIFY("verify"),
    ARCHIVE("archive"),
    BACKLOG_REVIEW("backlog review"),
    PROMOTE("promote"),
}

/** How OpenSpec's workflow commands are installed for Claude Code in a project. */
enum class Delivery { SKILLS, OPSX }

data class ProjectSetup(val delivery: Delivery?, val hasBacklog: Boolean) {
    companion object {
        fun detect(projectRoot: Path): ProjectSetup {
            val claude = projectRoot.resolve(".claude")
            val skills = claude.resolve("skills")
            val hasSkills = skills.isDirectory() &&
                skills.listDirectoryEntries("openspec-*").any { it.isDirectory() }
            val delivery = when {
                hasSkills -> Delivery.SKILLS
                claude.resolve("commands/opsx").isDirectory() -> Delivery.OPSX
                else -> null
            }
            return ProjectSetup(delivery, Files.isRegularFile(claude.resolve("commands/backlog.md")))
        }
    }
}

/** The slash command each action sends, following the project's own setup. */
class CommandResolver(private val setup: ProjectSetup) {
    fun command(action: Action, argument: String?): String? {
        val base = when (action) {
            Action.BACKLOG_REVIEW -> if (setup.hasBacklog) "/backlog review" else null
            Action.PROMOTE -> if (setup.hasBacklog) "/backlog promote" else null
            else -> workflowCommand(action)
        } ?: return null
        val text = argument?.trim().orEmpty()
        return if (text.isEmpty()) base else "$base $text"
    }

    private fun workflowCommand(action: Action): String? = when (setup.delivery) {
        Delivery.SKILLS -> when (action) {
            Action.EXPLORE -> "/openspec-explore"
            Action.PROPOSE -> "/openspec-propose"
            else -> "/openspec-${action.name.lowercase()}-change"
        }
        Delivery.OPSX -> "/opsx:${action.name.lowercase()}"
        null -> null
    }
}
