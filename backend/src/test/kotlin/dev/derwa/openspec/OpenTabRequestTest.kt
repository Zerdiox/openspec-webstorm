package dev.derwa.openspec

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenTabRequestTest {
    private fun assertRoundTrips(request: OpenTabRequest) {
        val serializer = OpenTabRequest.serializer()
        assertEquals(request, Json.decodeFromString(serializer, Json.encodeToString(serializer, request)))
    }

    @Test
    fun `a plain request`() =
        assertRoundTrips(OpenTabRequest("apply: board-paging", "/home/me/project", "claude '/openspec-apply-change board-paging'"))

    @Test
    fun `quotes, dollar signs and newlines in the command line`() =
        assertRoundTrips(
            OpenTabRequest(
                "explore: Don't show",
                "/home/me/project",
                "claude '/openspec-explore Don'\\''t show \"\$cost\" or \$(whoami)\nwhen it'\\''s 0'",
            ),
        )

    @Test
    fun `a project without a base path`() = assertRoundTrips(OpenTabRequest("promote: FU-0001", null, "claude '/backlog promote FU-0001'"))
}
