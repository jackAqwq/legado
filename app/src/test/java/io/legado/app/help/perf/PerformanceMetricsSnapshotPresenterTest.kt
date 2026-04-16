package io.legado.app.help.perf

import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceMetricsSnapshotPresenterTest {

    @Test
    fun build_preview_text_should_include_metrics_lines() {
        val text = PerformanceMetricsSnapshotPresenter.buildPreviewText(
            lines = listOf(
                "100|startup.main_ui_ready|90ms|stage=main_activity",
                "200|read.page_flip|35ms|result=success"
            ),
            generatedAtMs = 300
        )

        assertTrue(text.contains("# Performance Metrics Export"))
        assertTrue(text.contains("generated_at_ms=300"))
        assertTrue(text.contains("startup.main_ui_ready"))
        assertTrue(text.contains("read.page_flip"))
    }

    @Test
    fun build_preview_text_should_include_empty_hint_for_empty_lines() {
        val text = PerformanceMetricsSnapshotPresenter.buildPreviewText(
            lines = emptyList(),
            generatedAtMs = 400
        )

        assertTrue(text.contains("generated_at_ms=400"))
        assertTrue(text.contains("No performance metrics recorded."))
    }
}
