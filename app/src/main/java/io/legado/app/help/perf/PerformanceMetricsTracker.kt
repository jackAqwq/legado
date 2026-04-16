package io.legado.app.help.perf

import io.legado.app.constant.AppLog
import io.legado.app.help.config.AppConfig
import kotlin.math.max

internal data class PerformanceMetricRecord(
    val timestampMs: Long,
    val name: String,
    val durationMs: Long,
    val details: String
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

    fun exportLines(): List<String> = snapshot().map { record ->
        "${record.timestampMs}|${record.name}|${record.durationMs}ms|${record.details}"
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
