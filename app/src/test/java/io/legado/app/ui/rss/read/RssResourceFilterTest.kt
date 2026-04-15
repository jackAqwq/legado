package io.legado.app.ui.rss.read

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RssResourceFilterTest {

    private val filter = RssResourceFilter()

    @Test
    fun blacklist_match_returns_block() {
        val decision = filter.decide(
            url = "https://cdn.example.com/ad.js",
            blacklist = listOf("https://cdn\\.example\\.com/.*"),
            whitelist = null
        )
        assertEquals(RssResourceFilter.Decision.BLOCK, decision)
    }

    @Test
    fun whitelist_no_match_returns_block() {
        val decision = filter.decide(
            url = "https://cdn.example.com/script.js",
            blacklist = null,
            whitelist = listOf("https://safe\\.example\\.com/.*")
        )
        assertEquals(RssResourceFilter.Decision.BLOCK, decision)
    }

    @Test
    fun whitelist_match_returns_allow() {
        val decision = filter.decide(
            url = "https://safe.example.com/script.js",
            blacklist = null,
            whitelist = listOf("https://safe\\.example\\.com/.*")
        )
        assertEquals(RssResourceFilter.Decision.ALLOW, decision)
    }

    @Test
    fun invalid_pattern_callback_is_invoked_once() {
        var callbackCount = 0
        repeat(3) {
            filter.decide(
                url = "https://safe.example.com/script.js",
                blacklist = listOf("["),
                whitelist = null,
                onInvalidBlacklistPattern = { _, _ -> callbackCount++ }
            )
        }
        assertTrue(callbackCount == 1)
    }
}
