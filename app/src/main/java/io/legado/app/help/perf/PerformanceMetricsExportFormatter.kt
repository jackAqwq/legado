package io.legado.app.help.perf

internal object PerformanceMetricsExportFormatter {

    internal const val FILE_NAME = "performance_metrics.txt"
    private const val EMPTY_HINT = "No performance metrics recorded."

    fun toText(lines: List<String>, generatedAtMs: Long = System.currentTimeMillis()): String {
        val body = if (lines.isEmpty()) {
            EMPTY_HINT
        } else {
            lines.joinToString(separator = "\n")
        }
        return buildString {
            appendLine("# Performance Metrics Export")
            appendLine("generated_at_ms=$generatedAtMs")
            appendLine()
            append(body)
            appendLine()
        }
    }
}
