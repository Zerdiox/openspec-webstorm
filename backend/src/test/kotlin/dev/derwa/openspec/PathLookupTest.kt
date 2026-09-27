package dev.derwa.openspec

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions

class PathLookupTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private fun file(dir: Path, name: String, executable: Boolean = true): Path =
        dir.resolve(name).also {
            Files.writeString(it, "")
            Files.setPosixFilePermissions(it, PosixFilePermissions.fromString(if (executable) "rwxr-xr-x" else "rw-r--r--"))
        }

    @Test
    fun `finds an executable on PATH`() {
        val bin = tmp.newFolder("bin").toPath()
        val tool = file(bin, "openspec")

        assertEquals(tool, findOnPath("openspec", mapOf("PATH" to bin.toString()), windows = false))
    }

    @Test
    fun `skips a file that isn't executable`() {
        val bin = tmp.newFolder("bin").toPath()
        file(bin, "openspec", executable = false)

        assertNull(findOnPath("openspec", mapOf("PATH" to bin.toString()), windows = false))
    }

    @Test
    fun `on Windows takes the cmd launcher npm writes, not the shell script beside it`() {
        val npm = tmp.newFolder("npm").toPath()
        file(npm, "openspec")
        val cmd = file(npm, "openspec.cmd")

        assertEquals(cmd, findOnPath("openspec", mapOf("PATH" to npm.toString(), "PATHEXT" to ".COM;.EXE;.BAT;.CMD"), windows = true))
    }

    @Test
    fun `on Windows follows the order of PATHEXT`() {
        val bin = tmp.newFolder("bin").toPath()
        file(bin, "claude.cmd")
        val exe = file(bin, "claude.exe")

        assertEquals(exe, findOnPath("claude", mapOf("PATH" to bin.toString(), "PATHEXT" to ".EXE;.CMD"), windows = true))
    }

    @Test
    fun `on Windows without PATHEXT still finds a cmd launcher`() {
        val npm = tmp.newFolder("npm").toPath()
        file(npm, "openspec")
        val cmd = file(npm, "openspec.cmd")

        assertEquals(cmd, findOnPath("openspec", mapOf("PATH" to npm.toString()), windows = true))
    }

    @Test
    fun `on Windows ignores a file with no extension`() {
        val npm = tmp.newFolder("npm").toPath()
        file(npm, "openspec")

        assertNull(findOnPath("openspec", mapOf("PATH" to npm.toString()), windows = true))
    }
}
