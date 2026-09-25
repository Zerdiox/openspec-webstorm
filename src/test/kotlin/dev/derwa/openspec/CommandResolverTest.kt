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

class CommandResolverTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private fun project(vararg dirs: String, files: List<String> = emptyList()): Path {
        val root = tmp.newFolder().toPath()
        dirs.forEach { Files.createDirectories(root.resolve(it)) }
        files.forEach {
            Files.createDirectories(root.resolve(it).parent)
            Files.writeString(root.resolve(it), "")
        }
        return root
    }

    @Test
    fun `skills delivery is detected from openspec skills`() {
        val setup = ProjectSetup.detect(project(".claude/skills/openspec-apply-change"))
        assertEquals(Delivery.SKILLS, setup.delivery)
    }

    @Test
    fun `opsx delivery is detected from the opsx commands folder`() {
        val setup = ProjectSetup.detect(project(".claude/commands/opsx"))
        assertEquals(Delivery.OPSX, setup.delivery)
    }

    @Test
    fun `skills win when both deliveries are present`() {
        val setup = ProjectSetup.detect(project(".claude/skills/openspec-explore", ".claude/commands/opsx"))
        assertEquals(Delivery.SKILLS, setup.delivery)
    }

    @Test
    fun `unrelated skills are not an openspec delivery`() {
        val setup = ProjectSetup.detect(project(".claude/skills/e2e-testing"))
        assertNull(setup.delivery)
    }

    @Test
    fun `backlog is detected from the backlog command`() {
        assertTrue(ProjectSetup.detect(project(files = listOf(".claude/commands/backlog.md"))).hasBacklog)
        assertFalse(ProjectSetup.detect(project(".claude/commands")).hasBacklog)
    }

    private val skills = CommandResolver(ProjectSetup(Delivery.SKILLS, hasBacklog = true))
    private val opsx = CommandResolver(ProjectSetup(Delivery.OPSX, hasBacklog = true))

    @Test
    fun `skills commands for every workflow action`() {
        assertEquals("/openspec-explore board paging", skills.command(Action.EXPLORE, "board paging"))
        assertEquals("/openspec-propose board paging", skills.command(Action.PROPOSE, "board paging"))
        assertEquals("/openspec-apply-change plan-panel-fixes", skills.command(Action.APPLY, "plan-panel-fixes"))
        assertEquals("/openspec-verify-change plan-panel-fixes", skills.command(Action.VERIFY, "plan-panel-fixes"))
        assertEquals("/openspec-archive-change plan-panel-fixes", skills.command(Action.ARCHIVE, "plan-panel-fixes"))
    }

    @Test
    fun `exploring a change sends its name`() {
        assertEquals("/openspec-explore plan-panel-fixes", skills.command(Action.EXPLORE, "plan-panel-fixes"))
        assertEquals("/opsx:explore plan-panel-fixes", opsx.command(Action.EXPLORE, "plan-panel-fixes"))
    }

    @Test
    fun `opsx commands for every workflow action`() {
        assertEquals("/opsx:explore board paging", opsx.command(Action.EXPLORE, "board paging"))
        assertEquals("/opsx:propose board paging", opsx.command(Action.PROPOSE, "board paging"))
        assertEquals("/opsx:apply plan-panel-fixes", opsx.command(Action.APPLY, "plan-panel-fixes"))
        assertEquals("/opsx:verify plan-panel-fixes", opsx.command(Action.VERIFY, "plan-panel-fixes"))
        assertEquals("/opsx:archive plan-panel-fixes", opsx.command(Action.ARCHIVE, "plan-panel-fixes"))
    }

    @Test
    fun `backlog commands with a backlog, whatever the delivery`() {
        for (resolver in listOf(skills, opsx)) {
            assertEquals("/backlog review", resolver.command(Action.BACKLOG_REVIEW, null))
            assertEquals("/backlog promote FU-0033", resolver.command(Action.PROMOTE, "FU-0033"))
        }
    }

    @Test
    fun `no backlog commands without a backlog`() {
        val resolver = CommandResolver(ProjectSetup(Delivery.SKILLS, hasBacklog = false))
        assertNull(resolver.command(Action.BACKLOG_REVIEW, null))
        assertNull(resolver.command(Action.PROMOTE, "FU-0033"))
        assertEquals("/openspec-apply-change x", resolver.command(Action.APPLY, "x"))
    }

    @Test
    fun `no workflow commands without an openspec delivery`() {
        val resolver = CommandResolver(ProjectSetup(null, hasBacklog = true))
        for (action in listOf(Action.EXPLORE, Action.PROPOSE, Action.APPLY, Action.VERIFY, Action.ARCHIVE)) {
            assertNull(resolver.command(action, "x"))
        }
        assertEquals("/backlog review", resolver.command(Action.BACKLOG_REVIEW, null))
    }

    @Test
    fun `a blank description sends the bare command`() {
        assertEquals("/openspec-explore", skills.command(Action.EXPLORE, "  \n"))
    }

    @Test
    fun `a description keeps its inner line breaks`() {
        assertEquals("/openspec-propose line one\nline two", skills.command(Action.PROPOSE, "line one\nline two\n"))
    }
}
