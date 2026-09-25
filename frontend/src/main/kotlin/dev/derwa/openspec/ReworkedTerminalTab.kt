package dev.derwa.openspec

import com.intellij.openapi.project.Project
import com.intellij.terminal.frontend.toolwindow.TerminalToolWindowTabsManager

/**
 * Opens a tab in the reworked terminal, the one Claude Code renders correctly in. Its API lives in the
 * Terminal plugin's client-side module, so this runs where the UI is. Uses only what every supported
 * release has.
 */
internal class ReworkedTerminalTab : LocalTabOpener {
    override fun open(project: Project, request: OpenTabRequest) {
        val tab = TerminalToolWindowTabsManager.getInstance(project)
            .createTabBuilder()
            .workingDirectory(request.workingDirectory)
            .tabName(request.tabName)
            .requestFocus(true)
            .createTab()
        tab.view.createSendTextBuilder().shouldExecute().send(request.commandLine)
    }
}
