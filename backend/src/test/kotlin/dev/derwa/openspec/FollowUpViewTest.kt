package dev.derwa.openspec

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.file.Path

class FollowUpViewTest {
    private val rows = panelModel(
        ProjectSetup(Delivery.SKILLS, hasBacklog = true),
        Path.of("/project"),
        ChangesResult.Loaded(emptyList()),
        listOf(
            FollowUp("FU-0001-a.md", id = "FU-0001", title = "A", type = "idea", capability = "panel"),
            FollowUp("FU-0002-b.md", id = "FU-0002", title = "B", type = "bug", capability = "panel"),
            FollowUp("FU-0003-c.md", id = "FU-0003", title = "C", type = "tech-debt", capability = "launcher"),
            FollowUp("FU-0004-d.md", id = "FU-0004", title = "D"),
            FollowUp("FU-0005-e.md", id = "FU-0005", unreadable = true),
        ),
    ).followUps!!

    private fun ids(rows: List<FollowUpRow>) = rows.map { it.id }

    @Test
    fun `without a filter everything is shown, flat`() {
        val section = followUpSection(rows, FollowUpView())

        assertEquals(listOf("FU-0001", "FU-0002", "FU-0003", "FU-0004", "FU-0005"), ids(section.rows))
        assertNull(section.groups)
        assertEquals(false, section.filtered)
        assertEquals(5, section.count)
    }

    @Test
    fun `hidden types are filtered out, and the section says so`() {
        val section = followUpSection(rows, FollowUpView(hiddenTypes = setOf("idea", NONE_VALUE)))

        assertEquals(listOf("FU-0002", "FU-0003", "FU-0005"), ids(section.rows))
        assertEquals(true, section.filtered)
        assertEquals(3, section.count)
    }

    @Test
    fun `hidden capabilities are filtered out`() {
        val section = followUpSection(rows, FollowUpView(hiddenCapabilities = setOf("panel")))

        assertEquals(listOf("FU-0003", "FU-0004", "FU-0005"), ids(section.rows))
    }

    @Test
    fun `a filter that hides nothing present isn't shown as filtered`() {
        assertEquals(false, followUpSection(rows, FollowUpView(hiddenTypes = setOf("test-gap"))).filtered)
    }

    @Test
    fun `unreadable follow-ups are never filtered out`() {
        val section = followUpSection(
            rows,
            FollowUpView(hiddenTypes = setOf("idea", "bug", "tech-debt", NONE_VALUE), hiddenCapabilities = setOf(NONE_VALUE)),
        )

        assertEquals(listOf("FU-0005"), ids(section.rows))
    }

    @Test
    fun `grouping by type orders known types first, then none, then unreadable`() {
        val groups = followUpSection(rows, FollowUpView(grouping = Grouping.TYPE)).groups!!

        assertEquals(listOf("bug", "tech-debt", "idea", "none", "unreadable"), groups.map { it.label })
        assertEquals(listOf(listOf("FU-0002"), listOf("FU-0003"), listOf("FU-0001"), listOf("FU-0004"), listOf("FU-0005")),
            groups.map { ids(it.rows) })
        assertEquals("followups:type:bug", groups[0].key)
        assertEquals("followups:unreadable", groups[4].key)
    }

    @Test
    fun `grouping by capability is alphabetical, then none, then unreadable`() {
        val groups = followUpSection(rows, FollowUpView(grouping = Grouping.CAPABILITY)).groups!!

        assertEquals(listOf("launcher", "panel", "none", "unreadable"), groups.map { it.label })
        assertEquals(listOf("FU-0001", "FU-0002"), ids(groups[1].rows))
        assertEquals("followups:capability:panel", groups[1].key)
    }

    @Test
    fun `grouping follows the filter`() {
        val section = followUpSection(rows, FollowUpView(hiddenTypes = setOf("bug"), grouping = Grouping.CAPABILITY))

        assertEquals(listOf("FU-0001"), ids(section.groups!!.single { it.label == "panel" }.rows))
        assertEquals(4, section.count)
    }

    @Test
    fun `filter choices come from the open follow-ups, with the known types always offered`() {
        val choices = filterChoices(rows)

        assertEquals(listOf("bug", "tech-debt", "test-gap", "idea", NONE_VALUE), choices.types)
        assertEquals(listOf("launcher", "panel", NONE_VALUE), choices.capabilities)
    }

    @Test
    fun `none is only offered when a follow-up lacks the field`() {
        val choices = filterChoices(rows.filter { it.id in setOf("FU-0001", "FU-0005") })

        assertEquals(listOf("bug", "tech-debt", "test-gap", "idea"), choices.types)
        assertEquals(listOf("panel"), choices.capabilities)
    }
}
