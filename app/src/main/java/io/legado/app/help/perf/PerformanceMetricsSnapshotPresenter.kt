package io.legado.app.help.perf

internal object PerformanceMetricsSnapshotPresenter {

    fun buildPreviewText(
        lines: List<String>,
        generatedAtMs: Long = System.currentTimeMillis(),
        summary: PerformanceMetricsSummary? = null
    ): String {
        return PerformanceMetricsExportFormatter.toText(
            lines = lines,
            generatedAtMs = generatedAtMs,
            summary = summary
        )
    }

    fun buildCopyText(
        lines: List<String>,
        generatedAtMs: Long = System.currentTimeMillis(),
        summary: PerformanceMetricsSummary? = null
    ): String {
        return buildPreviewText(lines, generatedAtMs, summary)
    }

    fun buildSourceSummaryText(
        summaries: List<PerformanceMetricsSourceSummary>,
        generatedAtMs: Long = System.currentTimeMillis()
    ): String {
        val lines = summaries.map { summary ->
            "source=${summary.source}|count=${summary.count}|avg=${summary.avgDurationMs}ms|p95=${summary.p95DurationMs}ms"
        }
        return buildPreviewText(lines = lines, generatedAtMs = generatedAtMs)
    }

    fun buildResultSummaryText(
        summaries: List<PerformanceMetricsResultSummary>,
        generatedAtMs: Long = System.currentTimeMillis()
    ): String {
        val lines = summaries.map { summary ->
            "result=${summary.result}|count=${summary.count}|avg=${summary.avgDurationMs}ms|p95=${summary.p95DurationMs}ms"
        }
        return buildPreviewText(lines = lines, generatedAtMs = generatedAtMs)
    }

    fun buildSourceResultSummaryText(
        summaries: List<PerformanceMetricsSourceResultSummary>,
        generatedAtMs: Long = System.currentTimeMillis()
    ): String {
        val lines = summaries.map { summary ->
            "source=${summary.source}|result=${summary.result}|count=${summary.count}|avg=${summary.avgDurationMs}ms|p95=${summary.p95DurationMs}ms"
        }
        return buildPreviewText(lines = lines, generatedAtMs = generatedAtMs)
    }
}
