package dev.derwa.openspec

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
        assertEquals(PromoteOffer(emptyList(), enabled = false, reason = SELECT_FOLLOW_UPS), actions.promote)
    }

    @Test
    fun `archive is never the next step`() {
        assertEquals(Action.EXPLORE, selectionActions(setup, listOf(changes[0])).changeActions.first().action)
    }

    @Test
    fun `follow-ups are promoted together, in the order listed`() {
        val actions = selectionActions(setup, listOf(followUps[0], followUps[1]))

        assertEquals(PromoteOffer(listOf(followUps[0], followUps[1]), enabled = true, confirm = true), actions.promote)
        assertEquals("FU-0003 FU-0004", actions.promote!!.target)
        assertEquals(true, actions.changeActions.all { !it.enabled && it.reason == SELECT_ONE_CHANGE })
    }

    @Test
    fun `a follow-up without an id can't be promoted`() {
        val actions = selectionActions(setup, listOf(followUps[0], followUps[2]))

        assertEquals(PromoteOffer(emptyList(), enabled = false, reason = NO_ID), actions.promote)
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
        assertEquals(PromoteOffer(emptyList(), enabled = false, reason = ONLY_FOLLOW_UPS), actions.promote)
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
        assertEquals(PromoteOffer(emptyList(), enabled = false, reason = ONLY_FOLLOW_UPS), withFollowUps.promote)
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

    private val more = panelModel(
        setup,
        root,
        ChangesResult.Loaded(emptyList()),
        listOf(
            FollowUp("F5-a.md", id = "F5", title = "Five"),
            FollowUp("F6-b.md", id = "F6", title = "Six"),
            FollowUp("F7-c.md", id = "F7", title = "Seven"),
        ),
    ).followUps!!

    @Test
    fun `the toolbar promotes the checked follow-ups, whatever is selected`() {
        val checked = listOf(more[0], more[2])

        for (selection in listOf(listOf(more[1]), listOf(changes[0]), emptyList())) {
            val promote = selectionActions(setup, selection, checked = checked, place = Place.TOOLBAR).promote!!

            assertEquals(checked, promote.followUps)
            assertEquals("F5 F7", promote.target)
            assertTrue(promote.enabled)
        }
        val withGroup = selectionActions(setup, listOf(more[1]), includesOther = true, checked = checked, place = Place.TOOLBAR)
        assertEquals(checked, withGroup.promote!!.followUps)
    }

    @Test
    fun `the toolbar falls back to the selection when nothing is checked`() {
        val promote = selectionActions(setup, listOf(more[1]), place = Place.TOOLBAR).promote!!

        assertEquals(listOf(more[1]), promote.followUps)
        assertEquals(
            PromoteOffer(emptyList(), enabled = false, reason = ONLY_FOLLOW_UPS),
            selectionActions(setup, listOf(more[1], changes[0]), place = Place.TOOLBAR).promote,
        )
    }

    @Test
    fun `the context menu promotes the selection and ignores checks`() {
        val promote = selectionActions(setup, listOf(more[1]), checked = listOf(more[0], more[2]), place = Place.CONTEXT_MENU).promote!!

        assertEquals(listOf(more[1]), promote.followUps)
        assertEquals("F6", promote.target)
    }

    @Test
    fun `the label says what will be promoted`() {
        assertEquals("Promote", selectionActions(setup, emptyList()).promote!!.label)
        assertEquals("Promote F6", selectionActions(setup, listOf(more[1])).promote!!.label)
        assertEquals("Promote 3", selectionActions(setup, emptyList(), checked = more, place = Place.TOOLBAR).promote!!.label)
    }

    @Test
    fun `only the toolbar asks before promoting several`() {
        assertTrue(selectionActions(setup, emptyList(), checked = more, place = Place.TOOLBAR).promote!!.confirm)
        assertTrue(selectionActions(setup, more.take(2), place = Place.TOOLBAR).promote!!.confirm)
        assertEquals(false, selectionActions(setup, emptyList(), checked = listOf(more[0]), place = Place.TOOLBAR).promote!!.confirm)
        assertEquals(false, selectionActions(setup, more, place = Place.CONTEXT_MENU).promote!!.confirm)
    }

    @Test
    fun `the offer carries the ids and titles it will promote`() {
        val promote = selectionActions(setup, emptyList(), checked = listOf(more[0], more[1]), place = Place.TOOLBAR).promote!!

        assertEquals(listOf("F5" to "Five", "F6" to "Six"), promote.followUps.map { it.id to it.title })
    }

    @Test
    fun `checks don't change the change actions`() {
        assertEquals(
            selectionActions(setup, listOf(changes[1])).changeActions,
            selectionActions(setup, listOf(changes[1]), checked = more, place = Place.TOOLBAR).changeActions,
        )
    }
}
