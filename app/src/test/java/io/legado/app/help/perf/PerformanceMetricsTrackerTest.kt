package io.legado.app.help.perf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceMetricsTrackerTest {

    @Test
    fun record_startup_duration_when_enabled() {
        val logs = mutableListOf<String>()
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { true }
        PerformanceMetricsTracker.logSink = { logs.add(it) }

        PerformanceMetricsTracker.markAppOnCreateStart(uptimeMs = 100)
        PerformanceMetricsTracker.markMainUiReady(uptimeMs = 165)

        val records = PerformanceMetricsTracker.snapshot()
        assertEquals(1, records.size)
        assertEquals("startup.main_ui_ready", records[0].name)
        assertEquals(65, records[0].durationMs)
        assertTrue(records[0].details.contains("stage=main_activity"))
        assertEquals("main_activity", records[0].detailEntries["stage"])
        assertTrue(logs.any { it.contains("startup.main_ui_ready") })
    }

    @Test
    fun do_not_record_when_disabled() {
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { false }
        PerformanceMetricsTracker.logSink = {}

        PerformanceMetricsTracker.markAppOnCreateStart(uptimeMs = 100)
        PerformanceMetricsTracker.markMainUiReady(uptimeMs = 150)
        PerformanceMetricsTracker.markPageFlipGestureStart(uptimeMs = 1000)
        PerformanceMetricsTracker.markPageFlipCompleted(uptimeMs = 1100)
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 40,
            source = "ReadRssActivity",
            success = true
        )

        assertTrue(PerformanceMetricsTracker.snapshot().isEmpty())
    }

    @Test
    fun record_page_flip_latency_between_start_and_complete() {
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { true }
        PerformanceMetricsTracker.logSink = {}

        PerformanceMetricsTracker.markPageFlipGestureStart(uptimeMs = 1000)
        PerformanceMetricsTracker.markPageFlipCompleted(uptimeMs = 1033)

        val records = PerformanceMetricsTracker.snapshot()
        assertEquals(1, records.size)
        assertEquals("read.page_flip", records[0].name)
        assertEquals(33, records[0].durationMs)
        assertTrue(records[0].details.contains("result=success"))
    }

    @Test
    fun cancel_page_flip_metric_should_clear_pending_start_without_recording() {
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { true }
        PerformanceMetricsTracker.logSink = {}

        PerformanceMetricsTracker.markPageFlipGestureStart(uptimeMs = 1000)
        PerformanceMetricsTracker.cancelPageFlipGesture()
        PerformanceMetricsTracker.markPageFlipCompleted(uptimeMs = 1033)

        assertTrue(PerformanceMetricsTracker.snapshot().isEmpty())
    }

    @Test
    fun record_startup_stage_metrics_without_clearing_main_ui_ready_start() {
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { true }
        PerformanceMetricsTracker.logSink = {}

        PerformanceMetricsTracker.markAppOnCreateStart(uptimeMs = 100)
        PerformanceMetricsTracker.markStartupStage(
            stageName = "app_bootstrap_ready",
            uptimeMs = 130
        )
        PerformanceMetricsTracker.markStartupStage(
            stageName = "main_activity_created",
            uptimeMs = 160
        )
        PerformanceMetricsTracker.markMainUiReady(uptimeMs = 190)

        val records = PerformanceMetricsTracker.snapshot()
        assertEquals(3, records.size)
        assertEquals("startup.app_bootstrap_ready", records[0].name)
        assertEquals(30, records[0].durationMs)
        assertEquals("startup.main_activity_created", records[1].name)
        assertEquals(60, records[1].durationMs)
        assertEquals("startup.main_ui_ready", records[2].name)
        assertEquals(90, records[2].durationMs)
    }

    @Test
    fun keep_latest_records_with_ring_buffer_limit() {
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { true }
        PerformanceMetricsTracker.logSink = {}

        repeat(PerformanceMetricsTracker.MAX_RECORDS + 15) { index ->
            PerformanceMetricsTracker.recordRssInterceptDuration(
                durationMs = index.toLong(),
                source = "ReadRssActivity",
                success = true
            )
        }

        val records = PerformanceMetricsTracker.snapshot()
        assertEquals(PerformanceMetricsTracker.MAX_RECORDS, records.size)
        assertEquals(15L, records.first().durationMs)
        assertEquals(
            (PerformanceMetricsTracker.MAX_RECORDS + 14).toLong(),
            records.last().durationMs
        )
    }

    @Test
    fun export_lines_include_recorded_metrics() {
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { true }
        PerformanceMetricsTracker.logSink = {}

        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 88,
            source = "BottomWebViewDialog",
            success = false
        )
        val lines = PerformanceMetricsTracker.exportLines()

        assertFalse(lines.isEmpty())
        assertTrue(lines.first().contains("rss.intercept"))
        assertTrue(lines.first().contains("BottomWebViewDialog"))
    }

    @Test
    fun export_lines_support_prefix_and_limit() {
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { true }
        PerformanceMetricsTracker.logSink = {}

        PerformanceMetricsTracker.markAppOnCreateStart(uptimeMs = 100)
        PerformanceMetricsTracker.markMainUiReady(uptimeMs = 180)
        PerformanceMetricsTracker.markPageFlipGestureStart(uptimeMs = 200)
        PerformanceMetricsTracker.markPageFlipCompleted(uptimeMs = 240)
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 50,
            source = "ReadRssActivity",
            success = true
        )
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 70,
            source = "BottomWebViewDialog",
            success = false
        )

        val rssOnlyRecentOne = PerformanceMetricsTracker.exportLines(
            namePrefix = "rss.",
            limit = 1
        )
        assertEquals(1, rssOnlyRecentOne.size)
        assertTrue(rssOnlyRecentOne.first().contains("BottomWebViewDialog"))
    }

    @Test
    fun clear_metrics_should_remove_all_records() {
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { true }
        PerformanceMetricsTracker.logSink = {}

        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 10,
            source = "ReadRssActivity",
            success = true
        )
        assertFalse(PerformanceMetricsTracker.snapshot().isEmpty())

        PerformanceMetricsTracker.clearMetrics()

        assertTrue(PerformanceMetricsTracker.snapshot().isEmpty())
        assertTrue(PerformanceMetricsTracker.exportLines().isEmpty())
    }

    @Test
    fun export_grouped_lines_should_split_metrics_by_prefix() {
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { true }
        PerformanceMetricsTracker.logSink = {}

        PerformanceMetricsTracker.markAppOnCreateStart(uptimeMs = 100)
        PerformanceMetricsTracker.markMainUiReady(uptimeMs = 150)
        PerformanceMetricsTracker.markPageFlipGestureStart(uptimeMs = 200)
        PerformanceMetricsTracker.markPageFlipCompleted(uptimeMs = 260)
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 90,
            source = "ReadRssActivity",
            success = true
        )

        val grouped = PerformanceMetricsTracker.exportGroupedLines()

        assertEquals(3, grouped.all.size)
        assertEquals(1, grouped.startup.size)
        assertEquals(1, grouped.read.size)
        assertEquals(1, grouped.rss.size)
        assertTrue(grouped.startup.first().contains("startup.main_ui_ready"))
        assertTrue(grouped.read.first().contains("read.page_flip"))
        assertTrue(grouped.rss.first().contains("rss.intercept"))
    }

    @Test
    fun export_slow_lines_should_return_top_n_in_desc_order() {
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { true }
        PerformanceMetricsTracker.logSink = {}

        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 10,
            source = "A",
            success = true
        )
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 70,
            source = "B",
            success = true
        )
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 30,
            source = "C",
            success = true
        )

        val top2 = PerformanceMetricsTracker.exportSlowLines(limit = 2)

        assertEquals(2, top2.size)
        assertTrue(top2[0].contains("|70ms|"))
        assertTrue(top2[1].contains("|30ms|"))
    }

    @Test
    fun build_summary_should_compute_count_avg_and_p95() {
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { true }
        PerformanceMetricsTracker.logSink = {}

        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 10,
            source = "A",
            success = true
        )
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 20,
            source = "B",
            success = true
        )
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 100,
            source = "C",
            success = true
        )

        val summary = PerformanceMetricsTracker.buildSummary(namePrefix = "rss.")

        assertEquals(3, summary.count)
        assertEquals(43, summary.avgDurationMs)
        assertEquals(100, summary.p95DurationMs)
    }

    @Test
    fun export_lines_should_support_source_filter() {
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { true }
        PerformanceMetricsTracker.logSink = {}

        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 30,
            source = "ReadRssActivity",
            success = true
        )
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 60,
            source = "BottomWebViewDialog",
            success = false
        )

        val readRssOnly = PerformanceMetricsTracker.exportLines(
            namePrefix = "rss.",
            source = "ReadRssActivity"
        )

        assertEquals(1, readRssOnly.size)
        assertTrue(readRssOnly.first().contains("ReadRssActivity"))
    }

    @Test
    fun build_source_summaries_should_group_by_source() {
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { true }
        PerformanceMetricsTracker.logSink = {}

        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 10,
            source = "ReadRssActivity",
            success = true
        )
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 30,
            source = "ReadRssActivity",
            success = true
        )
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 100,
            source = "BottomWebViewDialog",
            success = true
        )

        val summaries = PerformanceMetricsTracker.buildSourceSummaries(namePrefix = "rss.")

        assertEquals(2, summaries.size)
        val readSummary = summaries.first { it.source == "ReadRssActivity" }
        assertEquals(2, readSummary.count)
        assertEquals(20, readSummary.avgDurationMs)
        assertEquals(30, readSummary.p95DurationMs)
        val webViewSummary = summaries.first { it.source == "BottomWebViewDialog" }
        assertEquals(1, webViewSummary.count)
        assertEquals(100, webViewSummary.avgDurationMs)
        assertEquals(100, webViewSummary.p95DurationMs)
    }

    @Test
    fun export_lines_should_support_result_filter() {
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { true }
        PerformanceMetricsTracker.logSink = {}

        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 20,
            source = "ReadRssActivity",
            success = true
        )
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 80,
            source = "BottomWebViewDialog",
            success = false
        )

        val failedOnly = PerformanceMetricsTracker.exportLines(
            namePrefix = "rss.",
            result = "failure"
        )

        assertEquals(1, failedOnly.size)
        assertTrue(failedOnly.first().contains("result=failure"))
    }

    @Test
    fun build_result_summaries_should_group_by_success_and_failure() {
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { true }
        PerformanceMetricsTracker.logSink = {}

        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 10,
            source = "ReadRssActivity",
            success = true
        )
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 30,
            source = "BottomWebViewDialog",
            success = true
        )
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 90,
            source = "BottomWebViewDialog",
            success = false
        )

        val summaries = PerformanceMetricsTracker.buildResultSummaries(namePrefix = "rss.")

        assertEquals(2, summaries.size)
        val successSummary = summaries.first { it.result == "success" }
        assertEquals(2, successSummary.count)
        assertEquals(20, successSummary.avgDurationMs)
        assertEquals(30, successSummary.p95DurationMs)
        val failureSummary = summaries.first { it.result == "failure" }
        assertEquals(1, failureSummary.count)
        assertEquals(90, failureSummary.avgDurationMs)
        assertEquals(90, failureSummary.p95DurationMs)
    }

    @Test
    fun build_source_result_summaries_should_group_by_source_and_result() {
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { true }
        PerformanceMetricsTracker.logSink = {}

        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 40,
            source = "ReadRssActivity",
            success = false
        )
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 20,
            source = "ReadRssActivity",
            success = true
        )
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 80,
            source = "BottomWebViewDialog",
            success = false
        )

        val summaries = PerformanceMetricsTracker.buildSourceResultSummaries(namePrefix = "rss.")

        assertEquals(3, summaries.size)
        val readFailure = summaries.first {
            it.source == "ReadRssActivity" && it.result == "failure"
        }
        assertEquals(1, readFailure.count)
        assertEquals(40, readFailure.avgDurationMs)
        val webFailure = summaries.first {
            it.source == "BottomWebViewDialog" && it.result == "failure"
        }
        assertEquals(1, webFailure.count)
        assertEquals(80, webFailure.avgDurationMs)
    }

    @Test
    fun build_failure_summaries_should_group_failed_rss_metrics_by_bucket() {
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { true }
        PerformanceMetricsTracker.logSink = {}

        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 80,
            source = "ReadRssActivity",
            success = false,
            failureType = "SocketTimeoutException"
        )
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 40,
            source = "BottomWebViewDialog",
            success = false,
            statusCode = 500,
            contentType = "text/html"
        )
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 20,
            source = "BottomWebViewDialog",
            success = true
        )

        val summaries = PerformanceMetricsTracker.buildFailureSummaries(namePrefix = "rss.")

        assertEquals(2, summaries.size)
        val statusSummary = summaries.first { it.bucket == "http_500" }
        assertEquals(1, statusSummary.count)
        assertEquals(40, statusSummary.avgDurationMs)
        val timeoutSummary = summaries.first { it.bucket == "SocketTimeoutException" }
        assertEquals(1, timeoutSummary.count)
        assertEquals(80, timeoutSummary.avgDurationMs)
    }

    @Test
    fun export_lines_should_support_failure_bucket_filter() {
        PerformanceMetricsTracker.resetForTest()
        PerformanceMetricsTracker.enabledProvider = { true }
        PerformanceMetricsTracker.logSink = {}

        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 80,
            source = "ReadRssActivity",
            success = false,
            failureType = "SocketTimeoutException"
        )
        PerformanceMetricsTracker.recordRssInterceptDuration(
            durationMs = 40,
            source = "BottomWebViewDialog",
            success = false,
            statusCode = 500
        )

        val http500Only = PerformanceMetricsTracker.exportLines(
            namePrefix = "rss.",
            result = "failure",
            failureBucket = "http_500"
        )

        assertEquals(1, http500Only.size)
        assertTrue(http500Only.first().contains("statusCode=500"))
    }
}
