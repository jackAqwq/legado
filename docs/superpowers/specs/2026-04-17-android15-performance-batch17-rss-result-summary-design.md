# Android15+ Performance Batch-17 (RSS Result Summary + Result Filter View) Design

## Context

- Batch-16 added RSS source-level summary and source-filtered in-app diagnostics.
- RSS interception metrics still lacked direct result-level (`success` / `failure`) aggregation and filtered viewing.

## Goal

Improve RSS diagnostics with result-aware operations:

1. aggregate RSS metrics summary by `result`
2. support result-filtered RSS metrics view in log dialog

## Scope

1. Extend `PerformanceMetricsTracker`:
   - support `result` filter in export/slow/summary APIs
   - add `buildResultSummaries(namePrefix = "rss.")`
2. Extend `PerformanceMetricsSnapshotPresenter`:
   - add result-summary text builder
3. Extend `AppLogDialog` + menu + strings:
   - add RSS result-summary menu action
   - add filtered RSS view actions (`success` / `failure`)
4. Add JVM tests:
   - result-filtered export behavior
   - result summary grouping/count/avg/p95
   - result summary text rendering

## Non-goals

- No change to metrics collection points.
- No new persistence format.
- No new page/fragment.

## Validation

Run:

- `PerformanceMetricsTrackerTest`
- `PerformanceMetricsSnapshotPresenterTest`
- `PerformanceMetricsBatchExportBuilderTest`
- `PerformanceMetricsExportFormatterTest`
- `RssWebInterceptDeciderTest`
- `RssHtmlHeadInjectorTest`
- `ReadViewTouchBoundsTest`
