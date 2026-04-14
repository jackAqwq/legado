package io.legado.app.help.source

import java.util.concurrent.ConcurrentHashMap

class BookSourceUrlMatcher {

    private val regexCache = ConcurrentHashMap<String, Regex>()
    private val invalidPatternCache = ConcurrentHashMap.newKeySet<String>()

    fun matches(url: String, pattern: String?): Boolean {
        val normalizedPattern = pattern?.trim()
        if (normalizedPattern.isNullOrEmpty()) {
            return false
        }
        if (invalidPatternCache.contains(normalizedPattern)) {
            return false
        }
        val regex = regexCache[normalizedPattern] ?: compileRegex(normalizedPattern) ?: return false
        return runCatching { regex.matches(url) }.getOrDefault(false)
    }

    private fun compileRegex(pattern: String): Regex? {
        return runCatching { pattern.toRegex() }
            .onFailure { invalidPatternCache.add(pattern) }
            .getOrNull()
            ?.also { compiled ->
                regexCache.putIfAbsent(pattern, compiled)
            }
            ?.let { regexCache[pattern] ?: it }
    }

    internal fun cachedPatternCount(): Int = regexCache.size

    internal fun invalidPatternCount(): Int = invalidPatternCache.size
}
