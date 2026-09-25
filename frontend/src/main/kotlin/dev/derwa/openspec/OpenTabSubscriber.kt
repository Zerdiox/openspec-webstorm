package dev.derwa.openspec

import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.platform.project.projectId
import fleet.rpc.client.durable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Opens the tabs the backend asks this client for, for as long as the project is open. In a standalone
 * IDE the backend opens them directly and never sends any.
 */
@Service(Service.Level.PROJECT)
internal class OpenTabSubscriber(private val project: Project, private val scope: CoroutineScope) {
    fun start() {
        scope.launch {
            // Resubscribes after the connection to the backend is lost and restored.
            durable {
                OpenTabApi.getInstance().openTabRequests(project.projectId()).collect { request ->
                    // Requests arrive on a worker thread; the tab is created on the EDT.
                    withContext(Dispatchers.EDT) { ReworkedTerminalTab().open(project, request) }
                }
            }
        }
    }

    class Starter : ProjectActivity {
        override suspend fun execute(project: Project) = project.service<OpenTabSubscriber>().start()
    }
}
