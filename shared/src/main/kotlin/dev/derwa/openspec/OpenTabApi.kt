package dev.derwa.openspec

import com.intellij.platform.project.ProjectId
import com.intellij.platform.rpc.RemoteApiProviderService
import fleet.rpc.RemoteApi
import fleet.rpc.Rpc
import fleet.rpc.remoteApiDescriptor
import kotlinx.coroutines.flow.Flow

/**
 * How the client learns which terminal tabs to open: it subscribes, and the backend, where the panel
 * runs, pushes a request whenever the user chooses an action.
 */
@Rpc
interface OpenTabApi : RemoteApi<Unit> {
    suspend fun openTabRequests(projectId: ProjectId): Flow<OpenTabRequest>

    companion object {
        suspend fun getInstance(): OpenTabApi = RemoteApiProviderService.resolve(remoteApiDescriptor<OpenTabApi>())
    }
}
