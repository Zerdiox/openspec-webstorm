package dev.derwa.openspec

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PanelModelTest {
    private val skillsWithBacklog = ProjectSetup(Delivery.SKILLS, hasBacklog = true)
    private val skillsOnly = ProjectSetup(Delivery.SKILLS, hasBacklog = false)
    private val twoChanges = ChangesResult.Loaded(
        listOf(
            Change("astro-7-upgrade", completedTasks = 0, totalTasks = 27, status = "in-progress"),
            Change("plan-panel-fixes", completedTasks = 3, totalTasks = 10, status = "in-progress"),
        ),
    )

    @Test
    fun `project actions are explore, propose and backlog review with a backlog`() {
        val model = panelModel(skillsWithBacklog, twoChanges, emptyList())

        assertEquals(
            listOf(Action.EXPLORE, Action.PROPOSE, Action.BACKLOG_REVIEW),
            model.projectActions.map { it.action },
        )
    }

    @Test
    fun `no backlog review without a backlog`() {
        val model = panelModel(skillsOnly, twoChanges, null)

        assertEquals(listOf(Action.EXPLORE, Action.PROPOSE), model.projectActions.map { it.action })
    }

    @Test
    fun `changes are listed with done out of total and apply, verify, archive`() {
        val rows = (panelModel(skillsWithBacklog, twoChanges, null).changes as ChangesSection.Rows).rows

        assertEquals(listOf("astro-7-upgrade", "plan-panel-fixes"), rows.map { it.name })
        assertEquals("3/10", rows[1].progress)
        assertEquals(
            listOf(
                ActionButton(Action.APPLY, "plan-panel-fixes", enabled = true),
                ActionButton(Action.VERIFY, "plan-panel-fixes", enabled = true),
                ActionButton(Action.ARCHIVE, "plan-panel-fixes", enabled = true),
            ),
            rows[1].actions,
        )
    }

    @Test
    fun `unreadable changes say so and why`() {
        val model = panelModel(skillsWithBacklog, ChangesResult.Unreadable("openspec wasn't found"), null)

        assertEquals(ChangesSection.Message("Couldn't read changes: openspec wasn't found"), model.changes)
    }

    @Test
    fun `no changes in flight says so`() {
        val model = panelModel(skillsWithBacklog, ChangesResult.Loaded(emptyList()), null)

        assertEquals(ChangesSection.Message("No changes in flight."), model.changes)
    }

    @Test
    fun `no follow-ups section without a backlog, even if the folder exists`() {
        val model = panelModel(skillsOnly, twoChanges, listOf(FollowUp("FU-0001-a.md", id = "FU-0001")))

        assertNull(model.followUps)
    }

    @Test
    fun `a backlog without a follow-ups folder is an empty section`() {
        assertEquals(emptyList<FollowUpRow>(), panelModel(skillsWithBacklog, twoChanges, null).followUps)
    }

    @Test
    fun `follow-ups show id, title, type and capability, with promote`() {
        val followUp = FollowUp(
            "FU-0033-x.md", id = "FU-0033", title = "A refused move is untested",
            type = "test-gap", capability = "board-milestones",
        )

        val row = panelModel(skillsWithBacklog, twoChanges, listOf(followUp)).followUps!!.single()

        assertEquals(
            FollowUpRow(
                id = "FU-0033",
                title = "A refused move is untested",
                detail = "test-gap · board-milestones",
                promote = ActionButton(Action.PROMOTE, "FU-0033", enabled = true),
            ),
            row,
        )
    }

    @Test
    fun `an unreadable follow-up is identified by its file and marked unreadable`() {
        val followUp = FollowUp("FU-0040-broken.md", id = "FU-0040", unreadable = true)

        val row = panelModel(skillsWithBacklog, twoChanges, listOf(followUp)).followUps!!.single()

        assertEquals(
            FollowUpRow(
                id = "FU-0040",
                title = "FU-0040-broken.md",
                detail = "unreadable",
                promote = ActionButton(Action.PROMOTE, "FU-0040", enabled = true),
                unreadable = true,
            ),
            row,
        )
    }

    @Test
    fun `a readable follow-up whose type is unreadable isn't marked unreadable`() {
        val followUp = FollowUp("FU-0050-x.md", id = "FU-0050", title = "Odd", type = "unreadable")

        assertEquals(false, panelModel(skillsWithBacklog, twoChanges, listOf(followUp)).followUps!!.single().unreadable)
    }

    @Test
    fun `a follow-up without any id can't be promoted`() {
        val row = panelModel(skillsWithBacklog, twoChanges, listOf(FollowUp("FU-x.md", unreadable = true)))
            .followUps!!.single()

        assertEquals("FU-x.md", row.id)
        assertNull(row.promote)
    }

    @Test
    fun `workflow buttons are disabled when openspec isn't set up for claude`() {
        val model = panelModel(ProjectSetup(null, hasBacklog = true), twoChanges, emptyList())

        assertEquals(listOf(false, false, true), model.projectActions.map { it.enabled })
        val row = (model.changes as ChangesSection.Rows).rows.first()
        assertEquals(listOf(false, false, false), row.actions.map { it.enabled })
    }

    @Test
    fun `tab names are the action and its target`() {
        assertEquals("apply: plan-panel-fixes", tabName(Action.APPLY, "plan-panel-fixes"))
        assertEquals("promote: FU-0033", tabName(Action.PROMOTE, "FU-0033"))
        assertEquals("backlog review", tabName(Action.BACKLOG_REVIEW, null))
    }

    @Test
    fun `a description's tab name is its first line, shortened`() {
        assertEquals("explore: board paging", tabName(Action.EXPLORE, "board paging\nmore detail"))
        assertEquals(
            "propose: Don't show \$cost when it's 0 and…",
            tabName(Action.PROPOSE, "Don't show \$cost when it's 0 and also other things"),
        )
    }
}
