# Android15+ Performance Batch-12 (Metrics Filter/Window/Clear) Design

## Context

- Batch-11 added quick view/copy entry in `AppLogDialog`.
- Metrics operations still lacked grouping/time-window selection and explicit in-memory cleanup.

## Goal

Add low-risk selective operations for metrics:

1. grouped view (`startup`, `read`, `rss`)
2. windowed export (`recent 20`)
3. explicit metrics cleanup

## Scope

1. Extend `PerformanceMetricsTracker`:
   - `exportLines(namePrefix, limit)`
   - `clearMetrics()`
2. Extend `AppLogDialog` toolbar menu:
   - view startup/read/rss metrics
   - copy recent 20 metrics
   - clear performance metrics
3. Add tracker unit tests for:
   - prefix + limit combination
   - clear behavior

## Non-goals

- No persistence changes.
- No new screen or heavy interaction flow.
- No metric schema changes.

## Validation

Run:

- `PerformanceMetricsTrackerTest`
- `PerformanceMetricsSnapshotPresenterTest`
- `PerformanceMetricsExportFormatterTest`
- `RssWebInterceptDeciderTest`
- `RssHtmlHeadInjectorTest`
- `ReadViewTouchBoundsTest`
