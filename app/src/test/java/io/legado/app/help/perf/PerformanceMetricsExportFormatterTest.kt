package io.legado.app.help.perf

import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceMetricsExportFormatterTest {

    @Test
    fun include_header_timestamp_and_all_lines_when_metrics_exist() {
        val text = PerformanceMetricsExportFormatter.toText(
            lines = listOf(
                "1|startup.main_ui_ready|120ms|stage=main_activity",
                "2|read.page_flip|45ms|result=success"
            ),
            generatedAtMs = 1234L
        )

        assertTrue(text.contains("# Performance Metrics Export"))
        assertTrue(text.contains("generated_at_ms=1234"))
        assertTrue(text.contains("1|startup.main_ui_ready|120ms|stage=main_activity"))
        assertTrue(text.contains("2|read.page_flip|45ms|result=success"))
    }

    @Test
    fun include_empty_hint_when_metrics_are_empty() {
        val text = PerformanceMetricsExportFormatter.toText(
            lines = emptyList(),
            generatedAtMs = 5678L
        )

        assertTrue(text.contains("generated_at_ms=5678"))
        assertTrue(text.contains("No performance metrics recorded."))
    }
}
