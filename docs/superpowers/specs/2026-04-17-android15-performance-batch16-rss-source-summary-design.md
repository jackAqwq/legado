# Android15+ Performance Batch-16 (RSS Source Summary + Source Filter View) Design

## Context

- Batch-15 added slow Top-N operations and summary visibility in log-dialog metrics view/copy.
- RSS metrics are collected from multiple interception sources (`ReadRssActivity`, `BottomWebViewDialog`), but in-app diagnostics still lacked source-level aggregation and direct source-filtered viewing.

## Goal

Improve RSS performance diagnostics with source-aware operations:

1. aggregate RSS metrics summary by `source`
2. support source-filtered metrics view in log dialog

## Scope

1. Extend `PerformanceMetricsTracker`:
   - support `source` filter in export/slow/summary APIs
   - add `buildSourceSummaries(namePrefix = "rss.")` grouped by details `source=...`
2. Extend `PerformanceMetricsSnapshotPresenter`:
   - add source-summary text builder for dialog display
3. Extend `AppLogDialog` + menu + strings:
   - add RSS source-summary menu action
   - add source-filtered RSS view actions (`ReadRssActivity` / `BottomWebViewDialog`)
4. Add JVM tests:
   - source-filtered export behavior
   - source summary grouping/count/avg/p95
   - source summary text rendering

## Non-goals

- No change to metrics collection schema/storage.
- No new page/fragment.
- No persistent history aggregation.

## Validation

Run:

- `PerformanceMetricsTrackerTest`
- `PerformanceMetricsSnapshotPresenterTest`
- `PerformanceMetricsBatchExportBuilderTest`
- `PerformanceMetricsExportFormatterTest`
- `RssWebInterceptDeciderTest`
- `RssHtmlHeadInjectorTest`
- `ReadViewTouchBoundsTest`
