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

internal data class PerformanceMetricsSourceSummary(
    val source: String,
    val count: Int,
    val avgDurationMs: Long,
    val p95DurationMs: Long
)

internal data class PerformanceMetricsResultSummary(
    val result: String,
    val count: Int,
    val avgDurationMs: Long,
    val p95DurationMs: Long
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

    fun exportLines(
        namePrefix: String? = null,
        limit: Int? = null,
        source: String? = null,
        result: String? = null
    ): List<String> {
        val filteredRecords = selectRecords(
            source = snapshot(),
            namePrefix = namePrefix,
            limit = limit,
            sourceFilter = source,
            resultFilter = result
        )
        return filteredRecords.map(::toExportLine)
    }

    fun exportSlowLines(
        limit: Int = 20,
        namePrefix: String? = null,
        source: String? = null,
        result: String? = null
    ): List<String> {
        if (limit <= 0) return emptyList()
        return selectRecords(
            source = snapshot(),
            namePrefix = namePrefix,
            limit = null,
            sourceFilter = source,
            resultFilter = result
        )
            .sortedByDescending { it.durationMs }
            .take(limit)
            .map(::toExportLine)
    }

    fun buildSummary(
        namePrefix: String? = null,
        limit: Int? = null,
        source: String? = null,
        result: String? = null
    ): PerformanceMetricsSummary {
        val selected = selectRecords(
            source = snapshot(),
            namePrefix = namePrefix,
            limit = limit,
            sourceFilter = source,
            resultFilter = result
        )
        return buildSummaryFromRecords(selected)
    }

    fun buildSourceSummaries(
        namePrefix: String = "rss.",
        limit: Int? = null
    ): List<PerformanceMetricsSourceSummary> {
        val selected = selectRecords(
            source = snapshot(),
            namePrefix = namePrefix,
            limit = limit,
            sourceFilter = null,
            resultFilter = null
        )
        val grouped = linkedMapOf<String, MutableList<PerformanceMetricRecord>>()
        selected.forEach { record ->
            val source = detailValue(record.details, "source") ?: return@forEach
            grouped.getOrPut(source) { mutableListOf() }.add(record)
        }
        return grouped.map { (source, records) ->
            val summary = buildSummaryFromRecords(records)
            PerformanceMetricsSourceSummary(
                source = source,
                count = summary.count,
                avgDurationMs = summary.avgDurationMs,
                p95DurationMs = summary.p95DurationMs
            )
        }.sortedByDescending { it.avgDurationMs }
    }

    fun buildResultSummaries(
        namePrefix: String = "rss.",
        limit: Int? = null
    ): List<PerformanceMetricsResultSummary> {
        val selected = selectRecords(
            source = snapshot(),
            namePrefix = namePrefix,
            limit = limit,
            sourceFilter = null,
            resultFilter = null
        )
        val grouped = linkedMapOf<String, MutableList<PerformanceMetricRecord>>()
        selected.forEach { record ->
            val result = detailValue(record.details, "result") ?: return@forEach
            grouped.getOrPut(result) { mutableListOf() }.add(record)
        }
        return grouped.map { (result, records) ->
            val summary = buildSummaryFromRecords(records)
            PerformanceMetricsResultSummary(
                result = result,
                count = summary.count,
                avgDurationMs = summary.avgDurationMs,
                p95DurationMs = summary.p95DurationMs
            )
        }.sortedByDescending { it.avgDurationMs }
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
        limit: Int?,
        sourceFilter: String?,
        resultFilter: String?
    ): List<PerformanceMetricRecord> {
        var selected = source
        if (!namePrefix.isNullOrBlank()) {
            selected = selected.filter { it.name.startsWith(namePrefix) }
        }
        if (!sourceFilter.isNullOrBlank()) {
            selected = selected.filter { detailValue(it.details, "source") == sourceFilter }
        }
        if (!resultFilter.isNullOrBlank()) {
            selected = selected.filter { detailValue(it.details, "result") == resultFilter }
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

    private fun buildSummaryFromRecords(records: List<PerformanceMetricRecord>): PerformanceMetricsSummary {
        if (records.isEmpty()) {
            return PerformanceMetricsSummary(
                count = 0,
                avgDurationMs = 0L,
                p95DurationMs = 0L
            )
        }
        val sortedDurations = records.map { it.durationMs }.sorted()
        val p95Index = (ceil(sortedDurations.size * 0.95).toInt() - 1)
            .coerceIn(0, sortedDurations.lastIndex)
        return PerformanceMetricsSummary(
            count = sortedDurations.size,
            avgDurationMs = sortedDurations.sum() / sortedDurations.size,
            p95DurationMs = sortedDurations[p95Index]
        )
    }

    private fun detailValue(details: String, key: String): String? {
        val token = "$key="
        val start = details.indexOf(token)
        if (start < 0) return null
        val valueStart = start + token.length
        val valueEnd = details.indexOf(',', valueStart).takeIf { it >= 0 } ?: details.length
        if (valueStart >= valueEnd) return null
        return details.substring(valueStart, valueEnd)
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
