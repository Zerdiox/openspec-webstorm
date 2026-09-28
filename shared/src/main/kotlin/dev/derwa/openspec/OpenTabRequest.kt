package dev.derwa.openspec

import kotlinx.serialization.Serializable

/** Asks the client to open a terminal tab named [tabName] in [workingDirectory] and run [command] in it. */
@Serializable
data class OpenTabRequest(val tabName: String, val workingDirectory: String?, val command: TabCommand)

/** What a new terminal tab runs. */
@Serializable
sealed interface TabCommand

/** Start the user's shell and type [line] into it, as if they had. */
@Serializable
data class TypeIntoShell(val line: String) : TabCommand

/** Start [command]'s first element as the tab's process, with the rest as its arguments; no shell reads them. */
@Serializable
data class StartProgram(val command: List<String>) : TabCommand
