package dev.derwa.openspec

import com.intellij.codeWithMe.ClientId
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

/**
 * The project's requests to open a tab in a client, each for the client whose click made it. A
 * request sent while that client isn't subscribed is dropped.
 */
@Service(Service.Level.PROJECT)
class OpenTabRequests {
    private class Addressed(val clientId: ClientId, val request: OpenTabRequest)

    private val requests = MutableSharedFlow<Addressed>(extraBufferCapacity = 16)

    fun send(clientId: ClientId, request: OpenTabRequest) {
        requests.tryEmit(Addressed(clientId, request))
    }

    fun requestsFor(clientId: ClientId): Flow<OpenTabRequest> =
        requests.filter { it.clientId == clientId }.map { it.request }

    companion object {
        fun getInstance(project: Project): OpenTabRequests = project.getService(OpenTabRequests::class.java)
    }
}
