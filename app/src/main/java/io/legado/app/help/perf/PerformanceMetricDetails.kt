package io.legado.app.help.perf

internal data class PerformanceMetricDetails(
    private val entries: LinkedHashMap<String, String> = linkedMapOf()
) {

    operator fun get(key: String): String? = entries[key]

    fun isEmpty(): Boolean = entries.isEmpty()

    fun encode(): String {
        return entries.entries.joinToString(separator = ",") { (key, value) ->
            "$key=$value"
        }
    }

    companion object {
        fun empty(): PerformanceMetricDetails = PerformanceMetricDetails()

        fun of(vararg entries: Pair<String, String?>): PerformanceMetricDetails {
            val map = linkedMapOf<String, String>()
            entries.forEach { (key, value) ->
                if (key.isBlank() || value.isNullOrBlank()) {
                    return@forEach
                }
                map[key] = value
            }
            return PerformanceMetricDetails(map)
        }

        fun parse(encoded: String): PerformanceMetricDetails {
            if (encoded.isBlank()) {
                return empty()
            }
            val map = linkedMapOf<String, String>()
            encoded.split(',').forEach { segment ->
                val trimmed = segment.trim()
                if (trimmed.isEmpty()) {
                    return@forEach
                }
                val separatorIndex = trimmed.indexOf('=')
                if (separatorIndex <= 0 || separatorIndex == trimmed.lastIndex) {
                    return@forEach
                }
                val key = trimmed.substring(0, separatorIndex)
                val value = trimmed.substring(separatorIndex + 1)
                if (key.isBlank() || value.isBlank()) {
                    return@forEach
                }
                map[key] = value
            }
            return PerformanceMetricDetails(map)
        }
    }
}
