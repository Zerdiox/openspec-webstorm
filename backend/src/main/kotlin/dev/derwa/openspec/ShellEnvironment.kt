package dev.derwa.openspec

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.EnvironmentUtil
import com.intellij.util.ShellEnvironmentReader

private val LOG = logger<ShellEnvironment>()
private const val TIMEOUT_MS = 20_000L

/**
 * The environment of [shell] started as an interactive login shell from [baseEnvironment], the way a
 * terminal tab starts it, or null when it can't be read.
 */
internal fun readShellEnvironment(shell: String, baseEnvironment: Map<String, String>): Map<String, String>? = try {
    val command = ShellEnvironmentReader.shellCommand(shell, null, true, emptyList())
    command.environment().apply { clear(); putAll(baseEnvironment) }
    ShellEnvironmentReader.readEnvironment(command, TIMEOUT_MS).first
} catch (e: Exception) {
    LOG.info("Couldn't read the environment of $shell", e)
    null
}

/** The login shell whose environment a terminal tab gets, or null on Windows, which has none to read. */
internal fun userShell(environment: Map<String, String>, windows: Boolean = SystemInfo.isWindows): String? =
    if (windows) null else environment["SHELL"]?.takeIf { it.isNotBlank() } ?: "/bin/sh"

/** The user's shell environment, read once per IDE session; the IDE's own environment if that fails. */
internal object ShellEnvironment {
    val map: Map<String, String> by lazy {
        val base = System.getenv()
        userShell(base)?.let { readShellEnvironment(it, base) } ?: EnvironmentUtil.getEnvironmentMap()
    }
}
