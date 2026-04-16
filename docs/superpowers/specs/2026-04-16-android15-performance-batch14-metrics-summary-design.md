# Android15+ Performance Batch-14 (Metrics Summary + Grouped Export Path) Design

## Context

- Batch-13 added grouped export files (`all/startup/read/rss`) in About save-log flow.
- Export content still lacked quick statistical summary, and About export requested tracker data by multiple independent calls.

## Goal

Add low-overhead metrics summary and reduce export-path duplicate work:

1. include `count/avg/p95` summary in each grouped export file
2. provide single-snapshot grouped export API in tracker
3. wire About export to grouped API to avoid repeated snapshot/filter/mapping passes

## Scope

1. Extend perf export formatter:
   - support optional summary block (`summary.count`, `summary.avg_duration_ms`, `summary.p95_duration_ms`)
2. Extend batch export builder:
   - compute summary per group from export lines
   - include summary in each output text file
3. Extend tracker and About export wiring:
   - add `exportGroupedLines(limit?)`
   - replace four independent `exportLines(...)` calls with grouped output in `AboutFragment`
4. Add JVM tests:
   - formatter summary output
   - builder summary computation (including p95 case)
   - tracker grouped split behavior

## Non-goals

- No change to runtime collection schema or sampling points.
- No UI interaction changes.
- No persistence or cross-session aggregation.

## Validation

Run:

- `PerformanceMetricsBatchExportBuilderTest`
- `PerformanceMetricsExportFormatterTest`
- `PerformanceMetricsTrackerTest`
- `PerformanceMetricsSnapshotPresenterTest`
- `RssWebInterceptDeciderTest`
- `RssHtmlHeadInjectorTest`
- `ReadViewTouchBoundsTest`
