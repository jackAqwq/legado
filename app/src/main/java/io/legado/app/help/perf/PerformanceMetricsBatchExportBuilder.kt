package io.legado.app.help.perf

internal data class PerformanceMetricsExportEntry(
    val fileName: String,
    val text: String
)

internal object PerformanceMetricsBatchExportBuilder {

    const val DIR_NAME = "performance_metrics"
    private const val FILE_ALL = "performance_metrics_all.txt"
    private const val FILE_STARTUP = "performance_metrics_startup.txt"
    private const val FILE_READ = "performance_metrics_read.txt"
    private const val FILE_RSS = "performance_metrics_rss.txt"

    fun buildEntries(
        allLines: List<String>,
        startupLines: List<String>,
        readLines: List<String>,
        rssLines: List<String>,
        generatedAtMs: Long = System.currentTimeMillis()
    ): List<PerformanceMetricsExportEntry> {
        return listOf(
            PerformanceMetricsExportEntry(
                fileName = FILE_ALL,
                text = PerformanceMetricsExportFormatter.toText(
                    lines = allLines,
                    generatedAtMs = generatedAtMs
                )
            ),
            PerformanceMetricsExportEntry(
                fileName = FILE_STARTUP,
                text = PerformanceMetricsExportFormatter.toText(
                    lines = startupLines,
                    generatedAtMs = generatedAtMs
                )
            ),
            PerformanceMetricsExportEntry(
                fileName = FILE_READ,
                text = PerformanceMetricsExportFormatter.toText(
                    lines = readLines,
                    generatedAtMs = generatedAtMs
                )
            ),
            PerformanceMetricsExportEntry(
                fileName = FILE_RSS,
                text = PerformanceMetricsExportFormatter.toText(
                    lines = rssLines,
                    generatedAtMs = generatedAtMs
                )
            )
        )
    }
}
