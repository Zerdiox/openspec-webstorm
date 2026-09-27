package dev.derwa.openspec

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files

class ShellEnvironmentTest {
    @get:Rule
    val tmp = TemporaryFolder()

    /** A HOME set up like Ubuntu's: .profile sources .bashrc, which returns early unless interactive. */
    private fun home(): Map<String, String> {
        val home = tmp.newFolder("home").toPath()
        Files.writeString(home.resolve(".profile"), "[ -f \"\$HOME/.bashrc\" ] && . \"\$HOME/.bashrc\"\n")
        Files.writeString(
            home.resolve(".bashrc"),
            """
            case $- in *i*) ;; *) return;; esac
            echo "welcome from bashrc"
            export PATH="/opt/fake-nvm/bin:${'$'}PATH"
            """.trimIndent() + "\n",
        )
        return mapOf("HOME" to home.toString(), "PATH" to "/usr/bin:/bin")
    }

    @Test
    fun `reads PATH set up only for interactive shells`() {
        val environment = readShellEnvironment("/bin/bash", home())!!

        assertTrue(environment["PATH"]!!.split(':').contains("/opt/fake-nvm/bin"))
    }

    @Test
    fun `keeps the rest of the environment`() {
        val base = home()

        assertEquals(base["HOME"], readShellEnvironment("/bin/bash", base)!!["HOME"])
    }

    @Test
    fun `a shell that can't be started gives no environment`() {
        assertNull(readShellEnvironment("/nonexistent/shell", home()))
    }

    @Test
    fun `uses SHELL as the user's shell`() {
        assertEquals("/bin/zsh", userShell(mapOf("SHELL" to "/bin/zsh"), windows = false))
    }

    @Test
    fun `falls back to sh without SHELL`() {
        assertEquals("/bin/sh", userShell(emptyMap(), windows = false))
    }

    @Test
    fun `has no login shell to read on Windows`() {
        assertNull(userShell(mapOf("SHELL" to "/usr/bin/bash"), windows = true))
    }
}
