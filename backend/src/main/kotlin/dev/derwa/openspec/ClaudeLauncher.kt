package dev.derwa.openspec

import com.intellij.codeWithMe.ClientId
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import java.nio.file.Path

/** Starts Claude Code with an OpenSpec command in a new terminal tab of the project. */
internal object ClaudeLauncher {
    fun launch(project: Project, action: Action, target: String?, command: String) {
        // The client that clicked is only current here, on the EDT, before the PATH check moves off it.
        val clientId = ClientId.current
        ApplicationManager.getApplication().executeOnPooledThread {
            val claude = findOnPath("claude", ShellEnvironment.map)
            ApplicationManager.getApplication().invokeLater({
                val opener = LocalTabOpener.EP_NAME.extensionList.firstOrNull()
                startSession(
                    claude = claude,
                    action = action,
                    target = target,
                    basePath = project.basePath,
                    command = command,
                    clientId = clientId,
                    openHere = opener?.let { { request: OpenTabRequest -> it.open(project, request) } },
                    sendToClient = { id, request -> OpenTabRequests.getInstance(project).send(id, request) },
                    notifyNotFound = { notifyNotFound(project) },
                    notifyLauncher = { launcher -> notifyLauncher(project, launcher) },
                )
            }, project.disposed)
        }
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

    private fun notifyLauncher(project: Project, launcher: Path) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("OpenSpec")
            .createNotification(
                "Claude Code can't be started from here",
                "The <code>claude</code> in your PATH is <code>${launcher.fileName}</code>, a launcher Windows runs " +
                    "through cmd.exe, which would alter the command. Install Claude Code with its native installer, " +
                    "make sure its <code>claude.exe</code> comes before this one in your PATH, then restart the IDE.",
                NotificationType.ERROR,
            )
            .notify(project)
    }
}

/**
 * What the tab runs to start Claude Code at [claude] with [command] as its first prompt, or null when
 * it can't be started faithfully. On Windows the program itself is started, so no shell reads the
 * command; a `.cmd` or `.bat` launcher would still go through cmd.exe, which re-parses it. Elsewhere
 * the line is typed into the user's shell, so Claude Code gets what their startup files set up.
 */
internal fun tabCommand(claude: Path, command: String, windows: Boolean = SystemInfo.isWindows): TabCommand? = when {
    !windows -> TypeIntoShell(claudeCommandLine(command))
    claude.fileName.toString().substringAfterLast('.', "").lowercase() in setOf("exe", "com") ->
        StartProgram(listOf(claude.toString(), command))
    else -> null
}

/**
 * Opens a tab running [claude] with [command] for [action] on [target], in the project's [basePath]:
 * with [openHere] when this IDE has the client-side code (a standalone IDE), or sent to the client that
 * clicked (a remote-development backend). Opens nothing when `claude` is missing or can't be started.
 */
internal fun startSession(
    claude: Path?,
    action: Action,
    target: String?,
    basePath: String?,
    command: String,
    clientId: ClientId,
    openHere: ((OpenTabRequest) -> Unit)?,
    sendToClient: (ClientId, OpenTabRequest) -> Unit,
    notifyNotFound: () -> Unit,
    notifyLauncher: (Path) -> Unit,
    windows: Boolean = SystemInfo.isWindows,
) {
    if (claude == null) return notifyNotFound()
    val tabCommand = tabCommand(claude, command, windows) ?: return notifyLauncher(claude)
    val request = OpenTabRequest(tabName(action, target), basePath, tabCommand)
    if (openHere != null) openHere(request) else sendToClient(clientId, request)
}
