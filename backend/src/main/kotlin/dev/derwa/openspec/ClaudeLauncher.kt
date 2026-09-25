package dev.derwa.openspec

import com.intellij.codeWithMe.ClientId
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project

/** Starts Claude Code with an OpenSpec command in a new terminal tab of the project. */
object ClaudeLauncher {
    fun launch(project: Project, action: Action, target: String?, command: String) {
        // The client that clicked is only current here, on the EDT, before the PATH check moves off it.
        val clientId = ClientId.current
        val request = tabRequest(action, target, project.basePath, command)
        ApplicationManager.getApplication().executeOnPooledThread {
            val found = findOnPath("claude", ShellEnvironment.map) != null
            ApplicationManager.getApplication().invokeLater({
                val opener = LocalTabOpener.EP_NAME.extensionList.firstOrNull()
                startSession(
                    claudeFound = found,
                    request = request,
                    clientId = clientId,
                    openHere = opener?.let { { request: OpenTabRequest -> it.open(project, request) } },
                    sendToClient = { id, request -> OpenTabRequests.getInstance(project).send(id, request) },
                    notifyNotFound = { notifyNotFound(project) },
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
}

/** The tab to open for [action] on [target]: Claude Code running [command] in the project's [basePath]. */
internal fun tabRequest(action: Action, target: String?, basePath: String?, command: String) =
    OpenTabRequest(tabName(action, target), basePath, claudeCommandLine(command))

/**
 * Opens [request]'s tab with [openHere] when this IDE has the client-side code (a standalone IDE), or
 * sends it to the client that clicked (a remote-development backend). Opens nothing without `claude`.
 */
internal fun startSession(
    claudeFound: Boolean,
    request: OpenTabRequest,
    clientId: ClientId,
    openHere: ((OpenTabRequest) -> Unit)?,
    sendToClient: (ClientId, OpenTabRequest) -> Unit,
    notifyNotFound: () -> Unit,
) {
    when {
        !claudeFound -> notifyNotFound()
        openHere != null -> openHere(request)
        else -> sendToClient(clientId, request)
    }
}
