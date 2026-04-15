package io.legado.app.ui.rss.read

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RssWebInterceptDeciderTest {

    @Test
    fun intercept_mainframe_rewrite_requires_mainframe_and_preload_enabled() {
        assertTrue(
            RssWebInterceptDecider.shouldInterceptMainFrameRewrite(
                isForMainFrame = true,
                hasPreloadJs = true
            )
        )
        assertFalse(
            RssWebInterceptDecider.shouldInterceptMainFrameRewrite(
                isForMainFrame = false,
                hasPreloadJs = true
            )
        )
        assertFalse(
            RssWebInterceptDecider.shouldInterceptMainFrameRewrite(
                isForMainFrame = true,
                hasPreloadJs = false
            )
        )
    }

    @Test
    fun skip_mainframe_rewrite_for_data_html_or_post_request() {
        assertTrue(
            RssWebInterceptDecider.shouldSkipMainFrameRewrite(
                url = "data:text/html;charset=utf-8,xxx",
                method = "GET"
            )
        )
        assertTrue(
            RssWebInterceptDecider.shouldSkipMainFrameRewrite(
                url = "https://example.com/index.html",
                method = "POST"
            )
        )
        assertFalse(
            RssWebInterceptDecider.shouldSkipMainFrameRewrite(
                url = "https://example.com/index.html",
                method = "GET"
            )
        )
    }

    @Test
    fun preload_script_injection_requires_not_injected_and_matching_url() {
        assertTrue(
            RssWebInterceptDecider.shouldInjectPreloadScript(
                jsInjected = false,
                requestUrl = "https://legado.local/preload.js",
                preloadScriptUrl = "https://legado.local/preload.js"
            )
        )
        assertFalse(
            RssWebInterceptDecider.shouldInjectPreloadScript(
                jsInjected = true,
                requestUrl = "https://legado.local/preload.js",
                preloadScriptUrl = "https://legado.local/preload.js"
            )
        )
        assertFalse(
            RssWebInterceptDecider.shouldInjectPreloadScript(
                jsInjected = false,
                requestUrl = "https://legado.local/other.js",
                preloadScriptUrl = "https://legado.local/preload.js"
            )
        )
    }
}
