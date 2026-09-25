package dev.derwa.openspec

import com.intellij.codeWithMe.ClientId
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenTabRequestsTest {
    private val requests = OpenTabRequests()
    private val me = ClientId("me")
    private val guest = ClientId("guest")

    private fun request(name: String) = OpenTabRequest(name, "/home/me/project", "claude '/openspec-explore'")

    @Test
    fun `a client gets only the requests from its own clicks`() = runBlocking {
        val received = async(start = CoroutineStart.UNDISPATCHED) { requests.requestsFor(me).take(2).toList() }

        requests.send(me, request("first"))
        requests.send(guest, request("guest's"))
        requests.send(me, request("second"))

        assertEquals(listOf(request("first"), request("second")), withTimeout(5_000) { received.await() })
    }

    @Test
    fun `a request sent before the client subscribes is dropped`() = runBlocking {
        requests.send(me, request("before"))
        val received = async(start = CoroutineStart.UNDISPATCHED) { requests.requestsFor(me).take(1).toList() }

        requests.send(me, request("after"))

        assertEquals(listOf(request("after")), withTimeout(5_000) { received.await() })
    }
}
