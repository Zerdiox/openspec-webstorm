package dev.derwa.openspec

import com.intellij.openapi.util.SystemInfo
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/** Windows' own list when the environment has none; its order is the order Windows tries them in. */
private const val DEFAULT_PATHEXT = ".COM;.EXE;.BAT;.CMD"

/**
 * The executable [name] on the PATH of [environment], or null when it isn't there. On Windows only
 * names with a PATHEXT extension count: npm puts an extensionless Unix script beside each `.cmd`
 * launcher, and Windows can't start it.
 */
internal fun findOnPath(name: String, environment: Map<String, String>, windows: Boolean = SystemInfo.isWindows): Path? {
    val candidates = if (windows) {
        (environment["PATHEXT"] ?: DEFAULT_PATHEXT).split(';').filter { it.isNotBlank() }.map { name + it.lowercase() }
    } else {
        listOf(name)
    }
    return environment["PATH"].orEmpty()
        .split(File.pathSeparator)
        .filter { it.isNotBlank() }
        .flatMap { dir -> candidates.map { Path.of(dir, it) } }
        .firstOrNull { Files.isRegularFile(it) && (windows || Files.isExecutable(it)) }
}
