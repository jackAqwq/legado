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
}
