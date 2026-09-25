package dev.derwa.openspec

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.Alarm
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

/** The OpenSpec tool window: the project's changes and follow-ups, and buttons that start Claude Code on them. */
class OpenSpecPanel(private val project: Project, private val root: Path) : Disposable {
    private val content = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = JBUI.Borders.empty(8)
    }
    val component: JComponent = JPanel(BorderLayout()).apply {
        add(JBScrollPane(JPanel(BorderLayout()).apply { add(content, BorderLayout.NORTH) }), BorderLayout.CENTER)
    }

    private val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)
    private val generation = AtomicInteger()
    private var setup = ProjectSetup(null, hasBacklog = false)

    init {
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
        val model = panelModel(setup, changes, followUps)
        ApplicationManager.getApplication().invokeLater({
            if (current == generation.get()) show(setup, model)
        }, project.disposed)
    }

    private fun show(setup: ProjectSetup, model: PanelModel) {
        this.setup = setup
        content.removeAll()

        content.add(row(
            left = model.projectActions.filter { it.action != Action.BACKLOG_REVIEW }.map(::projectButton),
            right = model.projectActions.filter { it.action == Action.BACKLOG_REVIEW }.map(::projectButton),
        ))

        content.add(heading("Changes"))
        when (val changes = model.changes) {
            is ChangesSection.Message -> content.add(indented(JBLabel(changes.text).apply {
                foreground = UIUtil.getContextHelpForeground()
            }))
            is ChangesSection.Rows -> changes.rows.forEach { change ->
                content.add(itemRow(
                    JBLabel(change.name),
                    JBLabel(change.progress).apply { foreground = UIUtil.getContextHelpForeground() },
                    change.actions.map(::actionButton),
                ))
            }
        }

        model.followUps?.let { followUps ->
            content.add(heading("Follow-ups (open)"))
            if (followUps.isEmpty()) {
                content.add(indented(JBLabel("No open follow-ups.").apply { foreground = UIUtil.getContextHelpForeground() }))
            }
            followUps.forEach { followUp ->
                content.add(itemRow(
                    JBLabel("${followUp.id}  ${followUp.title}").apply { toolTipText = followUp.title },
                    JBLabel(followUp.detail).apply {
                        foreground = if (followUp.unreadable) JBColor.RED else UIUtil.getContextHelpForeground()
                    },
                    listOfNotNull(followUp.promote?.let(::actionButton)),
                ))
            }
        }

        content.add(Box.createVerticalStrut(JBUI.scale(8)))
        content.add(row(left = emptyList(), right = listOf(JButton("Refresh").apply {
            addActionListener { loadIntoVfs(); scheduleRefresh(0) }
        })))

        content.revalidate()
        content.repaint()
    }

    private fun projectButton(button: ActionButton): JButton {
        val label = when (button.action) {
            Action.EXPLORE -> "Explore…"
            Action.PROPOSE -> "Propose…"
            else -> "Backlog review"
        }
        return JButton(label).apply {
            isEnabled = button.enabled
            if (!button.enabled) toolTipText = NOT_SET_UP
            addActionListener {
                if (button.action == Action.EXPLORE || button.action == Action.PROPOSE) {
                    val dialog = DescriptionDialog(project, button.action)
                    if (dialog.showAndGet()) launch(button.action, dialog.description)
                } else {
                    launch(button.action, null)
                }
            }
        }
    }

    private fun actionButton(button: ActionButton): JButton =
        JButton(button.action.label.replaceFirstChar { it.uppercase() }).apply {
            isEnabled = button.enabled
            if (!button.enabled) toolTipText = NOT_SET_UP
            addActionListener { launch(button.action, button.target) }
        }

    private fun launch(action: Action, target: String?) {
        val command = CommandResolver(setup).command(action, target) ?: return
        ClaudeLauncher.launch(project, action, target, command)
    }

    private fun heading(text: String): JComponent = row(
        left = listOf(JBLabel(text.uppercase()).apply {
            font = JBUI.Fonts.smallFont().asBold()
            foreground = UIUtil.getContextHelpForeground()
            border = JBUI.Borders.emptyTop(10)
        }),
        right = emptyList(),
    )

    private fun indented(component: JComponent): JComponent = row(listOf(component), emptyList())

    private fun itemRow(name: JComponent, info: JComponent, buttons: List<JButton>): JComponent =
        JPanel(BorderLayout(JBUI.scale(8), 0)).apply {
            border = JBUI.Borders.emptyLeft(8)
            add(name, BorderLayout.CENTER)
            add(JPanel(FlowLayout(FlowLayout.RIGHT, JBUI.scale(4), 0)).apply {
                add(info)
                buttons.forEach(::add)
            }, BorderLayout.EAST)
            alignmentX = JComponent.LEFT_ALIGNMENT
            maximumSize = java.awt.Dimension(Int.MAX_VALUE, preferredSize.height)
        }

    private fun row(left: List<JComponent>, right: List<JComponent>): JComponent =
        JPanel(BorderLayout()).apply {
            add(JPanel(FlowLayout(FlowLayout.LEFT, JBUI.scale(4), 2)).apply { left.forEach(::add) }, BorderLayout.WEST)
            add(JPanel(FlowLayout(FlowLayout.RIGHT, JBUI.scale(4), 2)).apply { right.forEach(::add) }, BorderLayout.EAST)
            alignmentX = JComponent.LEFT_ALIGNMENT
            maximumSize = java.awt.Dimension(Int.MAX_VALUE, preferredSize.height)
        }

    override fun dispose() = Unit

    private companion object {
        const val TOOL_WINDOW_ID = "OpenSpec"
        const val DEBOUNCE_MS = 500
        const val NOT_SET_UP = "OpenSpec isn't set up for Claude Code in this project"
    }
}
