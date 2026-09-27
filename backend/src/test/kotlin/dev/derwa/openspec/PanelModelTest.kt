package dev.derwa.openspec

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Path

class PanelModelTest {
    @get:Rule
    val temp = TemporaryFolder()

    private val root = Path.of("/project")
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
        val model = panelModel(skillsWithBacklog, root, twoChanges, emptyList())

        assertEquals(
            listOf(Action.EXPLORE, Action.PROPOSE, Action.BACKLOG_REVIEW),
            model.projectActions.map { it.action },
        )
    }

    @Test
    fun `no backlog review without a backlog`() {
        val model = panelModel(skillsOnly, root, twoChanges, null)

        assertEquals(listOf(Action.EXPLORE, Action.PROPOSE), model.projectActions.map { it.action })
    }

    @Test
    fun `changes are listed with done out of total, and apply first while tasks remain`() {
        val rows = (panelModel(skillsWithBacklog, root, twoChanges, null).changes as ChangesSection.Rows).rows

        assertEquals(listOf("astro-7-upgrade", "plan-panel-fixes"), rows.map { it.name })
        assertEquals("3/10", rows[1].progress)
        assertEquals(
            listOf(
                ActionButton(Action.APPLY, "plan-panel-fixes", enabled = true),
                ActionButton(Action.VERIFY, "plan-panel-fixes", enabled = true),
                ActionButton(Action.ARCHIVE, "plan-panel-fixes", enabled = true),
                ActionButton(Action.EXPLORE, "plan-panel-fixes", enabled = true),
            ),
            rows[1].actions,
        )
    }

    @Test
    fun `a change without tasks offers explore first`() {
        assertEquals(
            listOf(Action.EXPLORE, Action.APPLY, Action.VERIFY, Action.ARCHIVE),
            actionsOf(Change("new-idea", completedTasks = 0, totalTasks = 0, status = "no-tasks")),
        )
    }

    @Test
    fun `a change with all tasks done offers verify first`() {
        assertEquals(
            listOf(Action.VERIFY, Action.ARCHIVE, Action.APPLY, Action.EXPLORE),
            actionsOf(Change("done", completedTasks = 10, totalTasks = 10, status = "complete")),
        )
    }

    @Test
    fun `a change with more done than total counts as all done`() {
        assertEquals(
            listOf(Action.VERIFY, Action.ARCHIVE, Action.APPLY, Action.EXPLORE),
            actionsOf(Change("odd", completedTasks = 11, totalTasks = 10, status = "complete")),
        )
    }

    private fun actionsOf(change: Change): List<Action> =
        (panelModel(skillsWithBacklog, root, ChangesResult.Loaded(listOf(change)), null).changes as ChangesSection.Rows)
            .rows.single().actions.map { it.action }

    @Test
    fun `unreadable changes say so and why`() {
        val model = panelModel(skillsWithBacklog, root, ChangesResult.Unreadable("openspec wasn't found"), null)

        assertEquals(ChangesSection.Message("Couldn't read changes: openspec wasn't found"), model.changes)
    }

    @Test
    fun `no changes in flight says so`() {
        val model = panelModel(skillsWithBacklog, root, ChangesResult.Loaded(emptyList()), null)

        assertEquals(ChangesSection.Message("No changes in flight."), model.changes)
    }

    @Test
    fun `no follow-ups section without a backlog, even if the folder exists`() {
        val model = panelModel(skillsOnly, root, twoChanges, listOf(FollowUp("FU-0001-a.md", id = "FU-0001")))

        assertNull(model.followUps)
    }

    @Test
    fun `a backlog without a follow-ups folder is an empty section`() {
        assertEquals(emptyList<FollowUpRow>(), panelModel(skillsWithBacklog, root, twoChanges, null).followUps)
    }

    @Test
    fun `follow-ups show id, title, type and capability, with promote`() {
        val followUp = FollowUp(
            "FU-0033-x.md", id = "FU-0033", title = "A refused move is untested",
            type = "test-gap", capability = "board-milestones",
        )

        val row = panelModel(skillsWithBacklog, root, twoChanges, listOf(followUp)).followUps!!.single()

        assertEquals(
            FollowUpRow(
                id = "FU-0033",
                title = "A refused move is untested",
                detail = "test-gap · board-milestones",
                promote = ActionButton(Action.PROMOTE, "FU-0033", enabled = true),
                file = Path.of("/project/openspec/backlog/followup/FU-0033-x.md"),
                type = "test-gap",
                capability = "board-milestones",
            ),
            row,
        )
    }

    @Test
    fun `an unreadable follow-up shows its file once, with why, and can't be promoted`() {
        val problem = Problem("YAML error on line 4", 4, "mapping values are not allowed here")
        val followUp = FollowUp("FU-0040-broken.md", id = "FU-0040", problem = problem)

        val row = panelModel(skillsWithBacklog, root, twoChanges, listOf(followUp)).followUps!!.single()

        assertEquals(
            FollowUpRow(
                id = "FU-0040",
                title = "FU-0040-broken.md",
                detail = "unreadable: YAML error on line 4",
                promote = null,
                file = Path.of("/project/openspec/backlog/followup/FU-0040-broken.md"),
                problem = problem,
            ),
            row,
        )
        assertEquals(true, row.unreadable)
        assertEquals(4, row.line)
    }

    @Test
    fun `a readable follow-up whose type is unreadable isn't marked unreadable`() {
        val followUp = FollowUp("FU-0050-x.md", id = "FU-0050", title = "Odd", type = "unreadable")

        val row = panelModel(skillsWithBacklog, root, twoChanges, listOf(followUp)).followUps!!.single()
        assertEquals(false, row.unreadable)
        assertNull(row.line)
    }

    @Test
    fun `an unreadable follow-up without any id is identified by its file name`() {
        val row = panelModel(skillsWithBacklog, root, twoChanges, listOf(FollowUp("FU-x.md", problem = Problem("no frontmatter", 1))))
            .followUps!!.single()

        assertEquals("FU-x.md", row.id)
        assertEquals("FU-x.md", row.title)
        assertEquals("unreadable: no frontmatter", row.detail)
        assertNull(row.promote)
    }

    @Test
    fun `a readable follow-up without any id can't be promoted`() {
        val row = panelModel(skillsWithBacklog, root, twoChanges, listOf(FollowUp("notes.md", title = "Notes")))
            .followUps!!.single()

        assertEquals("notes.md", row.id)
        assertNull(row.promote)
    }

    @Test
    fun `workflow buttons are disabled when openspec isn't set up for claude`() {
        val model = panelModel(ProjectSetup(null, hasBacklog = true), root, twoChanges, emptyList())

        assertEquals(listOf(false, false, true), model.projectActions.map { it.enabled })
        val row = (model.changes as ChangesSection.Rows).rows.first()
        assertEquals(listOf(false, false, false, false), row.actions.map { it.enabled })
    }

    @Test
    fun `tab names are the action and its target`() {
        assertEquals("apply: plan-panel-fixes", tabName(Action.APPLY, "plan-panel-fixes"))
        assertEquals("promote: FU-0033", tabName(Action.PROMOTE, "FU-0033"))
        assertEquals("explore: plan-panel-fixes", tabName(Action.EXPLORE, "plan-panel-fixes"))
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

    @Test
    fun `a change opens its proposal when there is one`() {
        val folder = temp.newFolder("with-proposal").toPath()
        val proposal = folder.resolve("proposal.md").also { it.toFile().writeText("# Proposal") }

        assertEquals(proposal, changeOpenTarget(folder))
    }

    @Test
    fun `a change without a proposal opens its folder`() {
        val folder = temp.newFolder("scaffolded").toPath()

        assertEquals(folder, changeOpenTarget(folder))
    }

    private fun write(folder: Path, relative: String): Path =
        folder.resolve(relative).also {
            it.parent.toFile().mkdirs()
            it.toFile().writeText("# ${it.fileName}")
        }

    @Test
    fun `a change's artifacts are the files it has`() {
        val folder = temp.newFolder("full").toPath()
        val proposal = write(folder, "proposal.md")
        val design = write(folder, "design.md")
        val tasks = write(folder, "tasks.md")

        val artifacts = changeArtifacts(folder)

        assertEquals(proposal, artifacts.proposal)
        assertEquals(design, artifacts.design)
        assertEquals(tasks, artifacts.tasks)
    }

    @Test
    fun `a scaffolded change has no artifacts`() {
        val folder = temp.newFolder("scaffolded").toPath()

        assertEquals(ChangeArtifacts(null, null, null, emptyList()), changeArtifacts(folder))
    }

    @Test
    fun `an empty specs folder gives no specs`() {
        val folder = temp.newFolder("empty-specs").toPath()
        folder.resolve("specs").toFile().mkdirs()

        assertEquals(emptyList<DeltaSpec>(), changeArtifacts(folder).specs)
    }

    @Test
    fun `specs are listed by capability in name order`() {
        val folder = temp.newFolder("two-specs").toPath()
        val panel = write(folder, "specs/workflow-panel/spec.md")
        val browser = write(folder, "specs/spec-browser/spec.md")

        assertEquals(
            listOf(DeltaSpec("spec-browser", browser), DeltaSpec("workflow-panel", panel)),
            changeArtifacts(folder).specs,
        )
    }

    @Test
    fun `a nested spec is labelled with its domain`() {
        val folder = temp.newFolder("nested").toPath()
        val auth = write(folder, "specs/identity/user-auth/spec.md")

        assertEquals(listOf(DeltaSpec("identity/user-auth", auth)), changeArtifacts(folder).specs)
    }

    @Test
    fun `an unreadable specs folder gives the specs it could read`() {
        val folder = temp.newFolder("unreadable").toPath()
        val panel = write(folder, "specs/workflow-panel/spec.md")
        val locked = folder.resolve("specs/locked").toFile().apply { mkdirs() }
        assumeTrue(locked.setReadable(false) && !locked.canRead())
        try {
            assertEquals(listOf(DeltaSpec("workflow-panel", panel)), changeArtifacts(folder).specs)
        } finally {
            locked.setReadable(true)
        }
    }

    @Test
    fun `only spec files count as specs`() {
        val folder = temp.newFolder("stray").toPath()
        val panel = write(folder, "specs/workflow-panel/spec.md")
        write(folder, "specs/workflow-panel/notes.md")
        write(folder, "specs/README.md")
        folder.resolve("specs/not-yet").toFile().mkdirs()

        assertEquals(listOf(DeltaSpec("workflow-panel", panel)), changeArtifacts(folder).specs)
    }
}
