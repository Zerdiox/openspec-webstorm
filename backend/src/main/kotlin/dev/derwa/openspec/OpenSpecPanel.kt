package dev.derwa.openspec

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.actionSystem.DataSink
import com.intellij.openapi.actionSystem.toolbarLayout.ToolbarLayoutStrategy
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.pom.Navigatable
import com.intellij.ui.CheckboxTreeBase.CheckboxTreeCellRendererBase
import com.intellij.ui.PopupHandler
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.Alarm
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.ToolTipManager
import javax.swing.tree.DefaultMutableTreeNode

/** The OpenSpec tool window: the project's changes and follow-ups as a tree, with actions on the selection. */
internal class OpenSpecPanel(private val project: Project, private val root: Path) : Disposable {
    private val settings = PropertiesComponent.getInstance(project)
    private val keyedTree = KeyedTree(settings, COLLAPSED_SETTING, Renderer(), ::searchText)

    private val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)
    private val generation = AtomicInteger()
    @Volatile
    private var disposed = false

    var setup = ProjectSetup(null, hasBacklog = false)
        private set
    private var model: PanelModel? = null

    var followUpView = loadView()
        private set

    val component: JComponent = object : SimpleToolWindowPanel(true, true) {
        override fun uiDataSnapshot(sink: DataSink) {
            super.uiDataSnapshot(sink)
            sink[PANEL] = this@OpenSpecPanel
            val selection = selectedItems()
            sink.lazy(CommonDataKeys.NAVIGATABLE_ARRAY) {
                selection.map { PathNavigatable(project, openTarget(it)) }.toTypedArray<Navigatable>().takeIf { it.isNotEmpty() }
            }
        }
    }.apply {
        val actions = ActionManager.getInstance()
        val toolbar = actions.createActionToolbar(TOOLBAR_PLACE, toolbarActions(), true)
        toolbar.targetComponent = keyedTree.tree
        // Actions that don't fit a narrow panel go behind the toolbar's overflow chevron.
        toolbar.layoutStrategy = ToolbarLayoutStrategy.AUTOLAYOUT_STRATEGY
        setToolbar(toolbar.component)
        setContent(ScrollPaneFactory.createScrollPane(keyedTree.tree, true))
    }

    init {
        ToolTipManager.sharedInstance().registerComponent(keyedTree.tree)
        PopupHandler.installFollowingSelectionTreePopup(keyedTree.tree, contextMenuActions(), POPUP_PLACE)

        val connection = project.messageBus.connect(this)
        connection.subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                if (events.any { concernsOpenSpec(it.path) }) scheduleRefresh(DEBOUNCE_MS)
            }
        })
        connection.subscribe(ToolWindowManagerListener.TOPIC, object : ToolWindowManagerListener {
            override fun toolWindowShown(toolWindow: ToolWindow) {
                if (toolWindow.id == TOOL_WINDOW_ID) {
                    loadIntoVfs()
                    scheduleRefresh(0)
                }
            }
        })
        loadIntoVfs()
        scheduleRefresh(0)
    }

    /** The selected changes and follow-ups, in the order they're listed. */
    fun selectedItems(): List<PanelItem> = keyedTree.selection.mapNotNull { it.value as? PanelItem }

    /** The selected change when it is the only row selected. */
    fun selectedChange(): ChangeRow? = keyedTree.selection.singleOrNull()?.value as? ChangeRow

    /** The actions for the current selection and checks, chosen at [place]; a selected group or message disables them. */
    fun selectionActions(place: Place = Place.TOOLBAR): SelectionActions {
        val selection = keyedTree.selection
        val items = selection.mapNotNull { it.value as? PanelItem }
        return selectionActions(setup, items, includesOther = items.size < selection.size, checkedFollowUps(), place)
    }

    /** The checked follow-ups shown, in the order they're listed; a follow-up hidden by the filter isn't counted. */
    private fun checkedFollowUps(): List<FollowUpRow> {
        val checked = keyedTree.checked
        if (checked.isEmpty()) return emptyList()
        return followUpSection(model?.followUps.orEmpty(), followUpView).listed.filter { it.key in checked }
    }

    /** The follow-ups as last loaded, for the filter's choices. */
    fun followUpRows(): List<FollowUpRow> = model?.followUps.orEmpty()

    fun projectAction(action: Action): ActionButton? = model?.projectActions?.firstOrNull { it.action == action }

    fun updateFollowUpView(view: FollowUpView) {
        followUpView = view
        settings.setList(HIDDEN_TYPES_SETTING, view.hiddenTypes.sorted())
        settings.setList(HIDDEN_CAPABILITIES_SETTING, view.hiddenCapabilities.sorted())
        settings.setValue(GROUPING_SETTING, view.grouping.name)
        render()
    }

    fun launch(action: Action, target: String?) {
        val command = CommandResolver(setup).command(action, target) ?: return
        ClaudeLauncher.launch(project, action, target, command)
    }

    /** Reloads the lists now, for the tool window's refresh action. */
    fun refreshNow() {
        loadIntoVfs()
        scheduleRefresh(0)
    }

    private fun loadView() = FollowUpView(
        hiddenTypes = settings.getList(HIDDEN_TYPES_SETTING).orEmpty().toSet(),
        hiddenCapabilities = settings.getList(HIDDEN_CAPABILITIES_SETTING).orEmpty().toSet(),
        grouping = Grouping.entries.firstOrNull { it.name == settings.getValue(GROUPING_SETTING) } ?: Grouping.NONE,
    )

    private fun concernsOpenSpec(path: String): Boolean {
        val base = root.toString()
        return listOf("$base/openspec", "$base/.claude").any { path == it || path.startsWith("$it/") }
    }

    /** VFS only reports changes to files it has loaded, so load the OpenSpec folder and refresh it. */
    private fun loadIntoVfs() {
        ApplicationManager.getApplication().executeOnPooledThread {
            LocalFileSystem.getInstance().refreshAndFindFileByNioFile(root.resolve("openspec"))
                ?.let { VfsUtil.markDirtyAndRefresh(true, true, true, it) }
        }
    }

    private fun scheduleRefresh(delayMs: Int) {
        alarm.cancelAllRequests()
        alarm.addRequest(::refresh, delayMs)
    }

    private fun refresh() {
        val current = generation.incrementAndGet()
        val setup = ProjectSetup.detect(root)
        val changes = ChangesSource(ShellEnvironment.map).load(root)
        val followUps = FollowUpsSource.load(root)
        val model = panelModel(setup, root, changes, followUps)
        ApplicationManager.getApplication().invokeLater({
            if (current == generation.get() && !disposed) {
                this.setup = setup
                this.model = model
                render()
            }
        }, project.disposed)
    }

    private fun render() {
        val model = model ?: return
        keyedTree.retainChecked(keptChecks(keyedTree.checked, model.followUps.orEmpty()))
        keyedTree.setNodes(listOfNotNull(changesNode(model.changes), model.followUps?.let(::followUpsNode)))
    }

    private fun changesNode(changes: ChangesSection): KeyedNode = when (changes) {
        is ChangesSection.Message -> KeyedNode(
            CHANGES_KEY, Heading("Changes", 0), group = true,
            children = listOf(KeyedNode("$CHANGES_KEY:message", Message(changes.text))),
        )
        is ChangesSection.Rows -> KeyedNode(
            CHANGES_KEY, Heading("Changes", changes.rows.size), group = true,
            children = changes.rows.map { KeyedNode(it.key, it) },
        )
    }

    private fun followUpsNode(rows: List<FollowUpRow>): KeyedNode {
        val section = followUpSection(rows, followUpView)
        val children = when {
            rows.isEmpty() -> listOf(KeyedNode("$FOLLOW_UPS_KEY:message", Message("No open follow-ups.")))
            section.rows.isEmpty() -> listOf(KeyedNode("$FOLLOW_UPS_KEY:message", Message("No follow-ups match the filter.")))
            section.groups == null -> section.rows.map(::followUpNode)
            else -> section.groups.map { group ->
                KeyedNode(group.key, Heading(group.label, group.rows.size), group = true,
                    children = group.rows.map(::followUpNode), checkable = group.rows.any(::canBeChecked))
            }
        }
        return KeyedNode(FOLLOW_UPS_KEY, Heading("Follow-ups", section.count, section.filtered), group = true,
            children = children, checkable = section.rows.any(::canBeChecked))
    }

    private fun followUpNode(row: FollowUpRow) = KeyedNode(row.key, row, checkable = canBeChecked(row))

    /** Only a follow-up that can be promoted has a checkbox. */
    private fun canBeChecked(row: FollowUpRow) = row.promote != null

    private fun searchText(node: KeyedNode): String = when (val value = node.value) {
        is ChangeRow -> value.name
        is FollowUpRow -> "${value.id} ${value.title}"
        is Heading -> value.name
        else -> ""
    }

    private fun openTarget(item: PanelItem): Path = when (item) {
        is ChangeRow -> changeOpenTarget(item.folder)
        is FollowUpRow -> item.file
    }

    override fun dispose() {
        disposed = true
    }

    private data class Heading(val name: String, val count: Int, val filtered: Boolean = false)

    private data class Message(val text: String)

    /** Draws each row's text, after the checkbox of a checkable row. */
    private class Renderer : CheckboxTreeCellRendererBase(false, true) {
        override fun customizeRenderer(
            tree: JTree, value: Any, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean,
        ) {
            toolTipText = null
            val text = textRenderer
            when (val item = ((value as? DefaultMutableTreeNode)?.userObject as? KeyedNode)?.value) {
                is Heading -> {
                    text.append(item.name)
                    text.append("  ${item.count}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                    if (item.filtered) text.append("  filtered", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                }
                is Message -> text.append(item.text, SimpleTextAttributes.GRAYED_ATTRIBUTES)
                is ChangeRow -> {
                    text.append(item.name)
                    text.append("  ${item.progress}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                }
                is FollowUpRow -> {
                    text.append("${item.id}  ${item.title}")
                    val attributes = if (item.unreadable) SimpleTextAttributes.ERROR_ATTRIBUTES else SimpleTextAttributes.GRAYED_ATTRIBUTES
                    if (item.detail.isNotEmpty()) text.append("  ${item.detail}", attributes)
                    toolTipText = listOf(item.title, item.detail).filter { it.isNotEmpty() }.joinToString(" · ")
                }
            }
        }
    }

    companion object {
        val PANEL = DataKey.create<OpenSpecPanel>("dev.derwa.openspec.panel")

        private const val TOOL_WINDOW_ID = "OpenSpec"
        private const val TOOLBAR_PLACE = "OpenSpecToolbar"
        // A place the platform recognises as a popup, so actions can tell the context menu from the toolbar.
        private val POPUP_PLACE = ActionPlaces.getPopupPlace("OpenSpec")
        private const val DEBOUNCE_MS = 500
        private const val CHANGES_KEY = "changes"
        private const val FOLLOW_UPS_KEY = "followups"
        private const val COLLAPSED_SETTING = "dev.derwa.openspec.panel.collapsed"
        private const val HIDDEN_TYPES_SETTING = "dev.derwa.openspec.panel.hiddenTypes"
        private const val HIDDEN_CAPABILITIES_SETTING = "dev.derwa.openspec.panel.hiddenCapabilities"
        private const val GROUPING_SETTING = "dev.derwa.openspec.panel.grouping"
    }
}
