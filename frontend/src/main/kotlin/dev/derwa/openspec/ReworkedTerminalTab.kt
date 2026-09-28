package dev.derwa.openspec

import com.intellij.openapi.project.Project
import com.intellij.terminal.frontend.toolwindow.TerminalToolWindowTabsManager
import org.jetbrains.plugins.terminal.startup.TerminalProcessType

/**
 * Opens a tab in the reworked terminal, the one Claude Code renders correctly in. Its API lives in the
 * Terminal plugin's client-side module, so this runs where the UI is. Uses only what every supported
 * release has.
 */
internal class ReworkedTerminalTab : LocalTabOpener {
    override fun open(project: Project, request: OpenTabRequest) {
        val builder = TerminalToolWindowTabsManager.getInstance(project)
            .createTabBuilder()
            .workingDirectory(request.workingDirectory)
            .tabName(request.tabName)
            .requestFocus(true)
        when (val command = request.command) {
            is TypeIntoShell -> builder.createTab().view.createSendTextBuilder().shouldExecute().send(command.line)
            // Keeps the tab when the program exits, so its last output stays readable.
            is StartProgram -> builder
                .shellCommand(command.command)
                .processType(TerminalProcessType.NON_SHELL)
                .closeOnProcessTermination(false)
                .createTab()
        }
    }
}
