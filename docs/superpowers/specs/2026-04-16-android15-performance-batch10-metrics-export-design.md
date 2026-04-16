# Android15+ Performance Batch-10 (Metrics Export Entry) Design

## Context

- Batch-9 introduced `PerformanceMetricsTracker` and runtime switch.
- No user-facing export path existed for collected metric samples.

## Goal

Expose a behavior-neutral export entry by including performance metrics text in the existing About-page log export package.

## Scope

1. Add formatter for export text:
   - stable header
   - generation timestamp
   - empty-data hint
2. Extend About-page `saveLog` packaging:
   - generate `performance_metrics.txt` in cache
   - include file in `logs.zip`
3. Add JVM tests for:
   - non-empty export format
   - empty export boundary

## Non-goals

- No new external sharing flow.
- No UI redesign for a separate metrics screen.
- No tracker logic change.

## Design Decisions

1. Reuse existing export entry (`AboutFragment.saveLog`) to avoid new UX complexity.
2. Keep formatter pure (`PerformanceMetricsExportFormatter`) for fast JVM tests.
3. Preserve behavior when metrics are disabled or empty by always exporting a readable text file with explicit empty hint.

## Validation

Run:

- `PerformanceMetricsExportFormatterTest`
- `PerformanceMetricsTrackerTest`
- `RssWebInterceptDeciderTest`
- `RssHtmlHeadInjectorTest`
- `ReadViewTouchBoundsTest`
