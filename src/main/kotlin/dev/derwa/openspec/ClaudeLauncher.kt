package dev.derwa.openspec

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.terminal.TerminalToolWindowManager

/** Starts Claude Code with an OpenSpec command in a new terminal tab of the project. */
object ClaudeLauncher {
    fun launch(project: Project, action: Action, target: String?, command: String) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val found = findOnPath("claude", ShellEnvironment.map) != null
            ApplicationManager.getApplication().invokeLater({
                if (found) openTab(project, tabName(action, target), command) else notifyNotFound(project)
            }, project.disposed)
        }
    }

    private fun openTab(project: Project, name: String, command: String) {
        val commandLine = claudeCommandLine(command)
        openPreferringReworked(
            reworked = { ReworkedTerminalTab.open(project, name, commandLine) },
            classic = { openClassicTab(project, name, commandLine) },
        )
    }

    // The only tab the backend of a remote-development IDE can open. Deprecated, but in the Terminal
    // plugin's main module with the same signature in every supported release.
    @Suppress("DEPRECATION")
    private fun openClassicTab(project: Project, name: String, commandLine: String) {
        TerminalToolWindowManager.getInstance(project)
            .createShellWidget(project.basePath, name, true, true)
            .sendCommandToExecute(commandLine)
    }

    private fun notifyNotFound(project: Project) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("OpenSpec")
            .createNotification(
                "Claude Code wasn't found",
                "No <code>claude</code> command is in your shell's PATH, so no terminal tab was opened.",
                NotificationType.ERROR,
            )
            .notify(project)
    }
}
