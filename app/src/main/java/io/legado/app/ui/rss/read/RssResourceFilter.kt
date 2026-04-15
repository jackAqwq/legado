package io.legado.app.ui.rss.read

import io.legado.app.utils.RegexMatcherCache

class RssResourceFilter(
    private val regexMatcherCache: RegexMatcherCache = RegexMatcherCache()
) {

    enum class Decision {
        BLOCK,
        ALLOW,
        PASS_THROUGH
    }

    fun decide(
        url: String,
        blacklist: List<String>?,
        whitelist: List<String>?,
        onInvalidBlacklistPattern: ((pattern: String, throwable: Throwable) -> Unit)? = null,
        onInvalidWhitelistPattern: ((pattern: String, throwable: Throwable) -> Unit)? = null
    ): Decision {
        if (!blacklist.isNullOrEmpty()) {
            blacklist.forEach { rule ->
                val regexMatched = regexMatcherCache.matches(url, rule) { pattern, throwable ->
                    onInvalidBlacklistPattern?.invoke(pattern, throwable)
                }
                if (url.startsWith(rule) || regexMatched) {
                    return Decision.BLOCK
                }
            }
            return Decision.PASS_THROUGH
        }
        if (!whitelist.isNullOrEmpty()) {
            whitelist.forEach { rule ->
                val regexMatched = regexMatcherCache.matches(url, rule) { pattern, throwable ->
                    onInvalidWhitelistPattern?.invoke(pattern, throwable)
                }
                if (url.startsWith(rule) || regexMatched) {
                    return Decision.ALLOW
                }
            }
            return Decision.BLOCK
        }
        return Decision.PASS_THROUGH
    }
}
