package io.legado.app.help.source

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookSourceUrlMatcherTest {

    @Test
    fun matches_valid_pattern() {
        val matcher = BookSourceUrlMatcher()
        assertTrue(
            matcher.matches(
                "https://example.com/book/123",
                "https://example\\.com/book/\\d+"
            )
        )
    }

    @Test
    fun invalid_or_blank_pattern_returns_false() {
        val matcher = BookSourceUrlMatcher()
        assertFalse(matcher.matches("https://example.com/book/123", ""))
        assertFalse(matcher.matches("https://example.com/book/123", "["))
        assertFalse(matcher.matches("https://example.com/book/123", "["))
        assertEquals(1, matcher.invalidPatternCount())
    }

    @Test
    fun compiled_pattern_is_cached() {
        val matcher = BookSourceUrlMatcher()
        repeat(3) {
            matcher.matches(
                "https://example.com/book/123",
                "https://example\\.com/book/\\d+"
            )
        }
        assertEquals(1, matcher.cachedPatternCount())
        assertEquals(0, matcher.invalidPatternCount())
    }

    @Test
    fun trims_pattern_before_match() {
        val matcher = BookSourceUrlMatcher()
        assertTrue(
            matcher.matches(
                "https://example.com/book/123",
                "  https://example\\.com/book/\\d+  "
            )
        )
    }
}
