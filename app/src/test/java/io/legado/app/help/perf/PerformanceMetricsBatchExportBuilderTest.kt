package io.legado.app.help.perf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceMetricsBatchExportBuilderTest {

    @Test
    fun build_entries_should_output_four_named_files() {
        val entries = PerformanceMetricsBatchExportBuilder.buildEntries(
            allLines = listOf("1|startup.main_ui_ready|50ms|stage=main_activity"),
            startupLines = listOf("1|startup.main_ui_ready|50ms|stage=main_activity"),
            readLines = listOf("2|read.page_flip|30ms|result=success"),
            rssLines = listOf("3|rss.intercept|40ms|source=ReadRssActivity,result=success"),
            generatedAtMs = 1234L
        )

        assertEquals(4, entries.size)
        assertEquals("performance_metrics_all.txt", entries[0].fileName)
        assertEquals("performance_metrics_startup.txt", entries[1].fileName)
        assertEquals("performance_metrics_read.txt", entries[2].fileName)
        assertEquals("performance_metrics_rss.txt", entries[3].fileName)
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
        assertTrue(readEntry.text.contains("No performance metrics recorded."))
    }
}
