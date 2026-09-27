package dev.derwa.openspec

import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.error.MarkedYAMLException
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
    val problem: Problem? = null,
) {
    val unreadable: Boolean get() = problem != null
}

/** Why a follow-up can't be read: a short [reason], the 1-based [line] of the file to open at, and the parser's own [message]. */
internal data class Problem(val reason: String, val line: Int, val message: String? = null)

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
        val fields = when (val result = fields(file.readText())) {
            is Fields.Read -> result.fields
            is Fields.Unreadable -> return FollowUp(file = file.name, id = fileId, problem = result.problem)
        }

        fun field(key: String) = fields[key]?.toString()?.takeIf { it.isNotBlank() }
        return FollowUp(
            file = file.name,
            id = field("id") ?: fileId,
            title = field("title"),
            type = field("type"),
            capability = field("capability"),
        )
    }

    private sealed interface Fields {
        data class Read(val fields: Map<*, *>) : Fields
        data class Unreadable(val problem: Problem) : Fields
    }

    /** The fields of the YAML between the opening `---` line and the next one. */
    private fun fields(text: String): Fields {
        val lines = text.lines()
        if (lines.firstOrNull()?.trimEnd() != "---") return Fields.Unreadable(Problem("no frontmatter", 1))
        val end = lines.drop(1).indexOfFirst { it.trimEnd() == "---" }
        if (end < 0) return Fields.Unreadable(Problem("frontmatter isn't closed", 1))
        // The closing `---` is file line end + 2; a problem is never placed past it.
        val closingLine = end + 2
        val parsed = try {
            parseYaml(lines.subList(1, end + 1).joinToString("\n"))
        } catch (e: YAMLException) {
            val mark = (e as? MarkedYAMLException)?.problemMark
            // The parser counts from 0 within the frontmatter, which starts on the file's second line.
            val line = mark?.let { minOf(it.line + 2, closingLine) }
            return Fields.Unreadable(
                Problem(
                    reason = if (line != null) "YAML error on line $line" else "YAML error",
                    line = line ?: 2,
                    message = (e as? MarkedYAMLException)?.problem,
                ),
            )
        }
        return (parsed as? Map<*, *>)?.let(Fields::Read)
            ?: Fields.Unreadable(Problem("frontmatter isn't a list of fields", 2))
    }

    private fun parseYaml(yaml: String): Any? = Yaml(SafeConstructor(LoaderOptions())).load<Any?>(yaml)
}
