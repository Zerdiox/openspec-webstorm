package dev.derwa.openspec

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import java.nio.file.Path

class FollowUpsSourceTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val root: Path get() = tmp.root.toPath()
    private val backlog: Path get() = root.resolve("openspec/backlog/followup")

    private fun followUp(fileName: String, content: String, folder: Path = backlog) {
        Files.createDirectories(folder)
        Files.writeString(folder.resolve(fileName), content)
    }

    @Test
    fun `no backlog folder means no follow-ups section`() {
        Files.createDirectories(root.resolve("openspec"))

        assertNull(FollowUpsSource.load(root))
    }

    @Test
    fun `an empty backlog folder is an empty list`() {
        Files.createDirectories(backlog)

        assertEquals(emptyList<FollowUp>(), FollowUpsSource.load(root))
    }

    @Test
    fun `reads id, title, type and capability from the frontmatter`() {
        followUp(
            "FU-0033-refused-milestone-move-untested.md",
            """
            ---
            id: FU-0033
            title: A refused milestone move is untested
            found: 2026-09-05
            source: conversation
            capability: board-milestones
            type: test-gap
            size: S
            ---

            # FU-0033 — body
            """.trimIndent(),
        )

        assertEquals(
            listOf(
                FollowUp(
                    file = "FU-0033-refused-milestone-move-untested.md",
                    id = "FU-0033",
                    title = "A refused milestone move is untested",
                    type = "test-gap",
                    capability = "board-milestones",
                ),
            ),
            FollowUpsSource.load(root),
        )
    }

    @Test
    fun `quoted titles are unquoted`() {
        followUp(
            "FU-0012-modversion-event-shape.md",
            "---\nid: FU-0012\ntitle: '`ModVersionLinked` carries a different shape: it''s odd'\ntype: tech-debt\n---\n",
        )
        followUp(
            "FU-0013-double.md",
            "---\nid: FU-0013\ntitle: \"Say \\\"hi\\\": now\"\ntype: bug\n---\n",
        )

        assertEquals(
            listOf("`ModVersionLinked` carries a different shape: it's odd", "Say \"hi\": now"),
            FollowUpsSource.load(root)!!.map { it.title },
        )
    }

    @Test
    fun `missing fields are left empty, and the id falls back to the file name`() {
        followUp("F12-board-pager.md", "---\ntitle: Boards show everything\ncapability: \n---\n")

        assertEquals(
            listOf(FollowUp(file = "F12-board-pager.md", id = "F12", title = "Boards show everything")),
            FollowUpsSource.load(root),
        )
    }

    @Test
    fun `broken yaml is listed as unreadable, identified by its file`() {
        followUp("FU-0040-broken.md", "---\nid: FU-0040\ntitle: A title: with: colons\n  bad: [indent\n---\n")

        val followUp = FollowUpsSource.load(root)!!.single()
        assertEquals("FU-0040-broken.md", followUp.file)
        assertEquals("FU-0040", followUp.id)
        assertTrue(followUp.unreadable)
    }

    @Test
    fun `a yaml error gives its line in the file and the parser's message`() {
        followUp("F40-broken.md", "---\nid: F40\ntype: bug\ntitle: A: b\n---\n")

        val problem = FollowUpsSource.load(root)!!.single().problem!!
        assertEquals("YAML error on line 4", problem.reason)
        assertEquals(4, problem.line)
        assertEquals("mapping values are not allowed here", problem.message)
    }

    @Test
    fun `a file without frontmatter is listed as unreadable`() {
        followUp("FU-0041-no-frontmatter.md", "# Just a heading\n")

        assertEquals(
            listOf(FollowUp(file = "FU-0041-no-frontmatter.md", id = "FU-0041", problem = Problem("no frontmatter", 1))),
            FollowUpsSource.load(root),
        )
    }

    @Test
    fun `frontmatter that isn't closed is unreadable`() {
        followUp("F42-open.md", "---\nid: F42\ntitle: Never closed\n")

        assertEquals(Problem("frontmatter isn't closed", 1), FollowUpsSource.load(root)!!.single().problem)
    }

    @Test
    fun `frontmatter that isn't a map is unreadable`() {
        followUp("F43-list.md", "---\n- one\n- two\n---\n")
        followUp("F44-scalar.md", "---\njust text\n---\n")
        followUp("F45-empty.md", "---\n---\n")

        assertEquals(
            List(3) { Problem("frontmatter isn't a list of fields", 2) },
            FollowUpsSource.load(root)!!.map { it.problem },
        )
    }

    @Test
    fun `an unreadable file keeps the id from its file name`() {
        followUp("F46-broken.md", "no frontmatter\n")

        assertEquals("F46", FollowUpsSource.load(root)!!.single().id)
    }

    @Test
    fun `a readable file has no problem`() {
        followUp("F47-fine.md", "---\nid: F47\ntitle: Fine\n---\n")

        val followUp = FollowUpsSource.load(root)!!.single()
        assertNull(followUp.problem)
        assertFalse(followUp.unreadable)
    }

    @Test
    fun `resolved follow-ups and other files are excluded`() {
        followUp("FU-0002-open.md", "---\nid: FU-0002\ntitle: Open\n---\n")
        followUp("FU-0003-done.md", "---\nid: FU-0003\ntitle: Done\n---\n", folder = backlog.resolve("resolved"))
        followUp("README.md", "# Follow-ups\n")

        assertEquals(listOf("FU-0002"), FollowUpsSource.load(root)!!.map { it.id })
    }

    @Test
    fun `follow-ups are listed in file name order, numbers compared as numbers`() {
        followUp("F10-b.md", "---\nid: F10\n---\n")
        followUp("F2-a.md", "---\nid: F2\n---\n")
        followUp("F1-c.md", "---\nid: F1\n---\n")

        assertEquals(listOf("F1", "F2", "F10"), FollowUpsSource.load(root)!!.map { it.id })
    }

    @Test
    fun `follow-ups are listed whatever their id scheme`() {
        followUp("F5-new.md", "---\nid: F5\n---\n")
        followUp("FU-0002-old.md", "---\nid: FU-0002\n---\n")
        followUp("BUG-7-other.md", "---\nid: BUG-7\n---\n")

        assertEquals(listOf("BUG-7", "F5", "FU-0002"), FollowUpsSource.load(root)!!.map { it.id })
    }
}
