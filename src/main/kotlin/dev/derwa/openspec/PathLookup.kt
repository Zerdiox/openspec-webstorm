package dev.derwa.openspec

import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/** The executable [name] on the PATH of [environment], or null when it isn't there. */
fun findOnPath(name: String, environment: Map<String, String>): Path? =
    environment["PATH"].orEmpty()
        .split(File.pathSeparator)
        .filter { it.isNotBlank() }
        .map { Path.of(it, name) }
        .firstOrNull { Files.isRegularFile(it) && Files.isExecutable(it) }
