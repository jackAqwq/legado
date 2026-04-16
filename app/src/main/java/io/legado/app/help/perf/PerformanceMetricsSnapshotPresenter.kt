package io.legado.app.help.perf

internal object PerformanceMetricsSnapshotPresenter {

    fun buildPreviewText(lines: List<String>, generatedAtMs: Long = System.currentTimeMillis()): String {
        return PerformanceMetricsExportFormatter.toText(
            lines = lines,
            generatedAtMs = generatedAtMs
        )
    }

    fun buildCopyText(lines: List<String>, generatedAtMs: Long = System.currentTimeMillis()): String {
        return buildPreviewText(lines, generatedAtMs)
    }
}
