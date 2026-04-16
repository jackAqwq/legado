package io.legado.app.help.perf

import kotlin.math.ceil

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
                    generatedAtMs = generatedAtMs,
                    summary = buildSummary(allLines)
                )
            ),
            PerformanceMetricsExportEntry(
                fileName = FILE_STARTUP,
                text = PerformanceMetricsExportFormatter.toText(
                    lines = startupLines,
                    generatedAtMs = generatedAtMs,
                    summary = buildSummary(startupLines)
                )
            ),
            PerformanceMetricsExportEntry(
                fileName = FILE_READ,
                text = PerformanceMetricsExportFormatter.toText(
                    lines = readLines,
                    generatedAtMs = generatedAtMs,
                    summary = buildSummary(readLines)
                )
            ),
            PerformanceMetricsExportEntry(
                fileName = FILE_RSS,
                text = PerformanceMetricsExportFormatter.toText(
                    lines = rssLines,
                    generatedAtMs = generatedAtMs,
                    summary = buildSummary(rssLines)
                )
            )
        )
    }

    private fun buildSummary(lines: List<String>): PerformanceMetricsSummary {
        val durations = lines.mapNotNull(::parseDurationMs)
        if (durations.isEmpty()) {
            return PerformanceMetricsSummary(
                count = 0,
                avgDurationMs = 0L,
                p95DurationMs = 0L
            )
        }
        val sorted = durations.sorted()
        val p95Index = (ceil(sorted.size * 0.95).toInt() - 1).coerceIn(0, sorted.lastIndex)
        return PerformanceMetricsSummary(
            count = durations.size,
            avgDurationMs = durations.sum() / durations.size,
            p95DurationMs = sorted[p95Index]
        )
    }

    private fun parseDurationMs(line: String): Long? {
        val firstSep = line.indexOf('|')
        if (firstSep < 0) return null
        val secondSep = line.indexOf('|', firstSep + 1)
        if (secondSep < 0) return null
        val thirdSep = line.indexOf('|', secondSep + 1)
        if (thirdSep < 0) return null
        return line.substring(secondSep + 1, thirdSep).removeSuffix("ms").toLongOrNull()
    }
}
