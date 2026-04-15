package io.legado.app.utils

import java.util.concurrent.ConcurrentHashMap

class RegexMatcherCache {

    private val regexCache = ConcurrentHashMap<String, Regex>()
    private val invalidPatternCache = ConcurrentHashMap.newKeySet<String>()

    fun matches(input: CharSequence, pattern: String?): Boolean {
        return matches(input, pattern, null)
    }

    fun matches(
        input: CharSequence,
        pattern: String?,
        onInvalidPattern: ((pattern: String, throwable: Throwable) -> Unit)?
    ): Boolean {
        val normalizedPattern = pattern?.trim()
        if (normalizedPattern.isNullOrEmpty()) {
            return false
        }
        if (invalidPatternCache.contains(normalizedPattern)) {
            return false
        }
        val regex = regexCache[normalizedPattern]
            ?: compileRegex(normalizedPattern, onInvalidPattern)
            ?: return false
        return runCatching { regex.matches(input) }.getOrDefault(false)
    }

    private fun compileRegex(
        pattern: String,
        onInvalidPattern: ((pattern: String, throwable: Throwable) -> Unit)?
    ): Regex? {
        return runCatching { pattern.toRegex() }
            .onFailure { throwable ->
                if (invalidPatternCache.add(pattern)) {
                    onInvalidPattern?.invoke(pattern, throwable)
                }
            }
            .getOrNull()
            ?.also { compiled ->
                regexCache.putIfAbsent(pattern, compiled)
            }
            ?.let { regexCache[pattern] ?: it }
    }

    internal fun cachedPatternCount(): Int = regexCache.size

    internal fun invalidPatternCount(): Int = invalidPatternCache.size
}
