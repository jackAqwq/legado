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

    @Test
    fun build_preview_text_should_include_summary_when_provided() {
        val text = PerformanceMetricsSnapshotPresenter.buildPreviewText(
            lines = listOf("100|rss.intercept|80ms|source=ReadRssActivity,result=success"),
            generatedAtMs = 500,
            summary = PerformanceMetricsSummary(
                count = 1,
                avgDurationMs = 80,
                p95DurationMs = 80
            )
        )

        assertTrue(text.contains("summary.count=1"))
        assertTrue(text.contains("summary.avg_duration_ms=80"))
        assertTrue(text.contains("summary.p95_duration_ms=80"))
    }

    @Test
    fun build_source_summary_text_should_include_source_aggregation_lines() {
        val text = PerformanceMetricsSnapshotPresenter.buildSourceSummaryText(
            summaries = listOf(
                PerformanceMetricsSourceSummary(
                    source = "ReadRssActivity",
                    count = 2,
                    avgDurationMs = 40,
                    p95DurationMs = 60
                ),
                PerformanceMetricsSourceSummary(
                    source = "BottomWebViewDialog",
                    count = 1,
                    avgDurationMs = 80,
                    p95DurationMs = 80
                )
            ),
            generatedAtMs = 600
        )

        assertTrue(text.contains("generated_at_ms=600"))
        assertTrue(text.contains("source=ReadRssActivity"))
        assertTrue(text.contains("count=2"))
        assertTrue(text.contains("avg=40ms"))
        assertTrue(text.contains("p95=60ms"))
        assertTrue(text.contains("source=BottomWebViewDialog"))
    }

    @Test
    fun build_result_summary_text_should_include_result_aggregation_lines() {
        val text = PerformanceMetricsSnapshotPresenter.buildResultSummaryText(
            summaries = listOf(
                PerformanceMetricsResultSummary(
                    result = "success",
                    count = 2,
                    avgDurationMs = 25,
                    p95DurationMs = 30
                ),
                PerformanceMetricsResultSummary(
                    result = "failure",
                    count = 1,
                    avgDurationMs = 90,
                    p95DurationMs = 90
                )
            ),
            generatedAtMs = 700
        )

        assertTrue(text.contains("generated_at_ms=700"))
        assertTrue(text.contains("result=success"))
        assertTrue(text.contains("count=2"))
        assertTrue(text.contains("avg=25ms"))
        assertTrue(text.contains("p95=30ms"))
        assertTrue(text.contains("result=failure"))
    }

    @Test
    fun build_source_result_summary_text_should_include_2d_aggregation_lines() {
        val text = PerformanceMetricsSnapshotPresenter.buildSourceResultSummaryText(
            summaries = listOf(
                PerformanceMetricsSourceResultSummary(
                    source = "ReadRssActivity",
                    result = "failure",
                    count = 1,
                    avgDurationMs = 40,
                    p95DurationMs = 40
                ),
                PerformanceMetricsSourceResultSummary(
                    source = "BottomWebViewDialog",
                    result = "success",
                    count = 2,
                    avgDurationMs = 30,
                    p95DurationMs = 35
                )
            ),
            generatedAtMs = 800
        )

        assertTrue(text.contains("generated_at_ms=800"))
        assertTrue(text.contains("source=ReadRssActivity|result=failure"))
        assertTrue(text.contains("source=BottomWebViewDialog|result=success"))
        assertTrue(text.contains("count=2"))
    }

    @Test
    fun build_failure_summary_text_should_include_failure_bucket_lines() {
        val text = PerformanceMetricsSnapshotPresenter.buildFailureSummaryText(
            summaries = listOf(
                PerformanceMetricsFailureSummary(
                    bucket = "http_500",
                    count = 2,
                    avgDurationMs = 65,
                    p95DurationMs = 80
                ),
                PerformanceMetricsFailureSummary(
                    bucket = "SocketTimeoutException",
                    count = 1,
                    avgDurationMs = 120,
                    p95DurationMs = 120
                )
            ),
            generatedAtMs = 900
        )

        assertTrue(text.contains("generated_at_ms=900"))
        assertTrue(text.contains("bucket=http_500"))
        assertTrue(text.contains("avg=65ms"))
        assertTrue(text.contains("bucket=SocketTimeoutException"))
    }
}
