package dev.derwa.openspec

import com.intellij.ide.util.PropertiesComponent
import com.intellij.ui.CheckboxTreeBase.CheckPolicy
import com.intellij.ui.CheckboxTreeBase.CheckboxTreeCellRendererBase
import com.intellij.ui.CheckboxTreeHelper
import com.intellij.ui.CheckboxTreeListener
import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.EditSourceOnDoubleClickHandler
import com.intellij.util.EditSourceOnEnterKeyHandler
import com.intellij.util.EventDispatcher
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

/**
 * A tree of [KeyedNode]s that can be replaced wholesale on every refresh while keeping the selection,
 * the collapsed groups, the checks and the scroll position by key. Collapsed groups are remembered in
 * [settings] under [collapsedKey]; checks are kept only while the tree lives. Opening follows the IDE's
 * own double-click and Enter handling, so whoever holds the tree provides the navigatable data for its
 * selection. [renderer] draws the checkbox of checkable nodes and whatever the holder draws next to it.
 */
internal class KeyedTree(
    private val settings: PropertiesComponent,
    private val collapsedKey: String,
    renderer: CheckboxTreeCellRendererBase,
    searchText: (KeyedNode) -> String,
) {
    private val root = CheckedTreeNode()
    val tree = Tree(DefaultTreeModel(root)).apply {
        selectionModel.selectionMode = TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION
    }

    private val collapsed = settings.getList(collapsedKey).orEmpty().toMutableSet()
    private val checkedKeys = mutableSetOf<String>()
    private val checkboxes = CheckboxTreeHelper(CHECK_POLICY, EventDispatcher.create(CheckboxTreeListener::class.java).apply {
        addListener(object : CheckboxTreeListener {
            override fun nodeStateChanged(node: CheckedTreeNode) = onChecked(node)
        })
    })
    private var updating = false

    /** The keys of the checked rows, including those left out of the last refresh. */
    val checked: Set<String> get() = checkedKeys.toSet()

    init {
        TreeSpeedSearch.installOn(tree, false) { path -> nodeOf(path)?.let(searchText).orEmpty() }
        EditSourceOnDoubleClickHandler.install(tree)
        EditSourceOnEnterKeyHandler.install(tree)
        // After the edit-source handlers: the checkbox click handling consumes double-clicks, which
        // would otherwise stop them from opening a row.
        checkboxes.initTree(tree, tree, renderer)
        tree.addTreeExpansionListener(object : TreeExpansionListener {
            override fun treeExpanded(event: TreeExpansionEvent) = remember(event.path, isCollapsed = false)
            override fun treeCollapsed(event: TreeExpansionEvent) = remember(event.path, isCollapsed = true)
        })
    }

    /** The selected nodes, top to bottom. */
    val selection: List<KeyedNode>
        get() = (tree.selectionRows ?: IntArray(0)).sorted().mapNotNull { nodeOf(tree.getPathForRow(it)) }

    /** Replaces the tree's nodes with [nodes], keeping what was selected, collapsed and checked by key. */
    fun setNodes(nodes: List<KeyedNode>) {
        val selected = selection.map { it.key }
        val visible = tree.visibleRect
        updating = true
        try {
            root.removeAllChildren()
            nodes.forEach { root.add(treeNode(it)) }
            syncGroups(root)
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

    /** Checks or unchecks the node with [key], as a click on its checkbox would. */
    fun setChecked(key: String, checked: Boolean) {
        val node = pathsByKey()[key]?.lastPathComponent as? CheckedTreeNode ?: return
        checkboxes.setNodeState(tree, node, checked)
    }

    /** Drops the checks of every key not in [keys]. */
    fun retainChecked(keys: Set<String>) {
        if (!checkedKeys.retainAll(keys)) return
        forEachChecked(root) { node, key -> node.isChecked = key in checkedKeys }
        syncGroups(root)
        tree.repaint()
    }

    private fun onChecked(node: CheckedTreeNode) {
        val keyed = node.userObject as? KeyedNode ?: return
        if (!keyed.group) {
            if (node.isChecked) checkedKeys.add(keyed.key) else checkedKeys.remove(keyed.key)
        }
        syncGroups(root)
    }

    /**
     * Makes each group checked exactly when all its checkable children are, so its box is drawn
     * checked, unchecked or partial, and a click on a partial group checks everything.
     */
    private fun syncGroups(node: DefaultMutableTreeNode) {
        val children = node.children().toList().filterIsInstance<DefaultMutableTreeNode>()
        children.forEach(::syncGroups)
        val keyed = node.userObject as? KeyedNode ?: return
        if (keyed.group && node is CheckedTreeNode) {
            val checkable = children.filterIsInstance<CheckedTreeNode>()
            node.isChecked = checkable.isNotEmpty() && checkable.all { it.isChecked }
        }
    }

    private fun forEachChecked(node: DefaultMutableTreeNode, action: (CheckedTreeNode, String) -> Unit) {
        val keyed = node.userObject as? KeyedNode
        if (node is CheckedTreeNode && keyed != null && !keyed.group) action(node, keyed.key)
        node.children().asSequence().forEach { forEachChecked(it as DefaultMutableTreeNode, action) }
    }

    private fun remember(path: TreePath, isCollapsed: Boolean) {
        if (updating) return
        val key = nodeOf(path)?.key ?: return
        val changed = if (isCollapsed) collapsed.add(key) else collapsed.remove(key)
        if (changed) settings.setList(collapsedKey, collapsed.sorted())
    }

    private fun treeNode(node: KeyedNode): DefaultMutableTreeNode {
        val treeNode = if (node.checkable) {
            CheckedTreeNode(node).apply { isChecked = node.key in checkedKeys }
        } else {
            DefaultMutableTreeNode(node)
        }
        treeNode.allowsChildren = node.group
        node.children.forEach { treeNode.add(treeNode(it)) }
        return treeNode
    }

    private fun pathsByKey(): Map<String, TreePath> = buildMap {
        fun visit(node: DefaultMutableTreeNode) {
            (node.userObject as? KeyedNode)?.let { put(it.key, TreePath(node.path)) }
            node.children().asSequence().forEach { visit(it as DefaultMutableTreeNode) }
        }
        visit(root)
    }

    private fun nodeOf(path: TreePath?): KeyedNode? =
        (path?.lastPathComponent as? DefaultMutableTreeNode)?.userObject as? KeyedNode

    private companion object {
        // A group checks and unchecks its children; a child never changes its group, whose box is
        // drawn from the children instead. Clicking outside the box only selects.
        val CHECK_POLICY = CheckPolicy(
            checkChildrenWithCheckedParent = true,
            uncheckChildrenWithUncheckedParent = true,
            checkParentWithCheckedChild = false,
            uncheckParentWithUncheckedChild = false,
            checkByRowClick = false,
        )
    }
}
