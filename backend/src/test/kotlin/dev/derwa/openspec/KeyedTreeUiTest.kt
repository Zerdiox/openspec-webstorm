package dev.derwa.openspec

import com.intellij.ide.util.PropertiesComponent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.CheckboxTreeBase.CheckboxTreeCellRendererBase
import com.intellij.util.ui.ThreeStateCheckBox
import com.intellij.util.ui.tree.TreeUtil
import java.awt.event.KeyEvent

class KeyedTreeUiTest : BasePlatformTestCase() {
    // The light test project is shared between tests, so each test remembers its groups under its own key.
    private val settingKey get() = "test.keyedTree.collapsed.$name"

    private fun nodes(vararg changes: String) = listOf(
        KeyedNode("changes", "Changes", group = true, children = changes.map { KeyedNode("change:$it", it) }),
        KeyedNode("followups", "Follow-ups", group = true, children = listOf(KeyedNode("followup:FU-1.md", "FU-1"))),
    )

    private val renderer = CheckboxTreeCellRendererBase(false, true)

    private fun keyedTree() = KeyedTree(PropertiesComponent.getInstance(project), settingKey, renderer) { it.value.toString() }

    private fun followUps(vararg ids: String) = listOf(
        KeyedNode("changes", "Changes", group = true, children = listOf(KeyedNode("change:a", "a"))),
        KeyedNode(
            "followups", "Follow-ups", group = true, checkable = true,
            children = ids.map { KeyedNode("followup:$it.md", it, checkable = true) },
        ),
    )

    private fun KeyedTree.rowOf(key: String) =
        (0 until tree.rowCount).first { (TreeUtil.getLastUserObject(tree.getPathForRow(it)) as KeyedNode).key == key }

    /** The checkbox the renderer draws for [key]'s row, or null when it draws none. */
    private fun KeyedTree.box(key: String): ThreeStateCheckBox.State? {
        val row = rowOf(key)
        renderer.getTreeCellRendererComponent(tree, tree.getPathForRow(row).lastPathComponent, false, true, false, row, false)
        return renderer.threeStateCheckBox.takeIf { it.isVisible }?.state
    }

    fun `test groups start expanded`() {
        val keyed = keyedTree()
        keyed.setNodes(nodes("a", "b"))

        assertEquals(
            listOf("changes", "change:a", "change:b", "followups", "followup:FU-1.md"),
            (0 until keyed.tree.rowCount).map { (TreeUtil.getLastUserObject(keyed.tree.getPathForRow(it)) as KeyedNode).key },
        )
    }

    fun `test selection survives a refresh, and a removed row drops out`() {
        val keyed = keyedTree()
        keyed.setNodes(nodes("a", "b"))
        keyed.tree.setSelectionRows(intArrayOf(2, 4))

        keyed.setNodes(nodes("b", "c"))
        assertEquals(listOf("change:b", "followup:FU-1.md"), keyed.selection.map { it.key })

        keyed.setNodes(nodes("c"))
        assertEquals(listOf("followup:FU-1.md"), keyed.selection.map { it.key })
    }

    fun `test a collapsed group stays collapsed across refreshes and new trees`() {
        val keyed = keyedTree()
        keyed.setNodes(nodes("a"))
        keyed.tree.collapseRow(0)

        keyed.setNodes(nodes("a", "b"))
        assertTrue(keyed.tree.isCollapsed(0))
        assertEquals(listOf("changes"), PropertiesComponent.getInstance(project).getList(settingKey))

        val reopened = keyedTree()
        reopened.setNodes(nodes("a"))
        assertTrue(reopened.tree.isCollapsed(0))
        assertTrue(reopened.tree.isExpanded(1))

        reopened.tree.expandRow(0)
        assertEquals(emptyList<String>(), PropertiesComponent.getInstance(project).getList(settingKey).orEmpty())
    }

    fun `test a collapsed group holding subgroups stays collapsed after a refresh`() {
        val grouped = listOf(
            KeyedNode(
                "followups", "Follow-ups", group = true,
                children = listOf(KeyedNode("followups:type:bug", "bug", group = true, children = listOf(KeyedNode("followup:FU-1.md", "FU-1")))),
            ),
        )
        val keyed = keyedTree()
        keyed.setNodes(grouped)
        keyed.tree.collapseRow(0)

        keyed.setNodes(grouped)

        assertTrue(keyed.tree.isCollapsed(0))
        assertEquals(1, keyed.tree.rowCount)
    }

    fun `test only checkable nodes get a checkbox`() {
        val keyed = keyedTree()
        keyed.setNodes(followUps("F1", "F2"))

        assertNull(keyed.box("changes"))
        assertNull(keyed.box("change:a"))
        assertEquals(ThreeStateCheckBox.State.NOT_SELECTED, keyed.box("followups"))
        assertEquals(ThreeStateCheckBox.State.NOT_SELECTED, keyed.box("followup:F1.md"))
    }

    fun `test checks survive a refresh by key`() {
        val keyed = keyedTree()
        keyed.setNodes(followUps("F1", "F2"))
        keyed.setChecked("followup:F2.md", true)

        keyed.setNodes(followUps("F1", "F2", "F3"))

        assertEquals(setOf("followup:F2.md"), keyed.checked)
        assertEquals(ThreeStateCheckBox.State.SELECTED, keyed.box("followup:F2.md"))
        assertEquals(ThreeStateCheckBox.State.NOT_SELECTED, keyed.box("followup:F3.md"))
    }

    fun `test a node missing from one refresh comes back checked`() {
        val keyed = keyedTree()
        keyed.setNodes(followUps("F1", "F2"))
        keyed.setChecked("followup:F2.md", true)

        keyed.setNodes(followUps("F1"))
        assertEquals(setOf("followup:F2.md"), keyed.checked)

        keyed.setNodes(followUps("F1", "F2"))
        assertEquals(ThreeStateCheckBox.State.SELECTED, keyed.box("followup:F2.md"))
    }

    fun `test a group checks and unchecks only the children in the tree`() {
        val keyed = keyedTree()
        keyed.setNodes(followUps("F1", "F2", "F3"))
        keyed.setChecked("followup:F3.md", true)
        keyed.setNodes(followUps("F1", "F2"))

        keyed.setChecked("followups", false)
        assertEquals(setOf("followup:F3.md"), keyed.checked)

        keyed.setChecked("followups", true)
        assertEquals(setOf("followup:F1.md", "followup:F2.md", "followup:F3.md"), keyed.checked)
    }

    fun `test a group shows whether none, some or all of its children are checked`() {
        val keyed = keyedTree()
        keyed.setNodes(followUps("F1", "F2"))

        keyed.setChecked("followup:F1.md", true)
        assertEquals(ThreeStateCheckBox.State.DONT_CARE, keyed.box("followups"))

        keyed.setChecked("followup:F2.md", true)
        assertEquals(ThreeStateCheckBox.State.SELECTED, keyed.box("followups"))

        keyed.setNodes(followUps("F1", "F2"))
        assertEquals(ThreeStateCheckBox.State.SELECTED, keyed.box("followups"))
    }

    fun `test space toggles the selected rows`() {
        val keyed = keyedTree()
        keyed.setNodes(followUps("F1", "F2", "F3"))
        keyed.tree.setSelectionRows(intArrayOf(keyed.rowOf("followup:F1.md"), keyed.rowOf("followup:F3.md")))

        val space = KeyEvent(keyed.tree, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_SPACE, ' ')
        keyed.tree.keyListeners.forEach { it.keyPressed(space) }

        assertEquals(setOf("followup:F1.md", "followup:F3.md"), keyed.checked)
    }

    fun `test pruning drops checks for keys that are gone`() {
        val keyed = keyedTree()
        keyed.setNodes(followUps("F1", "F2"))
        keyed.setChecked("followup:F1.md", true)
        keyed.setChecked("followup:F2.md", true)

        keyed.retainChecked(setOf("followup:F2.md"))

        assertEquals(setOf("followup:F2.md"), keyed.checked)
        assertEquals(ThreeStateCheckBox.State.NOT_SELECTED, keyed.box("followup:F1.md"))
    }
}
