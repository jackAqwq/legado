package io.legado.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegexMatcherCacheTest {

    @Test
    fun matches_valid_pattern() {
        val matcher = RegexMatcherCache()
        assertTrue(
            matcher.matches(
                "https://example.com/book/123",
                "https://example\\.com/book/\\d+"
            )
        )
    }

    @Test
    fun invalid_pattern_callback_only_runs_once() {
        val matcher = RegexMatcherCache()
        var callbackCount = 0
        repeat(3) {
            assertFalse(
                matcher.matches("https://example.com/book/123", "[") { _, _ ->
                    callbackCount++
                }
            )
        }
        assertEquals(1, callbackCount)
        assertEquals(1, matcher.invalidPatternCount())
    }

    @Test
    fun compiled_pattern_is_cached() {
        val matcher = RegexMatcherCache()
        repeat(3) {
            matcher.matches(
                "https://example.com/book/123",
                "https://example\\.com/book/\\d+"
            )
        }
        assertEquals(1, matcher.cachedPatternCount())
        assertEquals(0, matcher.invalidPatternCount())
    }
}
