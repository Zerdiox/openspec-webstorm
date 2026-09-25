package dev.derwa.openspec

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

data class Change(val name: String, val completedTasks: Int, val totalTasks: Int, val status: String)

sealed interface ChangesResult {
    data class Loaded(val changes: List<Change>) : ChangesResult
    data class Unreadable(val reason: String) : ChangesResult
}

/** Maps `openspec list --json` output to changes; throws when it isn't that output. */
fun parseChanges(json: String): List<Change> =
    JsonParser.parseString(json).asJsonObject.getAsJsonArray("changes").map {
        val change = it.asJsonObject
        Change(
            name = change["name"].asString,
            completedTasks = change["completedTasks"].asInt,
            totalTasks = change["totalTasks"].asInt,
            status = change["status"]?.asString.orEmpty(),
        )
    }

/** Reads a project's changes by asking the `openspec` CLI, found on [environment]'s PATH. */
class ChangesSource(private val environment: Map<String, String>) {
    fun load(projectRoot: Path): ChangesResult {
        val openspec = findOnPath("openspec", environment)
            ?: return ChangesResult.Unreadable("openspec wasn't found in your shell's PATH")

        val process = ProcessBuilder(openspec.toString(), "list", "--json")
            .directory(projectRoot.toFile())
            .apply { environment().clear(); environment().putAll(environment) }
            .start()
        process.outputStream.close()
        val stdout = CompletableFuture.supplyAsync { process.inputStream.readAllBytes().toString(Charsets.UTF_8) }
        val stderr = CompletableFuture.supplyAsync { process.errorStream.readAllBytes().toString(Charsets.UTF_8) }
        if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            return ChangesResult.Unreadable("openspec list didn't answer within $TIMEOUT_SECONDS seconds")
        }

        if (process.exitValue() != 0) {
            val reason = stderr.get().lineSequence().firstOrNull { it.isNotBlank() }?.trim()
                ?: statusMessage(stdout.get())
                ?: "openspec list exited with code ${process.exitValue()}"
            return ChangesResult.Unreadable(reason)
        }
        return try {
            ChangesResult.Loaded(parseChanges(stdout.get()))
        } catch (e: RuntimeException) {
            ChangesResult.Unreadable("openspec list gave output that couldn't be read")
        }
    }

    /** openspec reports some failures as JSON on stdout: `{"status": [{"message": ...}]}`. */
    private fun statusMessage(stdout: String): String? = try {
        (JsonParser.parseString(stdout) as? JsonObject)
            ?.getAsJsonArray("status")
            ?.firstOrNull()?.asJsonObject?.get("message")?.asString
    } catch (e: RuntimeException) {
        null
    }

    private companion object {
        const val TIMEOUT_SECONDS = 30L
    }
}
