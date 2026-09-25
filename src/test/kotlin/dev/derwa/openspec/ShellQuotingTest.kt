package dev.derwa.openspec

import org.junit.Assert.assertEquals
import org.junit.Test

class ShellQuotingTest {
    /** Runs `printf %s <quoted>` in a real POSIX shell and returns what the shell passed on. */
    private fun throughShell(text: String): String {
        val process = ProcessBuilder("sh", "-c", "printf %s ${shellQuote(text)}")
            .redirectErrorStream(true)
            .start()
        val out = process.inputStream.readAllBytes().toString(Charsets.UTF_8)
        process.waitFor()
        return out
    }

    private fun assertArrivesIntact(text: String) = assertEquals(text, throughShell(text))

    @Test
    fun `plain text`() = assertArrivesIntact("board paging")

    @Test
    fun `single and double quotes`() = assertArrivesIntact("Don't show \"cost\" when it's 0")

    @Test
    fun `dollar signs are not expanded`() = assertArrivesIntact("Don't show \$cost when it's 0, nor \$(whoami) or \${HOME}")

    @Test
    fun `backticks are not executed`() = assertArrivesIntact("run `whoami` here")

    @Test
    fun `newlines are kept`() = assertArrivesIntact("line one\nline two\n\nline four")

    @Test
    fun `backslashes and globs are literal`() = assertArrivesIntact("a\\nb * ? [x] ~ ; | & > <")

    @Test
    fun `empty text`() = assertArrivesIntact("")

    @Test
    fun `claude invocation quotes the command`() {
        assertEquals("claude '/openspec-propose Don'\\''t'", claudeCommandLine("/openspec-propose Don't"))
    }
}
