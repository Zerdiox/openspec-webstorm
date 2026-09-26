package dev.derwa.openspec

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.DumbAwareToggleAction

private fun AnActionEvent.panel() = getData(OpenSpecPanel.PANEL)

private fun label(action: Action) = action.label.replaceFirstChar { it.uppercase() }

private fun AnAction.showingText() = apply { templatePresentation.putClientProperty(ActionUtil.SHOW_TEXT_IN_TOOLBAR, true) }

/** Explore…, Propose… and Backlog review, then the selection's actions, the filter and Group By. */
fun toolbarActions(): ActionGroup = DefaultActionGroup(
    ProjectAction(Action.EXPLORE).showingText(),
    ProjectAction(Action.PROPOSE).showingText(),
    ProjectAction(Action.BACKLOG_REVIEW).showingText(),
    Separator.create(),
    ChangeAction(0).showingText(),
    MoreChangeActions(),
    PromoteAction().showingText(),
    Separator.create(),
    FilterFollowUps(),
    GroupFollowUps(),
)

/** Jump to Source, then the selection's actions. */
fun contextMenuActions(): ActionGroup = DefaultActionGroup(
    ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE),
    Separator.create(),
    ChangeAction(0),
    ChangeAction(1),
    ChangeAction(2),
    ChangeAction(3),
    PromoteAction(),
)

private abstract class PanelAction : DumbAwareAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.EDT
}

private class ProjectAction(private val action: Action) : PanelAction() {
    init {
        templatePresentation.text = when (action) {
            Action.EXPLORE -> "Explore…"
            Action.PROPOSE -> "Propose…"
            else -> label(action)
        }
    }

    override fun update(e: AnActionEvent) {
        val panel = e.panel()
        val button = panel?.projectAction(action)
        // Explore and Propose are always offered, disabled until the first load; Backlog review only with a backlog.
        e.presentation.isVisible = button != null || (action != Action.BACKLOG_REVIEW && panel != null)
        e.presentation.isEnabled = button?.enabled == true
        e.presentation.description = if (button?.enabled == false) NOT_SET_UP else null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val panel = e.panel() ?: return
        if (action == Action.EXPLORE || action == Action.PROPOSE) {
            val dialog = DescriptionDialog(e.project ?: return, action)
            if (dialog.showAndGet()) panel.launch(action, dialog.description)
        } else {
            panel.launch(action, null)
        }
    }
}

/** The selection's change action at [index]: 0 is the likely next step. */
private class ChangeAction(private val index: Int) : PanelAction() {
    private fun offered(e: AnActionEvent): OfferedAction? {
        val panel = e.panel() ?: return null
        return panel.selectionActions().changeActions.getOrNull(index)
    }

    override fun update(e: AnActionEvent) {
        val offered = offered(e)
        val relevant = e.panel()?.selectedItems()?.any { it is ChangeRow } == true
        e.presentation.isVisible = offered != null && (!e.isFromContextMenu || relevant)
        if (offered == null) return
        e.presentation.text = label(offered.action)
        e.presentation.isEnabled = offered.enabled
        e.presentation.description = offered.reason
    }

    override fun actionPerformed(e: AnActionEvent) {
        val offered = offered(e)?.takeIf { it.enabled } ?: return
        e.panel()?.launch(offered.action, offered.target)
    }
}

/** The selected change's other actions, in a dropdown next to its next step. */
private class MoreChangeActions : DefaultActionGroup(ChangeAction(1), ChangeAction(2), ChangeAction(3)) {
    init {
        isPopup = true
        templatePresentation.text = "More Actions"
        templatePresentation.icon = AllIcons.Actions.More
        templatePresentation.putClientProperty(ActionUtil.HIDE_DROPDOWN_ICON, true)
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun update(e: AnActionEvent) {
        val panel = e.panel()
        val first = panel?.selectionActions()?.changeActions?.first()
        // Enabled only with one change selected, where the dropdown holds its other actions.
        e.presentation.isEnabled = first?.target != null
        e.presentation.description = if (first?.target == null) SELECT_ONE_CHANGE else null
    }
}

private class PromoteAction : PanelAction() {
    init {
        templatePresentation.text = label(Action.PROMOTE)
    }

    private fun offered(e: AnActionEvent): OfferedAction? {
        val panel = e.panel() ?: return null
        return panel.selectionActions().promote
    }

    override fun update(e: AnActionEvent) {
        val offered = offered(e)
        val relevant = e.panel()?.selectedItems()?.any { it is FollowUpRow } == true
        e.presentation.isVisible = offered != null && (!e.isFromContextMenu || relevant)
        e.presentation.isEnabled = offered?.enabled == true
        e.presentation.description = offered?.reason ?: "Explore the selected follow-ups as a candidate change"
    }

    override fun actionPerformed(e: AnActionEvent) {
        val offered = offered(e)?.takeIf { it.enabled } ?: return
        e.panel()?.launch(offered.action, offered.target)
    }
}

/** Filters the follow-ups by type and capability; the choices follow the open follow-ups. */
private class FilterFollowUps : ActionGroup("Filter Follow-ups", null, AllIcons.General.Filter) {
    init {
        isPopup = true
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = e.panel()?.setup?.hasBacklog == true
    }

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val panel = e?.panel() ?: return emptyArray()
        val choices = filterChoices(panel.followUpRows())
        return buildList {
            add(Separator.create("Type"))
            choices.types.forEach { add(FilterChoice(it, byType = true)) }
            add(Separator.create("Capability"))
            choices.capabilities.forEach { add(FilterChoice(it, byType = false)) }
        }.toTypedArray()
    }
}

private class FilterChoice(private val value: String, private val byType: Boolean) : DumbAwareToggleAction(value) {
    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    private fun hidden(view: FollowUpView) = if (byType) view.hiddenTypes else view.hiddenCapabilities

    override fun isSelected(e: AnActionEvent): Boolean {
        val view = e.panel()?.followUpView ?: return true
        return value !in hidden(view)
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val panel = e.panel() ?: return
        val view = panel.followUpView
        val hidden = if (state) hidden(view) - value else hidden(view) + value
        panel.updateFollowUpView(if (byType) view.copy(hiddenTypes = hidden) else view.copy(hiddenCapabilities = hidden))
    }
}

private class GroupFollowUps : DefaultActionGroup(
    GroupingChoice(Grouping.NONE, "None"),
    GroupingChoice(Grouping.TYPE, "Type"),
    GroupingChoice(Grouping.CAPABILITY, "Capability"),
) {
    init {
        isPopup = true
        templatePresentation.text = "Group Follow-ups By"
        templatePresentation.icon = AllIcons.Actions.GroupBy
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = e.panel()?.setup?.hasBacklog == true
    }
}

private class GroupingChoice(private val grouping: Grouping, text: String) : DumbAwareToggleAction(text) {
    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun isSelected(e: AnActionEvent) = e.panel()?.followUpView?.grouping == grouping

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val panel = e.panel() ?: return
        if (state) panel.updateFollowUpView(panel.followUpView.copy(grouping = grouping))
    }
}
