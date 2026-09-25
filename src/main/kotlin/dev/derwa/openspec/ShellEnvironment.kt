package dev.derwa.openspec

import com.intellij.openapi.diagnostic.logger
import com.intellij.util.EnvironmentUtil
import com.intellij.util.ShellEnvironmentReader

private val LOG = logger<ShellEnvironment>()
private const val TIMEOUT_MS = 20_000L

/**
 * The environment of [shell] started as an interactive login shell from [baseEnvironment], the way a
 * terminal tab starts it, or null when it can't be read.
 */
fun readShellEnvironment(shell: String, baseEnvironment: Map<String, String>): Map<String, String>? = try {
    val command = ShellEnvironmentReader.shellCommand(shell, null, true, emptyList())
    command.environment().apply { clear(); putAll(baseEnvironment) }
    ShellEnvironmentReader.readEnvironment(command, TIMEOUT_MS).first
} catch (e: Exception) {
    LOG.info("Couldn't read the environment of $shell", e)
    null
}

/** The user's shell environment, read once per IDE session; the IDE's own environment if that fails. */
object ShellEnvironment {
    val map: Map<String, String> by lazy {
        val base = System.getenv()
        readShellEnvironment(base["SHELL"]?.takeIf { it.isNotBlank() } ?: "/bin/sh", base)
            ?: EnvironmentUtil.getEnvironmentMap()
    }
}
