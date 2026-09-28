package dev.derwa.openspec

import com.intellij.codeWithMe.ClientId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.file.Path

class ClaudeLauncherTest {
    private val clicker = ClientId("clicker")
    private val openedHere = mutableListOf<OpenTabRequest>()
    private val sent = mutableListOf<Pair<ClientId, OpenTabRequest>>()
    private var notifiedNotFound = 0
    private var notifiedLauncher = 0

    private val posixClaude = Path.of("/usr/local/bin/claude")
    private val windowsClaude = Path.of("C:\\Users\\me\\.local\\bin\\claude.exe")
    private val tricky = "/openspec-propose Don't show \"\$cost\" & 100% ^ | < >\nwhen it's 0"

    private val expected = OpenTabRequest(
        tabName = "apply: board-paging",
        workingDirectory = "/home/me/project",
        command = TypeIntoShell("claude '/openspec-apply-change board-paging'"),
    )

    private fun start(claude: Path?, canOpenHere: Boolean, windows: Boolean = false) = startSession(
        claude = claude,
        windows = windows,
        action = Action.APPLY,
        target = "board-paging",
        basePath = "/home/me/project",
        command = "/openspec-apply-change board-paging",
        clientId = clicker,
        openHere = if (canOpenHere) { request -> openedHere += request } else null,
        sendToClient = { clientId, request -> sent += clientId to request },
        notifyNotFound = { notifiedNotFound++ },
        notifyLauncher = { notifiedLauncher++ },
    )

    @Test
    fun `off Windows the claude command line is typed into the shell, quoted as before`() =
        assertEquals(TypeIntoShell("claude '/openspec-propose Don'\\''t'"), tabCommand(posixClaude, "/openspec-propose Don't", windows = false))

    @Test
    fun `on Windows claude exe is started directly with the command untouched`() =
        assertEquals(StartProgram(listOf(windowsClaude.toString(), tricky)), tabCommand(windowsClaude, tricky, windows = true))

    @Test
    fun `on Windows a com program is started directly too, whatever the extension's case`() {
        val claude = Path.of("C:\\tools\\CLAUDE.COM")
        assertEquals(StartProgram(listOf(claude.toString(), tricky)), tabCommand(claude, tricky, windows = true))
    }

    @Test
    fun `on Windows a cmd or bat launcher can't be started`() {
        assertNull(tabCommand(Path.of("C:\\Users\\me\\AppData\\Roaming\\npm\\claude.cmd"), tricky, windows = true))
        assertNull(tabCommand(Path.of("C:\\tools\\claude.BAT"), tricky, windows = true))
    }

    @Test
    fun `opens the tab here when this IDE has the client-side code`() {
        start(posixClaude, canOpenHere = true)

        assertEquals(listOf(expected), openedHere)
        assertEquals(emptyList<Pair<ClientId, OpenTabRequest>>(), sent)
        assertEquals(0, notifiedNotFound)
    }

    @Test
    fun `sends the request to the client that clicked when this IDE can't open it`() {
        start(posixClaude, canOpenHere = false)

        assertEquals(listOf(clicker to expected), sent)
        assertEquals(0, notifiedNotFound)
    }

    @Test
    fun `on Windows the tab starts claude exe directly`() {
        start(windowsClaude, canOpenHere = true, windows = true)

        assertEquals(
            listOf(expected.copy(command = StartProgram(listOf(windowsClaude.toString(), "/openspec-apply-change board-paging")))),
            openedHere,
        )
    }

    @Test
    fun `on Windows a launcher opens nothing and says to use the native installer`() {
        start(Path.of("C:\\Users\\me\\AppData\\Roaming\\npm\\claude.cmd"), canOpenHere = true, windows = true)
        start(Path.of("C:\\Users\\me\\AppData\\Roaming\\npm\\claude.cmd"), canOpenHere = false, windows = true)

        assertEquals(emptyList<OpenTabRequest>(), openedHere)
        assertEquals(emptyList<Pair<ClientId, OpenTabRequest>>(), sent)
        assertEquals(2, notifiedLauncher)
        assertEquals(0, notifiedNotFound)
    }

    @Test
    fun `opens nothing and notifies when claude isn't found`() {
        start(null, canOpenHere = true)
        start(null, canOpenHere = false, windows = true)

        assertEquals(emptyList<OpenTabRequest>(), openedHere)
        assertEquals(emptyList<Pair<ClientId, OpenTabRequest>>(), sent)
        assertEquals(2, notifiedNotFound)
        assertEquals(0, notifiedLauncher)
    }
}
