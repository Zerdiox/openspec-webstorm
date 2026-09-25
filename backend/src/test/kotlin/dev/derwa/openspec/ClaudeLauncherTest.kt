package dev.derwa.openspec

import com.intellij.codeWithMe.ClientId
import org.junit.Assert.assertEquals
import org.junit.Test

class ClaudeLauncherTest {
    private val clicker = ClientId("clicker")
    private val openedHere = mutableListOf<OpenTabRequest>()
    private val sent = mutableListOf<Pair<ClientId, OpenTabRequest>>()
    private var notified = 0

    private val expected = OpenTabRequest(
        tabName = "apply: board-paging",
        workingDirectory = "/home/me/project",
        commandLine = "claude '/openspec-apply-change board-paging'",
    )

    private fun start(claudeFound: Boolean, canOpenHere: Boolean) = startSession(
        claudeFound = claudeFound,
        request = tabRequest(Action.APPLY, "board-paging", "/home/me/project", "/openspec-apply-change board-paging"),
        clientId = clicker,
        openHere = if (canOpenHere) { request -> openedHere += request } else null,
        sendToClient = { clientId, request -> sent += clientId to request },
        notifyNotFound = { notified++ },
    )

    @Test
    fun `the request carries the tab name, the project's base path and the quoted claude command line`() =
        assertEquals(expected, tabRequest(Action.APPLY, "board-paging", "/home/me/project", "/openspec-apply-change board-paging"))

    @Test
    fun `opens the tab here when this IDE has the client-side code`() {
        start(claudeFound = true, canOpenHere = true)

        assertEquals(listOf(expected), openedHere)
        assertEquals(emptyList<Pair<ClientId, OpenTabRequest>>(), sent)
        assertEquals(0, notified)
    }

    @Test
    fun `sends the request to the client that clicked when this IDE can't open it`() {
        start(claudeFound = true, canOpenHere = false)

        assertEquals(listOf(clicker to expected), sent)
        assertEquals(0, notified)
    }

    @Test
    fun `opens nothing and notifies when claude isn't found`() {
        start(claudeFound = false, canOpenHere = true)
        start(claudeFound = false, canOpenHere = false)

        assertEquals(emptyList<OpenTabRequest>(), openedHere)
        assertEquals(emptyList<Pair<ClientId, OpenTabRequest>>(), sent)
        assertEquals(2, notified)
    }
}
