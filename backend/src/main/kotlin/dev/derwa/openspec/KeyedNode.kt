package dev.derwa.openspec

/** A tree node identified by a [key] that stays the same across refreshes; a [checkable] node has a checkbox. */
internal data class KeyedNode(
    val key: String,
    val value: Any,
    val children: List<KeyedNode> = emptyList(),
    val group: Boolean = false,
    val checkable: Boolean = false,
)

private fun List<KeyedNode>.flatten(): List<KeyedNode> = flatMap { listOf(it) + it.children.flatten() }

/** The keys of [previous] still in [nodes], in tree order. */
internal fun keptSelection(previous: Collection<String>, nodes: List<KeyedNode>): List<String> {
    val wanted = previous.toSet()
    return nodes.flatten().map { it.key }.filter { it in wanted }
}

/**
 * The groups in [nodes] to expand: all of them, except those [collapsed] and those inside a collapsed
 * group, since expanding a group also expands everything above it.
 */
internal fun expandedGroups(nodes: List<KeyedNode>, collapsed: Set<String>): List<String> =
    nodes.filter { it.group && it.key !in collapsed }
        .flatMap { listOf(it.key) + expandedGroups(it.children, collapsed) }
