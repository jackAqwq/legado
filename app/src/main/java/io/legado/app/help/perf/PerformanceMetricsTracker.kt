package io.legado.app.help.perf

import io.legado.app.constant.AppLog
import io.legado.app.help.config.AppConfig
import kotlin.math.max
import kotlin.math.ceil

internal data class PerformanceMetricRecord(
    val timestampMs: Long,
    val name: String,
    val durationMs: Long,
    val detailEntries: PerformanceMetricDetails
)

internal val PerformanceMetricRecord.details: String
    get() = detailEntries.encode()

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

internal data class PerformanceMetricsSourceResultSummary(
    val source: String,
    val result: String,
    val count: Int,
    val avgDurationMs: Long,
    val p95DurationMs: Long
)

internal data class PerformanceMetricsFailureSummary(
    val bucket: String,
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
            detailEntries = PerformanceMetricDetails.of(
                "stage" to "main_activity"
            )
        )
    }

    fun markStartupStage(stageName: String, uptimeMs: Long) {
        if (!enabledProvider()) return
        if (stageName.isBlank()) return
        val startUptimeMs = synchronized(lock) {
            appOnCreateStartUptimeMs
        } ?: return
        record(
            name = "startup.$stageName",
            durationMs = uptimeMs - startUptimeMs,
            detailEntries = PerformanceMetricDetails.of(
                "stage" to stageName
            )
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
            detailEntries = PerformanceMetricDetails.of(
                "result" to result
            )
        )
    }

    fun cancelPageFlipGesture() {
        synchronized(lock) {
            pageFlipStartUptimeMs = null
        }
    }

    fun recordRssInterceptDuration(
        durationMs: Long,
        source: String,
        success: Boolean,
        failureType: String? = null,
        statusCode: Int? = null,
        contentType: String? = null
    ) {
        if (!enabledProvider()) return
        record(
            name = "rss.intercept",
            durationMs = durationMs,
            detailEntries = PerformanceMetricDetails.of(
                "source" to source,
                "result" to if (success) "success" else "failure",
                "failureType" to failureType,
                "statusCode" to statusCode?.toString(),
                "contentType" to contentType
            )
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
        result: String? = null,
        failureBucket: String? = null
    ): List<String> {
        val filteredRecords = selectRecords(
            source = snapshot(),
            namePrefix = namePrefix,
            limit = limit,
            sourceFilter = source,
            resultFilter = result,
            failureBucketFilter = failureBucket
        )
        return filteredRecords.map(::toExportLine)
    }

    fun exportSlowLines(
        limit: Int = 20,
        namePrefix: String? = null,
        source: String? = null,
        result: String? = null,
        failureBucket: String? = null
    ): List<String> {
        if (limit <= 0) return emptyList()
        return selectRecords(
            source = snapshot(),
            namePrefix = namePrefix,
            limit = null,
            sourceFilter = source,
            resultFilter = result,
            failureBucketFilter = failureBucket
        )
            .sortedByDescending { it.durationMs }
            .take(limit)
            .map(::toExportLine)
    }

    fun buildSummary(
        namePrefix: String? = null,
        limit: Int? = null,
        source: String? = null,
        result: String? = null,
        failureBucket: String? = null
    ): PerformanceMetricsSummary {
        val selected = selectRecords(
            source = snapshot(),
            namePrefix = namePrefix,
            limit = limit,
            sourceFilter = source,
            resultFilter = result,
            failureBucketFilter = failureBucket
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
            resultFilter = null,
            failureBucketFilter = null
        )
        val grouped = linkedMapOf<String, MutableList<PerformanceMetricRecord>>()
        selected.forEach { record ->
            val source = record.detailEntries["source"] ?: return@forEach
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
            resultFilter = null,
            failureBucketFilter = null
        )
        val grouped = linkedMapOf<String, MutableList<PerformanceMetricRecord>>()
        selected.forEach { record ->
            val result = record.detailEntries["result"] ?: return@forEach
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

    fun buildSourceResultSummaries(
        namePrefix: String = "rss.",
        limit: Int? = null
    ): List<PerformanceMetricsSourceResultSummary> {
        val selected = selectRecords(
            source = snapshot(),
            namePrefix = namePrefix,
            limit = limit,
            sourceFilter = null,
            resultFilter = null,
            failureBucketFilter = null
        )
        val grouped = linkedMapOf<Pair<String, String>, MutableList<PerformanceMetricRecord>>()
        selected.forEach { record ->
            val source = record.detailEntries["source"] ?: return@forEach
            val result = record.detailEntries["result"] ?: return@forEach
            grouped.getOrPut(source to result) { mutableListOf() }.add(record)
        }
        return grouped.map { (key, records) ->
            val summary = buildSummaryFromRecords(records)
            PerformanceMetricsSourceResultSummary(
                source = key.first,
                result = key.second,
                count = summary.count,
                avgDurationMs = summary.avgDurationMs,
                p95DurationMs = summary.p95DurationMs
            )
        }.sortedByDescending { it.avgDurationMs }
    }

    fun exportSourceResultSummaryLines(
        namePrefix: String = "rss.",
        limit: Int? = null
    ): List<String> {
        return buildSourceResultSummaries(namePrefix, limit).map { summary ->
            "source=${summary.source}|result=${summary.result}|count=${summary.count}|avg=${summary.avgDurationMs}ms|p95=${summary.p95DurationMs}ms"
        }
    }

    fun buildFailureSummaries(
        namePrefix: String = "rss.",
        limit: Int? = null
    ): List<PerformanceMetricsFailureSummary> {
        val selected = selectRecords(
            source = snapshot(),
            namePrefix = namePrefix,
            limit = limit,
            sourceFilter = null,
            resultFilter = "failure",
            failureBucketFilter = null
        )
        val grouped = linkedMapOf<String, MutableList<PerformanceMetricRecord>>()
        selected.forEach { record ->
            val bucket = failureBucketOf(record) ?: return@forEach
            grouped.getOrPut(bucket) { mutableListOf() }.add(record)
        }
        return grouped.map { (bucket, records) ->
            val summary = buildSummaryFromRecords(records)
            PerformanceMetricsFailureSummary(
                bucket = bucket,
                count = summary.count,
                avgDurationMs = summary.avgDurationMs,
                p95DurationMs = summary.p95DurationMs
            )
        }.sortedByDescending { it.avgDurationMs }
    }

    fun exportFailureSummaryLines(
        namePrefix: String = "rss.",
        limit: Int? = null
    ): List<String> {
        return buildFailureSummaries(namePrefix, limit).map { summary ->
            "bucket=${summary.bucket}|count=${summary.count}|avg=${summary.avgDurationMs}ms|p95=${summary.p95DurationMs}ms"
        }
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
        resultFilter: String?,
        failureBucketFilter: String?
    ): List<PerformanceMetricRecord> {
        var selected = source
        if (!namePrefix.isNullOrBlank()) {
            selected = selected.filter { it.name.startsWith(namePrefix) }
        }
        if (!sourceFilter.isNullOrBlank()) {
            selected = selected.filter { it.detailEntries["source"] == sourceFilter }
        }
        if (!resultFilter.isNullOrBlank()) {
            selected = selected.filter { it.detailEntries["result"] == resultFilter }
        }
        if (!failureBucketFilter.isNullOrBlank()) {
            selected = selected.filter { failureBucketOf(it) == failureBucketFilter }
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

    private fun failureBucketOf(record: PerformanceMetricRecord): String? {
        val result = record.detailEntries["result"]
        if (result != "failure") {
            return null
        }
        record.detailEntries["statusCode"]?.takeIf(String::isNotBlank)?.let { statusCode ->
            return "http_$statusCode"
        }
        record.detailEntries["failureType"]?.takeIf(String::isNotBlank)?.let { failureType ->
            return failureType
        }
        return "unknown"
    }

    private fun record(
        name: String,
        durationMs: Long,
        detailEntries: PerformanceMetricDetails = PerformanceMetricDetails.empty()
    ) {
        val safeDurationMs = max(0L, durationMs)
        val record = PerformanceMetricRecord(
            timestampMs = System.currentTimeMillis(),
            name = name,
            durationMs = safeDurationMs,
            detailEntries = detailEntries
        )
        synchronized(lock) {
            if (records.size >= MAX_RECORDS) {
                records.removeFirst()
            }
            records.addLast(record)
        }
        runCatching {
            val detailText = record.details
            logSink(
                if (detailText.isBlank()) {
                    "[Perf] $name duration=${safeDurationMs}ms"
                } else {
                    "[Perf] $name duration=${safeDurationMs}ms $detailText"
                }
            )
        }
    }
}
