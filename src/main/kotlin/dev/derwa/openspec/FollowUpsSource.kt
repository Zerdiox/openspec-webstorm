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

data class FollowUp(
    val file: String,
    val id: String? = null,
    val title: String? = null,
    val type: String? = null,
    val capability: String? = null,
    val unreadable: Boolean = false,
)

/** Reads a project's open follow-ups from the frontmatter of its backlog files. */
object FollowUpsSource {
    private val FILE_ID = Regex("^FU-\\d+")

    /** The open follow-ups, or null when the project has no follow-ups backlog. */
    fun load(projectRoot: Path): List<FollowUp>? {
        val folder = projectRoot.resolve("openspec/backlog/followup")
        if (!folder.isDirectory()) return null
        return folder.listDirectoryEntries("FU-*.md")
            .filter { it.isRegularFile() }
            .sortedBy { it.name }
            .map(::read)
    }

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
