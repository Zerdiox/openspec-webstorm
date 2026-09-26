package dev.derwa.openspec

import com.intellij.ide.util.PropertiesComponent
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.EditSourceOnDoubleClickHandler
import com.intellij.util.EditSourceOnEnterKeyHandler
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

/**
 * A tree of [KeyedNode]s that can be replaced wholesale on every refresh while keeping the selection,
 * the collapsed groups and the scroll position by key. Collapsed groups are remembered in [settings]
 * under [collapsedKey]. Opening follows the IDE's own double-click and Enter handling, so whoever
 * holds the tree provides the navigatable data for its selection.
 */
class KeyedTree(
    private val settings: PropertiesComponent,
    private val collapsedKey: String,
    searchText: (KeyedNode) -> String,
) {
    private val root = DefaultMutableTreeNode()
    val tree = Tree(DefaultTreeModel(root)).apply {
        isRootVisible = false
        showsRootHandles = true
        selectionModel.selectionMode = TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION
    }

    private val collapsed = settings.getList(collapsedKey).orEmpty().toMutableSet()
    private var updating = false

    init {
        TreeSpeedSearch.installOn(tree, false) { path -> nodeOf(path)?.let(searchText).orEmpty() }
        EditSourceOnDoubleClickHandler.install(tree)
        EditSourceOnEnterKeyHandler.install(tree)
        tree.addTreeExpansionListener(object : TreeExpansionListener {
            override fun treeExpanded(event: TreeExpansionEvent) = remember(event.path, isCollapsed = false)
            override fun treeCollapsed(event: TreeExpansionEvent) = remember(event.path, isCollapsed = true)
        })
    }

    /** The selected nodes, top to bottom. */
    val selection: List<KeyedNode>
        get() = (tree.selectionRows ?: IntArray(0)).sorted().mapNotNull { nodeOf(tree.getPathForRow(it)) }

    /** Replaces the tree's nodes with [nodes], keeping what was selected and collapsed by key. */
    fun setNodes(nodes: List<KeyedNode>) {
        val selected = selection.map { it.key }
        val visible = tree.visibleRect
        updating = true
        try {
            root.removeAllChildren()
            nodes.forEach { root.add(treeNode(it)) }
            (tree.model as DefaultTreeModel).reload()
            val paths = pathsByKey()
            expandedGroups(nodes, collapsed).forEach { key -> paths[key]?.let(tree::expandPath) }
            // Restoring a selection inside a collapsed group must not open the group.
            tree.expandsSelectedPaths = false
            tree.selectionPaths = keptSelection(selected, nodes).mapNotNull(paths::get).toTypedArray()
        } finally {
            tree.expandsSelectedPaths = true
            updating = false
        }
        tree.scrollRectToVisible(visible)
    }

    private fun remember(path: TreePath, isCollapsed: Boolean) {
        if (updating) return
        val key = nodeOf(path)?.key ?: return
        val changed = if (isCollapsed) collapsed.add(key) else collapsed.remove(key)
        if (changed) settings.setList(collapsedKey, collapsed.sorted())
    }

    private fun treeNode(node: KeyedNode): DefaultMutableTreeNode =
        DefaultMutableTreeNode(node, node.group).apply { node.children.forEach { add(treeNode(it)) } }

    private fun pathsByKey(): Map<String, TreePath> = buildMap {
        fun visit(node: DefaultMutableTreeNode) {
            (node.userObject as? KeyedNode)?.let { put(it.key, TreePath(node.path)) }
            node.children().asSequence().forEach { visit(it as DefaultMutableTreeNode) }
        }
        visit(root)
    }

    private fun nodeOf(path: TreePath?): KeyedNode? =
        (path?.lastPathComponent as? DefaultMutableTreeNode)?.userObject as? KeyedNode
}
