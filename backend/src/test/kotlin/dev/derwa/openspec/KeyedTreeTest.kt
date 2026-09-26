package dev.derwa.openspec

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.file.Path

class KeyedTreeTest {
    private val nodes = listOf(
        KeyedNode("changes", "Changes", group = true, children = listOf(KeyedNode("change:a", "a"), KeyedNode("change:b", "b"))),
        KeyedNode(
            "followups", "Follow-ups", group = true,
            children = listOf(
                KeyedNode("followups:type:bug", "bug", group = true, children = listOf(KeyedNode("followup:FU-1.md", "1"))),
            ),
        ),
    )

    @Test
    fun `selection keeps the keys still present, in tree order`() {
        assertEquals(
            listOf("change:b", "followup:FU-1.md"),
            keptSelection(listOf("followup:FU-1.md", "gone", "change:b"), nodes),
        )
    }

    @Test
    fun `selection of an empty tree is empty`() {
        assertEquals(emptyList<String>(), keptSelection(listOf("change:a"), emptyList()))
    }

    @Test
    fun `every group is expanded unless collapsed`() {
        assertEquals(listOf("changes", "followups"), expandedGroups(nodes, collapsed = setOf("followups:type:bug")))
        assertEquals(listOf("changes", "followups", "followups:type:bug"), expandedGroups(nodes, collapsed = emptySet()))
    }

    @Test
    fun `groups inside a collapsed group aren't expanded, so they don't reopen it`() {
        assertEquals(listOf("changes"), expandedGroups(nodes, collapsed = setOf("followups")))
    }

    @Test
    fun `an empty group is still a group`() {
        assertEquals(listOf("changes"), expandedGroups(listOf(KeyedNode("changes", "Changes", group = true)), emptySet()))
    }

    @Test
    fun `rows have stable keys`() {
        val model = panelModel(
            ProjectSetup(Delivery.SKILLS, hasBacklog = true),
            Path.of("/project"),
            ChangesResult.Loaded(listOf(Change("plan-panel-fixes", 3, 10, "in-progress"))),
            listOf(FollowUp("FU-0003-a.md", id = "FU-0003", title = "A"), FollowUp("FU-x.md", unreadable = true)),
        )

        assertEquals("change:plan-panel-fixes", (model.changes as ChangesSection.Rows).rows.single().key)
        assertEquals(listOf("followup:FU-0003-a.md", "followup:FU-x.md"), model.followUps!!.map { it.key })
    }

    @Test
    fun `rows carry what they open`() {
        val model = panelModel(
            ProjectSetup(Delivery.SKILLS, hasBacklog = true),
            Path.of("/project"),
            ChangesResult.Loaded(listOf(Change("plan-panel-fixes", 3, 10, "in-progress"))),
            listOf(FollowUp("FU-x.md", unreadable = true)),
        )

        assertEquals(
            Path.of("/project/openspec/changes/plan-panel-fixes"),
            (model.changes as ChangesSection.Rows).rows.single().folder,
        )
        assertEquals(Path.of("/project/openspec/backlog/followup/FU-x.md"), model.followUps!!.single().file)
    }
}
