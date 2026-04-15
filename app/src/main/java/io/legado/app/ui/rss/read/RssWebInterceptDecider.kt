package io.legado.app.ui.rss.read

import io.legado.app.utils.splitNotBlank

internal object RssWebInterceptDecider {

    fun shouldInterceptMainFrameRewrite(
        isForMainFrame: Boolean,
        hasPreloadJs: Boolean
    ): Boolean {
        return isForMainFrame && hasPreloadJs
    }

    fun shouldSkipMainFrameRewrite(
        url: String,
        method: String
    ): Boolean {
        return url.startsWith("data:text/html;") || method == "POST"
    }

    fun shouldInjectPreloadScript(
        jsInjected: Boolean,
        requestUrl: String,
        preloadScriptUrl: String
    ): Boolean {
        return !jsInjected && requestUrl == preloadScriptUrl
    }

    fun parseRuleList(rawRules: String?): List<String>? {
        return rawRules?.splitNotBlank(",")?.toList()
    }
}
