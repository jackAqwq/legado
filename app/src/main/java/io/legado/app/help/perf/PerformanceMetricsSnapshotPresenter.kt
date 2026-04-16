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
}
