package dev.derwa.openspec

import com.intellij.codeWithMe.ClientId
import com.intellij.platform.project.ProjectId
import com.intellij.platform.project.findProjectOrNull
import com.intellij.platform.rpc.backend.RemoteApiProvider
import fleet.rpc.remoteApiDescriptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class BackendOpenTabApi : OpenTabApi {
    // Runs as the subscribing client, so ClientId.current is the client the requests are for.
    override suspend fun openTabRequests(projectId: ProjectId): Flow<OpenTabRequest> {
        val project = projectId.findProjectOrNull() ?: return emptyFlow()
        return OpenTabRequests.getInstance(project).requestsFor(ClientId.current)
    }
}

internal class OpenTabApiProvider : RemoteApiProvider {
    override fun RemoteApiProvider.Sink.remoteApis() {
        remoteApi(remoteApiDescriptor<OpenTabApi>()) { BackendOpenTabApi() }
    }
}
