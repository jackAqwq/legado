package io.legado.app.help.source

import io.legado.app.utils.RegexMatcherCache

class BookSourceUrlMatcher {

    private val regexMatcherCache = RegexMatcherCache()

    fun matches(url: String, pattern: String?): Boolean {
        return regexMatcherCache.matches(url, pattern)
    }

    internal fun cachedPatternCount(): Int = regexMatcherCache.cachedPatternCount()

    internal fun invalidPatternCount(): Int = regexMatcherCache.invalidPatternCount()
}
