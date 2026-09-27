package dev.derwa.openspec

import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.error.YAMLException
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText

internal const val FOLLOW_UPS_FOLDER = "openspec/backlog/followup"

internal data class FollowUp(
    val file: String,
    val id: String? = null,
    val title: String? = null,
    val type: String? = null,
    val capability: String? = null,
    val unreadable: Boolean = false,
)

/** Reads a project's open follow-ups from the frontmatter of its backlog files. */
internal object FollowUpsSource {
    // Projects number follow-ups their own way (FU-0042, F42, ...): a leading word and number.
    private val FILE_ID = Regex("^[A-Za-z]+-?\\d+")
    private val NUMBER = Regex("\\d+")

    /** The open follow-ups, or null when the project has no follow-ups backlog. */
    fun load(projectRoot: Path): List<FollowUp>? {
        val folder = projectRoot.resolve(FOLLOW_UPS_FOLDER)
        if (!folder.isDirectory()) return null
        return folder.listDirectoryEntries("*.md")
            .filter { it.isRegularFile() && !it.name.equals("README.md", ignoreCase = true) }
            .sortedWith(compareBy<Path>(::naturalKey).thenBy { it.name })
            .map(::read)
    }

    /** The file name with its numbers zero-padded, so F2 sorts before F10. */
    private fun naturalKey(file: Path): String = NUMBER.replace(file.name) { it.value.trimStart('0').padStart(20, '0') }

    private fun read(file: Path): FollowUp {
        val fileId = FILE_ID.find(file.name)?.value
        val unreadable = FollowUp(file = file.name, id = fileId, unreadable = true)
        val fields = try {
            frontmatter(file.readText())?.let(::parseYaml)
        } catch (e: YAMLException) {
            null
        } ?: return unreadable

        fun field(key: String) = fields[key]?.toString()?.takeIf { it.isNotBlank() }
        return FollowUp(
            file = file.name,
            id = field("id") ?: fileId,
            title = field("title"),
            type = field("type"),
            capability = field("capability"),
        )
    }

    /** The YAML between the opening `---` line and the next one, or null without frontmatter. */
    private fun frontmatter(text: String): String? {
        val lines = text.lines()
        if (lines.firstOrNull()?.trimEnd() != "---") return null
        val end = lines.drop(1).indexOfFirst { it.trimEnd() == "---" }
        if (end < 0) return null
        return lines.subList(1, end + 1).joinToString("\n")
    }

    private fun parseYaml(yaml: String): Map<*, *>? =
        Yaml(SafeConstructor(LoaderOptions())).load<Any?>(yaml) as? Map<*, *>
}
