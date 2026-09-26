package dev.derwa.openspec

import com.intellij.ide.util.PropertiesComponent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ui.tree.TreeUtil

class KeyedTreeUiTest : BasePlatformTestCase() {
    // The light test project is shared between tests, so each test remembers its groups under its own key.
    private val settingKey get() = "test.keyedTree.collapsed.$name"

    private fun nodes(vararg changes: String) = listOf(
        KeyedNode("changes", "Changes", group = true, children = changes.map { KeyedNode("change:$it", it) }),
        KeyedNode("followups", "Follow-ups", group = true, children = listOf(KeyedNode("followup:FU-1.md", "FU-1"))),
    )

    private fun keyedTree() = KeyedTree(PropertiesComponent.getInstance(project), settingKey) { it.value.toString() }

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
}
