package io.legado.app.ui.rss.read

import org.junit.Assert.assertEquals
import org.junit.Test

class RssHtmlHeadInjectorTest {

    @Test
    fun insert_snippet_right_after_head_open_tag() {
        val html = "<html><head lang=\"en\"><title>T</title></head><body>ok</body></html>"
        val snippet = "<script src=\"injected.js\"></script>"

        val rewritten = RssHtmlHeadInjector.insertAfterHeadOpenTag(
            html = html,
            snippet = snippet
        )

        assertEquals(
            "<html><head lang=\"en\">$snippet<title>T</title></head><body>ok</body></html>",
            rewritten
        )
    }

    @Test
    fun keep_original_when_head_tag_not_found() {
        val html = "<html><body>no head</body></html>"

        val rewritten = RssHtmlHeadInjector.insertAfterHeadOpenTag(
            html = html,
            snippet = "<script src=\"injected.js\"></script>"
        )

        assertEquals(html, rewritten)
    }

    @Test
    fun keep_original_when_head_tag_not_closed() {
        val html = "<html><head attr=\"x\""

        val rewritten = RssHtmlHeadInjector.insertAfterHeadOpenTag(
            html = html,
            snippet = "<script src=\"injected.js\"></script>"
        )

        assertEquals(html, rewritten)
    }
}
