# Android15+ Performance Batch-11 (Log Dialog View/Copy Entry) Design

## Context

- Batch-9 added runtime performance metrics collection.
- Batch-10 added export into `logs.zip`.
- In-app quick inspection path was still missing.

## Goal

Add instant "view" and "copy" actions for performance metrics in `AppLogDialog` without changing existing log behavior.

## Scope

1. Extend `app_log` toolbar menu with:
   - view performance metrics
   - copy performance metrics
2. Add a pure presenter helper for display/copy text assembly:
   - uses existing export formatter
3. Wire `AppLogDialog` actions:
   - view -> open `TextDialog`
   - copy -> copy full text to clipboard
4. Add JVM tests for presenter:
   - non-empty lines
   - empty lines hint

## Non-goals

- No new page/activity for metrics.
- No tracker logic changes.
- No network or storage behavior changes.

## Validation

Run:

- `PerformanceMetricsSnapshotPresenterTest`
- `PerformanceMetricsExportFormatterTest`
- `PerformanceMetricsTrackerTest`
- `RssWebInterceptDeciderTest`
- `RssHtmlHeadInjectorTest`
- `ReadViewTouchBoundsTest`
