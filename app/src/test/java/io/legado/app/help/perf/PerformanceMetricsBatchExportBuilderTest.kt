package io.legado.app.help.perf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceMetricsBatchExportBuilderTest {

    @Test
    fun build_entries_should_output_all_named_files() {
        val entries = PerformanceMetricsBatchExportBuilder.buildEntries(
            allLines = listOf("1|startup.main_ui_ready|50ms|stage=main_activity"),
            startupLines = listOf("1|startup.main_ui_ready|50ms|stage=main_activity"),
            readLines = listOf("2|read.page_flip|30ms|result=success"),
            rssLines = listOf("3|rss.intercept|40ms|source=ReadRssActivity,result=success"),
            rssSourceResultSummaryLines = listOf(
                "source=ReadRssActivity|result=success|count=1|avg=40ms|p95=40ms"
            ),
            rssFailureSummaryLines = listOf(
                "bucket=http_500|count=1|avg=90ms|p95=90ms"
            ),
            rssFailureSlowestLines = listOf(
                "4|rss.intercept|90ms|source=BottomWebViewDialog,result=failure,statusCode=500"
            ),
            generatedAtMs = 1234L
        )

        assertEquals(7, entries.size)
        assertEquals("performance_metrics_all.txt", entries[0].fileName)
        assertEquals("performance_metrics_startup.txt", entries[1].fileName)
        assertEquals("performance_metrics_read.txt", entries[2].fileName)
        assertEquals("performance_metrics_rss.txt", entries[3].fileName)
        assertEquals("performance_metrics_rss_source_result_summary.txt", entries[4].fileName)
        assertEquals("performance_metrics_rss_failure_summary.txt", entries[5].fileName)
        assertEquals("performance_metrics_rss_failure_slowest_20.txt", entries[6].fileName)
        assertTrue(entries[0].text.contains("summary.count=1"))
        assertTrue(entries[0].text.contains("summary.avg_duration_ms=50"))
        assertTrue(entries[0].text.contains("summary.p95_duration_ms=50"))
        assertTrue(entries[4].text.contains("source=ReadRssActivity|result=success"))
        assertTrue(entries[5].text.contains("bucket=http_500"))
        assertTrue(entries[6].text.contains("summary.p95_duration_ms=90"))
    }

    @Test
    fun build_entries_should_keep_empty_hint_for_empty_group() {
        val entries = PerformanceMetricsBatchExportBuilder.buildEntries(
            allLines = listOf("1|startup.main_ui_ready|50ms|stage=main_activity"),
            startupLines = listOf("1|startup.main_ui_ready|50ms|stage=main_activity"),
            readLines = emptyList(),
            rssLines = emptyList(),
            generatedAtMs = 5678L
        )

        val readEntry = entries.first { it.fileName == "performance_metrics_read.txt" }
        assertTrue(readEntry.text.contains("generated_at_ms=5678"))
        assertTrue(readEntry.text.contains("summary.count=0"))
        assertTrue(readEntry.text.contains("summary.avg_duration_ms=0"))
        assertTrue(readEntry.text.contains("summary.p95_duration_ms=0"))
        assertTrue(readEntry.text.contains("No performance metrics recorded."))

        val rssSummaryEntry = entries.first {
            it.fileName == "performance_metrics_rss_source_result_summary.txt"
        }
        assertTrue(rssSummaryEntry.text.contains("No performance metrics recorded."))

        val rssFailureSummaryEntry = entries.first {
            it.fileName == "performance_metrics_rss_failure_summary.txt"
        }
        assertTrue(rssFailureSummaryEntry.text.contains("No performance metrics recorded."))
    }

    @Test
    fun build_entries_should_calculate_p95_for_group_lines() {
        val entries = PerformanceMetricsBatchExportBuilder.buildEntries(
            allLines = listOf(
                "1|read.page_flip|10ms|result=success",
                "2|read.page_flip|20ms|result=success",
                "3|read.page_flip|30ms|result=success",
                "4|read.page_flip|40ms|result=success",
                "5|read.page_flip|100ms|result=success"
            ),
            startupLines = emptyList(),
            readLines = listOf(
                "1|read.page_flip|10ms|result=success",
                "2|read.page_flip|20ms|result=success",
                "3|read.page_flip|30ms|result=success",
                "4|read.page_flip|40ms|result=success",
                "5|read.page_flip|100ms|result=success"
            ),
            rssLines = emptyList(),
            generatedAtMs = 9999L
        )

        val readEntry = entries.first { it.fileName == "performance_metrics_read.txt" }
        assertTrue(readEntry.text.contains("summary.count=5"))
        assertTrue(readEntry.text.contains("summary.avg_duration_ms=40"))
        assertTrue(readEntry.text.contains("summary.p95_duration_ms=100"))
    }
}
