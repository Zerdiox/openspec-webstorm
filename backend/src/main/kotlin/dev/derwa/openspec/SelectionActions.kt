package dev.derwa.openspec

internal const val NOT_SET_UP = "OpenSpec isn't set up for Claude Code in this project"
internal const val SELECT_ONE_CHANGE = "Select one change"
internal const val SELECT_FOLLOW_UPS = "Check or select one or more follow-ups"
internal const val ONLY_FOLLOW_UPS = "Select only follow-ups to promote"
internal const val NO_ID = "A follow-up without an ID can't be promoted"
internal const val UNREADABLE = "An unreadable follow-up can't be promoted"

/** An action as offered for the current selection; [reason] says why it's disabled. */
internal data class OfferedAction(val action: Action, val target: String?, val enabled: Boolean, val reason: String? = null)

/** Promote as offered: the follow-ups it targets, in list order, and whether to ask before promoting them. */
internal data class PromoteOffer(
    val followUps: List<FollowUpRow>,
    val enabled: Boolean,
    val reason: String? = null,
    val confirm: Boolean = false,
) {
    val target: String? get() = followUps.takeIf { it.isNotEmpty() }?.joinToString(" ") { it.id }

    val label: String
        get() = when (followUps.size) {
            0 -> "Promote"
            1 -> "Promote ${followUps.single().id}"
            else -> "Promote ${followUps.size}"
        }
}

/** Where an action was chosen: the toolbar acts on the checked follow-ups, the context menu on the rows clicked. */
internal enum class Place { TOOLBAR, CONTEXT_MENU }

/** The change actions, the likely next step first, and Promote, or null where there's no backlog. */
internal data class SelectionActions(val changeActions: List<OfferedAction>, val promote: PromoteOffer?)

private val DEFAULT_CHANGE_ACTIONS = listOf(Action.APPLY, Action.VERIFY, Action.ARCHIVE, Action.EXPLORE)

/**
 * What each action does for [selection], given in list order. [includesOther] is set when a group
 * or message is selected as well, which disables the selection's actions. [checked] are the checked
 * follow-ups shown, in list order, which Promote targets from the toolbar instead of the selection.
 */
internal fun selectionActions(
    setup: ProjectSetup,
    selection: List<PanelItem>,
    includesOther: Boolean = false,
    checked: List<FollowUpRow> = emptyList(),
    place: Place = Place.TOOLBAR,
): SelectionActions {
    val changes = selection.filterIsInstance<ChangeRow>()
    val followUps = selection.filterIsInstance<FollowUpRow>()

    val changeActions = if (changes.size == 1 && followUps.isEmpty() && !includesOther) {
        changes.single().actions.map { offered(it) }
    } else {
        DEFAULT_CHANGE_ACTIONS.map { OfferedAction(it, null, enabled = false, reason = SELECT_ONE_CHANGE) }
    }

    val promote = when {
        !setup.hasBacklog -> null
        place == Place.TOOLBAR && checked.isNotEmpty() -> promoteOffer(checked, place)
        else -> {
            val reason = when {
                followUps.isEmpty() -> SELECT_FOLLOW_UPS
                changes.isNotEmpty() || includesOther -> ONLY_FOLLOW_UPS
                followUps.any { it.unreadable } -> UNREADABLE
                followUps.any { it.promote == null } -> NO_ID
                else -> null
            }
            if (reason != null) PromoteOffer(emptyList(), enabled = false, reason = reason) else promoteOffer(followUps, place)
        }
    }
    return SelectionActions(changeActions, promote)
}

private fun promoteOffer(followUps: List<FollowUpRow>, place: Place): PromoteOffer {
    val enabled = followUps.all { it.promote?.enabled == true }
    return PromoteOffer(
        followUps,
        enabled,
        reason = if (enabled) null else NOT_SET_UP,
        confirm = enabled && place == Place.TOOLBAR && followUps.size > 1,
    )
}

private fun offered(button: ActionButton) =
    OfferedAction(button.action, button.target, button.enabled, reason = if (button.enabled) null else NOT_SET_UP)
