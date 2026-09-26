package dev.derwa.openspec

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.file.Path

class SelectionActionsTest {
    private val root = Path.of("/project")
    private val setup = ProjectSetup(Delivery.SKILLS, hasBacklog = true)
    private val model = panelModel(
        setup,
        root,
        ChangesResult.Loaded(
            listOf(
                Change("astro-7-upgrade", completedTasks = 0, totalTasks = 0, status = "no-tasks"),
                Change("plan-panel-fixes", completedTasks = 3, totalTasks = 10, status = "in-progress"),
            ),
        ),
        listOf(
            FollowUp("FU-0003-a.md", id = "FU-0003", title = "A"),
            FollowUp("FU-0004-b.md", id = "FU-0004", title = "B"),
            FollowUp("FU-x.md", unreadable = true),
        ),
    )
    private val changes = (model.changes as ChangesSection.Rows).rows
    private val followUps = model.followUps!!

    @Test
    fun `one change offers its actions, next step first`() {
        val actions = selectionActions(setup, listOf(changes[1]))

        assertEquals(
            listOf(
                OfferedAction(Action.APPLY, "plan-panel-fixes", enabled = true),
                OfferedAction(Action.VERIFY, "plan-panel-fixes", enabled = true),
                OfferedAction(Action.ARCHIVE, "plan-panel-fixes", enabled = true),
                OfferedAction(Action.EXPLORE, "plan-panel-fixes", enabled = true),
            ),
            actions.changeActions,
        )
        assertEquals(OfferedAction(Action.PROMOTE, null, enabled = false, reason = SELECT_FOLLOW_UPS), actions.promote)
    }

    @Test
    fun `archive is never the next step`() {
        assertEquals(Action.EXPLORE, selectionActions(setup, listOf(changes[0])).changeActions.first().action)
    }

    @Test
    fun `follow-ups are promoted together, in the order listed`() {
        val actions = selectionActions(setup, listOf(followUps[0], followUps[1]))

        assertEquals(OfferedAction(Action.PROMOTE, "FU-0003 FU-0004", enabled = true), actions.promote)
        assertEquals(true, actions.changeActions.all { !it.enabled && it.reason == SELECT_ONE_CHANGE })
    }

    @Test
    fun `a follow-up without an id can't be promoted`() {
        val actions = selectionActions(setup, listOf(followUps[0], followUps[2]))

        assertEquals(OfferedAction(Action.PROMOTE, null, enabled = false, reason = NO_ID), actions.promote)
    }

    @Test
    fun `several changes disable the change actions`() {
        val actions = selectionActions(setup, changes)

        assertEquals(4, actions.changeActions.size)
        assertEquals(true, actions.changeActions.all { !it.enabled && it.reason == SELECT_ONE_CHANGE && it.target == null })
        assertEquals(SELECT_FOLLOW_UPS, actions.promote!!.reason)
    }

    @Test
    fun `a mixed selection disables everything`() {
        val actions = selectionActions(setup, listOf(changes[0], followUps[0]))

        assertEquals(true, actions.changeActions.all { !it.enabled && it.reason == SELECT_ONE_CHANGE })
        assertEquals(OfferedAction(Action.PROMOTE, null, enabled = false, reason = ONLY_FOLLOW_UPS), actions.promote)
    }

    @Test
    fun `nothing selected disables the selection's actions`() {
        val actions = selectionActions(setup, emptyList())

        assertEquals(
            listOf(Action.APPLY, Action.VERIFY, Action.ARCHIVE, Action.EXPLORE),
            actions.changeActions.map { it.action },
        )
        assertEquals(true, actions.changeActions.all { !it.enabled && it.reason == SELECT_ONE_CHANGE })
        assertEquals(false, actions.promote!!.enabled)
    }

    @Test
    fun `a group selected along with rows disables the selection's actions`() {
        val withChange = selectionActions(setup, listOf(changes[1]), includesOther = true)
        val withFollowUps = selectionActions(setup, listOf(followUps[0]), includesOther = true)

        assertEquals(true, withChange.changeActions.all { !it.enabled && it.reason == SELECT_ONE_CHANGE })
        assertEquals(OfferedAction(Action.PROMOTE, null, enabled = false, reason = ONLY_FOLLOW_UPS), withFollowUps.promote)
    }

    @Test
    fun `no promote without a backlog`() {
        assertNull(selectionActions(ProjectSetup(Delivery.SKILLS, hasBacklog = false), listOf(changes[0])).promote)
    }

    @Test
    fun `without claude code set up the actions say so`() {
        val notSetUp = ProjectSetup(null, hasBacklog = true)
        val rows = (panelModel(notSetUp, root, ChangesResult.Loaded(listOf(Change("c", 0, 0, "no-tasks"))), null)
            .changes as ChangesSection.Rows).rows

        val actions = selectionActions(notSetUp, rows)

        assertEquals(true, actions.changeActions.all { !it.enabled && it.reason == NOT_SET_UP })
    }
}
