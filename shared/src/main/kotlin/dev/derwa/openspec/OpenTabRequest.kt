package dev.derwa.openspec

import kotlinx.serialization.Serializable

/** Asks the client to open a terminal tab named [tabName] in [workingDirectory] and run [commandLine] in it. */
@Serializable
data class OpenTabRequest(val tabName: String, val workingDirectory: String?, val commandLine: String)
