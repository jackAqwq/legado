package io.legado.app.help.perf

import io.legado.app.constant.AppLog
import io.legado.app.help.config.AppConfig
import kotlin.math.max
import kotlin.math.ceil

internal data class PerformanceMetricRecord(
    val timestampMs: Long,
    val name: String,
    val durationMs: Long,
    val details: String
)

internal data class PerformanceMetricsGroupedLines(
    val all: List<String>,
    val startup: List<String>,
    val read: List<String>,
    val rss: List<String>
)

internal object PerformanceMetricsTracker {

    internal const val MAX_RECORDS = 200

    @Volatile
    internal var enabledProvider: () -> Boolean = { AppConfig.recordPerformanceMetrics }

    @Volatile
    internal var logSink: (String) -> Unit = { AppLog.putDebug(it) }

    private val lock = Any()
    private val records = ArrayDeque<PerformanceMetricRecord>(MAX_RECORDS)
    private var appOnCreateStartUptimeMs: Long? = null
    private var pageFlipStartUptimeMs: Long? = null

    fun markAppOnCreateStart(uptimeMs: Long) {
        if (!enabledProvider()) return
        synchronized(lock) {
            appOnCreateStartUptimeMs = uptimeMs
        }
    }

    fun markMainUiReady(uptimeMs: Long) {
        if (!enabledProvider()) return
        val startUptimeMs = synchronized(lock) {
            appOnCreateStartUptimeMs.also {
                appOnCreateStartUptimeMs = null
            }
        } ?: return
        record(
            name = "startup.main_ui_ready",
            durationMs = uptimeMs - startUptimeMs,
            details = "stage=main_activity"
        )
    }

    fun markPageFlipGestureStart(uptimeMs: Long) {
        if (!enabledProvider()) return
        synchronized(lock) {
            pageFlipStartUptimeMs = uptimeMs
        }
    }

    fun markPageFlipCompleted(uptimeMs: Long, result: String = "success") {
        if (!enabledProvider()) return
        val startUptimeMs = synchronized(lock) {
            pageFlipStartUptimeMs.also {
                pageFlipStartUptimeMs = null
            }
        } ?: return
        record(
            name = "read.page_flip",
            durationMs = uptimeMs - startUptimeMs,
            details = "result=$result"
        )
    }

    fun recordRssInterceptDuration(durationMs: Long, source: String, success: Boolean) {
        if (!enabledProvider()) return
        record(
            name = "rss.intercept",
            durationMs = durationMs,
            details = "source=$source,result=${if (success) "success" else "failure"}"
        )
    }

    fun snapshot(): List<PerformanceMetricRecord> = synchronized(lock) {
        records.toList()
    }

    fun clearMetrics() {
        synchronized(lock) {
            records.clear()
            appOnCreateStartUptimeMs = null
            pageFlipStartUptimeMs = null
        }
    }

    fun exportLines(namePrefix: String? = null, limit: Int? = null): List<String> {
        val filteredRecords = selectRecords(
            source = snapshot(),
            namePrefix = namePrefix,
            limit = limit
        )
        return filteredRecords.map(::toExportLine)
    }

    fun exportSlowLines(limit: Int = 20, namePrefix: String? = null): List<String> {
        if (limit <= 0) return emptyList()
        return selectRecords(
            source = snapshot(),
            namePrefix = namePrefix,
            limit = null
        )
            .sortedByDescending { it.durationMs }
            .take(limit)
            .map(::toExportLine)
    }

    fun buildSummary(namePrefix: String? = null, limit: Int? = null): PerformanceMetricsSummary {
        val selected = selectRecords(
            source = snapshot(),
            namePrefix = namePrefix,
            limit = limit
        )
        if (selected.isEmpty()) {
            return PerformanceMetricsSummary(
                count = 0,
                avgDurationMs = 0L,
                p95DurationMs = 0L
            )
        }
        val sortedDurations = selected.map { it.durationMs }.sorted()
        val p95Index = (ceil(sortedDurations.size * 0.95).toInt() - 1)
            .coerceIn(0, sortedDurations.lastIndex)
        return PerformanceMetricsSummary(
            count = sortedDurations.size,
            avgDurationMs = sortedDurations.sum() / sortedDurations.size,
            p95DurationMs = sortedDurations[p95Index]
        )
    }

    fun exportGroupedLines(limit: Int? = null): PerformanceMetricsGroupedLines {
        val source = snapshot()
        val all = ArrayList<String>(source.size)
        val startup = ArrayList<String>()
        val read = ArrayList<String>()
        val rss = ArrayList<String>()
        source.forEach { record ->
            val line = toExportLine(record)
            all.add(line)
            when {
                record.name.startsWith("startup.") -> startup.add(line)
                record.name.startsWith("read.") -> read.add(line)
                record.name.startsWith("rss.") -> rss.add(line)
            }
        }
        return PerformanceMetricsGroupedLines(
            all = withLimit(all, limit),
            startup = withLimit(startup, limit),
            read = withLimit(read, limit),
            rss = withLimit(rss, limit)
        )
    }

    private fun selectRecords(
        source: List<PerformanceMetricRecord>,
        namePrefix: String?,
        limit: Int?
    ): List<PerformanceMetricRecord> {
        var selected = source
        if (!namePrefix.isNullOrBlank()) {
            selected = selected.filter { it.name.startsWith(namePrefix) }
        }
        if (limit != null && limit > 0 && selected.size > limit) {
            selected = selected.takeLast(limit)
        }
        return selected
    }

    internal fun resetForTest() {
        synchronized(lock) {
            records.clear()
            appOnCreateStartUptimeMs = null
            pageFlipStartUptimeMs = null
        }
        enabledProvider = { AppConfig.recordPerformanceMetrics }
        logSink = { AppLog.putDebug(it) }
    }

    private fun toExportLine(record: PerformanceMetricRecord): String {
        return "${record.timestampMs}|${record.name}|${record.durationMs}ms|${record.details}"
    }

    private fun withLimit(lines: List<String>, limit: Int?): List<String> {
        if (limit != null && limit > 0 && lines.size > limit) {
            return lines.takeLast(limit)
        }
        return lines
    }

    private fun record(name: String, durationMs: Long, details: String) {
        val safeDurationMs = max(0L, durationMs)
        val record = PerformanceMetricRecord(
            timestampMs = System.currentTimeMillis(),
            name = name,
            durationMs = safeDurationMs,
            details = details
        )
        synchronized(lock) {
            if (records.size >= MAX_RECORDS) {
                records.removeFirst()
            }
            records.addLast(record)
        }
        runCatching {
            logSink("[Perf] $name duration=${safeDurationMs}ms $details")
        }
    }
}
