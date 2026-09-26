package dev.derwa.openspec

const val NOT_SET_UP = "OpenSpec isn't set up for Claude Code in this project"
const val SELECT_ONE_CHANGE = "Select one change"
const val SELECT_FOLLOW_UPS = "Select one or more follow-ups"
const val ONLY_FOLLOW_UPS = "Select only follow-ups to promote"
const val NO_ID = "A follow-up without an ID can't be promoted"

/** An action as offered for the current selection; [reason] says why it's disabled. */
data class OfferedAction(val action: Action, val target: String?, val enabled: Boolean, val reason: String? = null)

/** The change actions, the likely next step first, and Promote, or null where there's no backlog. */
data class SelectionActions(val changeActions: List<OfferedAction>, val promote: OfferedAction?)

private val DEFAULT_CHANGE_ACTIONS = listOf(Action.APPLY, Action.VERIFY, Action.ARCHIVE, Action.EXPLORE)

/**
 * What each action does for [selection], given in list order. [includesOther] is set when a group
 * or message is selected as well, which disables the selection's actions.
 */
fun selectionActions(setup: ProjectSetup, selection: List<PanelItem>, includesOther: Boolean = false): SelectionActions {
    val changes = selection.filterIsInstance<ChangeRow>()
    val followUps = selection.filterIsInstance<FollowUpRow>()

    val changeActions = if (changes.size == 1 && followUps.isEmpty() && !includesOther) {
        changes.single().actions.map { offered(it) }
    } else {
        DEFAULT_CHANGE_ACTIONS.map { OfferedAction(it, null, enabled = false, reason = SELECT_ONE_CHANGE) }
    }

    val promote = if (!setup.hasBacklog) {
        null
    } else {
        val reason = when {
            followUps.isEmpty() -> SELECT_FOLLOW_UPS
            changes.isNotEmpty() || includesOther -> ONLY_FOLLOW_UPS
            followUps.any { it.promote == null } -> NO_ID
            else -> null
        }
        if (reason != null) {
            OfferedAction(Action.PROMOTE, null, enabled = false, reason = reason)
        } else {
            offered(
                ActionButton(
                    Action.PROMOTE,
                    followUps.joinToString(" ") { it.promote!!.target.orEmpty() },
                    enabled = followUps.all { it.promote!!.enabled },
                ),
            )
        }
    }
    return SelectionActions(changeActions, promote)
}

private fun offered(button: ActionButton) =
    OfferedAction(button.action, button.target, button.enabled, reason = if (button.enabled) null else NOT_SET_UP)
