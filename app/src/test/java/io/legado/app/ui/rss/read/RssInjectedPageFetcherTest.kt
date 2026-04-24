package io.legado.app.ui.rss.read

import io.legado.app.help.webView.RssInjectedPageFetcher
import io.legado.app.help.webView.RssInjectedPageHttpResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets

class RssInjectedPageFetcherTest {

    @Test
    fun fetch_should_pass_cookie_headers_and_inject_snippet() {
        var capturedMethod: String? = null
        var capturedCookie: String? = null
        var capturedHeaders: Map<String, String> = emptyMap()

        val outcome = RssInjectedPageFetcher.fetch(
            url = "https://example.com/article",
            method = "GET",
            requestHeaders = linkedMapOf("User-Agent" to "Legado", "Accept" to "text/html"),
            cookie = "sid=1",
            snippet = "<script src=\"injected.js\"></script>"
        ) { request ->
            capturedMethod = request.method
            capturedCookie = request.cookie
            capturedHeaders = request.requestHeaders
            RssInjectedPageHttpResponse(
                statusCode = 200,
                mimeType = "text/html",
                charset = StandardCharsets.UTF_8,
                contentType = "text/html; charset=utf-8",
                bodyText = "<html><head><title>T</title></head><body>ok</body></html>",
                setCookies = listOf("sid=2")
            )
        }

        assertEquals("GET", capturedMethod)
        assertEquals("sid=1", capturedCookie)
        assertEquals("Legado", capturedHeaders["User-Agent"])
        assertTrue(
            outcome.result!!.bodyText.contains("<head><script src=\"injected.js\"></script><title>")
        )
        assertEquals(200, outcome.result!!.statusCode)
        assertEquals("text/html; charset=utf-8", outcome.result!!.contentType)
        assertEquals(listOf("sid=2"), outcome.result!!.setCookies)
        assertNull(outcome.failureType)
    }

    @Test
    fun fetch_should_surface_failure_type_when_request_throws() {
        val outcome = RssInjectedPageFetcher.fetch(
            url = "https://example.com/article",
            method = "GET",
            requestHeaders = emptyMap(),
            cookie = null,
            snippet = "<script></script>"
        ) {
            throw IllegalStateException("network failed")
        }

        assertNull(outcome.result)
        assertEquals("IllegalStateException", outcome.failureType)
    }
}
