package dev.derwa.openspec

/** The filter value and group for a follow-up that has no type or no capability. */
const val NONE_VALUE = "none"

private val KNOWN_TYPES = listOf("bug", "tech-debt", "test-gap", "idea")

enum class Grouping { NONE, TYPE, CAPABILITY }

/** How the follow-ups are shown: the types and capabilities hidden, and how they're grouped. */
data class FollowUpView(
    val hiddenTypes: Set<String> = emptySet(),
    val hiddenCapabilities: Set<String> = emptySet(),
    val grouping: Grouping = Grouping.NONE,
)

data class FollowUpGroup(val key: String, val label: String, val rows: List<FollowUpRow>)

/** The follow-ups shown, and their [groups] when grouped; [filtered] when the filter hides any. */
data class FollowUpSection(val rows: List<FollowUpRow>, val groups: List<FollowUpGroup>?, val filtered: Boolean) {
    val count get() = rows.size
}

data class FilterChoices(val types: List<String>, val capabilities: List<String>)

private val FollowUpRow.typeValue get() = type ?: NONE_VALUE
private val FollowUpRow.capabilityValue get() = capability ?: NONE_VALUE

fun followUpSection(rows: List<FollowUpRow>, view: FollowUpView): FollowUpSection {
    // An unreadable follow-up has no type or capability to filter on, and hiding it would hide the problem.
    val shown = rows.filter {
        it.unreadable || (it.typeValue !in view.hiddenTypes && it.capabilityValue !in view.hiddenCapabilities)
    }
    val groups = when (view.grouping) {
        Grouping.NONE -> null
        Grouping.TYPE -> groups(shown, "type", FollowUpRow::typeValue, ::typeOrder)
        Grouping.CAPABILITY -> groups(shown, "capability", FollowUpRow::capabilityValue) { it.sorted() }
    }
    return FollowUpSection(shown, groups, filtered = shown.size < rows.size)
}

private fun groups(
    rows: List<FollowUpRow>,
    kind: String,
    value: (FollowUpRow) -> String,
    order: (List<String>) -> List<String>,
): List<FollowUpGroup> {
    val readable = rows.filter { !it.unreadable }.groupBy(value)
    val named = order(readable.keys.filter { it != NONE_VALUE })
    val valueGroups = (named + listOfNotNull(NONE_VALUE.takeIf { it in readable })).map {
        FollowUpGroup("followups:$kind:$it", it, readable.getValue(it))
    }
    val unreadable = rows.filter { it.unreadable }
    return valueGroups + listOfNotNull(unreadable.takeIf { it.isNotEmpty() }?.let {
        FollowUpGroup("followups:unreadable", "unreadable", it)
    })
}

private fun typeOrder(types: List<String>) =
    KNOWN_TYPES.filter { it in types } + types.filter { it !in KNOWN_TYPES }.sorted()

/** The filter's choices: the known types always, plus whatever the open follow-ups use. */
fun filterChoices(rows: List<FollowUpRow>): FilterChoices {
    val readable = rows.filter { !it.unreadable }
    fun withNone(values: List<String>, anyMissing: Boolean) = values + listOfNotNull(NONE_VALUE.takeIf { anyMissing })
    return FilterChoices(
        types = withNone(typeOrder(KNOWN_TYPES + readable.mapNotNull { it.type }.distinct()), readable.any { it.type == null }),
        capabilities = withNone(readable.mapNotNull { it.capability }.distinct().sorted(), readable.any { it.capability == null }),
    )
}
