package io.legado.app.help.perf

internal data class PerformanceMetricsSummary(
    val count: Int,
    val avgDurationMs: Long,
    val p95DurationMs: Long
)

internal object PerformanceMetricsExportFormatter {

    internal const val FILE_NAME = "performance_metrics.txt"
    private const val EMPTY_HINT = "No performance metrics recorded."

    fun toText(
        lines: List<String>,
        generatedAtMs: Long = System.currentTimeMillis(),
        summary: PerformanceMetricsSummary? = null
    ): String {
        val body = if (lines.isEmpty()) {
            EMPTY_HINT
        } else {
            lines.joinToString(separator = "\n")
        }
        return buildString {
            appendLine("# Performance Metrics Export")
            appendLine("generated_at_ms=$generatedAtMs")
            summary?.let {
                appendLine()
                appendLine("summary.count=${it.count}")
                appendLine("summary.avg_duration_ms=${it.avgDurationMs}")
                appendLine("summary.p95_duration_ms=${it.p95DurationMs}")
            }
            appendLine()
            append(body)
            appendLine()
        }
    }
}
