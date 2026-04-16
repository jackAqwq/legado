# Android15+ Performance Batch-15 (Slow Top-N + Log Summary) Design

## Context

- Batch-14 added grouped export summary (`count/avg/p95`) in About save-log output.
- Log dialog metrics actions still focused on grouped and recent-window views, lacking direct slow-metric Top-N operations.

## Goal

Improve in-app performance observability with low-risk log-dialog extensions:

1. add slow-metric Top-N view/copy operations (`top 20`)
2. add summary (`count/avg/p95`) to existing metrics preview/copy text in log dialog

## Scope

1. Extend `PerformanceMetricsTracker`:
   - `exportSlowLines(limit, namePrefix)` for duration-desc Top-N lines
   - `buildSummary(namePrefix, limit)` for in-memory summary generation
2. Extend `PerformanceMetricsSnapshotPresenter`:
   - support optional summary injection into preview/copy text
3. Extend `AppLogDialog` + menu + strings:
   - add `view slowest 20` and `copy slowest 20` actions
   - inject summary in existing metrics preview/copy path (all/startup/read/rss/recent)
4. Add JVM tests:
   - slow-line ordering and Top-N behavior
   - summary computation
   - presenter summary rendering

## Non-goals

- No schema/storage changes for metrics collection.
- No new fragment/page.
- No metrics persistence or long-term aggregation.

## Validation

Run:

- `PerformanceMetricsTrackerTest`
- `PerformanceMetricsSnapshotPresenterTest`
- `PerformanceMetricsExportFormatterTest`
- `PerformanceMetricsBatchExportBuilderTest`
- `RssWebInterceptDeciderTest`
- `RssHtmlHeadInjectorTest`
- `ReadViewTouchBoundsTest`
