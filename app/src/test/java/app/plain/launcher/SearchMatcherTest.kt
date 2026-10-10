package app.plain.launcher

import app.plain.launcher.search.SearchCandidate
import app.plain.launcher.search.SearchMatcher
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchMatcherTest {
    @Test
    fun emptyQueryReturnsNothing() {
        assertEquals(emptyList<SearchCandidate>(), SearchMatcher.match("", listOf(SearchCandidate("a", "Alpha"))))
    }

    @Test
    fun prefixBeatsSubstring() {
        val results = SearchMatcher.match(
            "cal",
            listOf(
                SearchCandidate("1", "Local Calendar"),
                SearchCandidate("2", "Calculator"),
            ),
        )
        assertEquals("Calculator", results.first().label)
    }
}
