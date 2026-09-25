package dev.derwa.openspec

import com.intellij.openapi.project.Project
import com.intellij.terminal.frontend.toolwindow.TerminalToolWindowTabsManager

/**
 * Opens a tab in the reworked terminal, the one Claude Code renders correctly in. Its API lives in the
 * Terminal plugin's client-side module, which isn't loaded on a remote-development backend, so only
 * reach this class through [openPreferringReworked]. Uses only what every supported release has.
 */
internal object ReworkedTerminalTab {
    fun open(project: Project, name: String, commandLine: String) {
        val tab = TerminalToolWindowTabsManager.getInstance(project)
            .createTabBuilder()
            .workingDirectory(project.basePath)
            .tabName(name)
            .requestFocus(true)
            .createTab()
        tab.view.createSendTextBuilder().shouldExecute().send(commandLine)
    }
}
