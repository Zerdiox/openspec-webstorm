package dev.derwa.openspec

import org.junit.Assert.assertEquals
import org.junit.Test

class TabOpeningTest {
    private val opened = mutableListOf<String>()

    @Test
    fun `opens the reworked tab when it can`() {
        openPreferringReworked(reworked = { opened += "reworked" }, classic = { opened += "classic" })

        assertEquals(listOf("reworked"), opened)
    }

    @Test
    fun `falls back to classic when the client-side terminal api isn't loaded`() {
        openPreferringReworked(
            reworked = { throw NoClassDefFoundError("com/intellij/terminal/frontend/toolwindow/TerminalToolWindowTabsManager") },
            classic = { opened += "classic" },
        )

        assertEquals(listOf("classic"), opened)
    }

    @Test
    fun `falls back to classic when the reworked tab can't be created`() {
        openPreferringReworked(
            reworked = { throw IllegalStateException("service not registered") },
            classic = { opened += "classic" },
        )

        assertEquals(listOf("classic"), opened)
    }
}
