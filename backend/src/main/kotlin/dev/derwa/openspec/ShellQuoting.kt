package dev.derwa.openspec

/** POSIX single-quotes [text] so a shell passes it on literally, whatever it contains. */
fun shellQuote(text: String): String = "'" + text.replace("'", "'\\''") + "'"

/** The shell line that starts Claude Code with [command] as its first prompt. */
fun claudeCommandLine(command: String): String = "claude ${shellQuote(command)}"
