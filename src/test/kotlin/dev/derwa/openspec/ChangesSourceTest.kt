package dev.derwa.openspec

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions

class ChangesSourceTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private fun fixture(name: String): String =
        javaClass.getResource("/$name")!!.readText()

    /** A directory holding a fake `openspec` that runs [script], usable as PATH. */
    private fun fakeOpenspec(script: String): Path {
        val bin = tmp.newFolder("bin").toPath()
        val tool = bin.resolve("openspec")
        Files.writeString(tool, "#!/bin/sh\n$script\n")
        Files.setPosixFilePermissions(tool, PosixFilePermissions.fromString("rwxr-xr-x"))
        return bin
    }

    private fun load(path: String): ChangesResult =
        ChangesSource(mapOf("PATH" to path)).load(tmp.root.toPath())

    /** The fake tool's folder first, then the system folders its script needs. */
    private fun withSystemPath(bin: Path) = "$bin:/usr/bin:/bin"

    @Test
    fun `parses the captured list with changes`() {
        val changes = parseChanges(fixture("openspec-list-with-changes.json"))
        assertEquals(
            listOf(
                Change("plan-panel-fixes", completedTasks = 0, totalTasks = 15, status = "in-progress"),
                Change("astro-7-upgrade", completedTasks = 0, totalTasks = 27, status = "in-progress"),
            ),
            changes,
        )
    }

    @Test
    fun `parses an empty list`() {
        assertEquals(emptyList<Change>(), parseChanges(fixture("openspec-list-empty.json")))
    }

    @Test
    fun `loads changes by running openspec in the project`() {
        val listing = tmp.newFile("listing.json").toPath()
        Files.writeString(listing, fixture("openspec-list-with-changes.json"))
        val bin = fakeOpenspec("""[ "$*" = "list --json" ] && [ "${'$'}PWD" = "${tmp.root}" ] && cat "$listing"""")

        val result = load(withSystemPath(bin)) as ChangesResult.Loaded

        assertEquals(listOf("plan-panel-fixes", "astro-7-upgrade"), result.changes.map { it.name })
    }

    @Test
    fun `a non-zero exit is unreadable with the first line of stderr`() {
        val bin = fakeOpenspec("echo 'config.yaml is broken' >&2; echo 'second line' >&2; exit 2")

        assertEquals(ChangesResult.Unreadable("config.yaml is broken"), load(withSystemPath(bin)))
    }

    @Test
    fun `a non-zero exit without stderr uses openspec's own status message`() {
        val listing = tmp.newFile("no-root.json").toPath()
        Files.writeString(listing, fixture("openspec-list-no-root.json"))
        val bin = fakeOpenspec("cat \"$listing\"; exit 1")

        assertEquals(ChangesResult.Unreadable("No OpenSpec root found from the current directory."), load(withSystemPath(bin)))
    }

    @Test
    fun `a non-zero exit with no message at all names the exit code`() {
        val bin = fakeOpenspec("exit 3")

        assertEquals(ChangesResult.Unreadable("openspec list exited with code 3"), load(withSystemPath(bin)))
    }

    @Test
    fun `output that isn't the expected json is unreadable`() {
        val bin = fakeOpenspec("echo 'not json'")

        assertTrue(load(withSystemPath(bin)) is ChangesResult.Unreadable)
    }

    @Test
    fun `a missing tool is unreadable and says openspec wasn't found`() {
        val empty = tmp.newFolder("empty-bin")

        assertEquals(
            ChangesResult.Unreadable("openspec wasn't found in your shell's PATH"),
            load(empty.toString()),
        )
    }
}
