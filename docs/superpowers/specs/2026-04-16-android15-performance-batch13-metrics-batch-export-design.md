# Android15+ Performance Batch-13 (Grouped Batch Export Naming) Design

## Context

- Batch-10 exported a single `performance_metrics.txt` into `logs.zip`.
- Batch-11/12 improved in-app viewing and filtering, but file-level grouped export was still missing.

## Goal

Upgrade one-click About export to output grouped metrics files with stable naming:

- `performance_metrics_all.txt`
- `performance_metrics_startup.txt`
- `performance_metrics_read.txt`
- `performance_metrics_rss.txt`

## Scope

1. Add pure builder for batch export entries:
   - stable file naming
   - formatted text generation per group
2. Replace single-file About export with directory batch export:
   - generate `performance_metrics/` temp directory
   - write four files
   - include directory in `logs.zip`
3. Add unit tests for builder:
   - filename order/naming
   - empty-group text behavior

## Non-goals

- No change to tracker collection schema.
- No extra UI controls.
- No change to existing log/crash packaging behavior.

## Validation

Run:

- `PerformanceMetricsBatchExportBuilderTest`
- `PerformanceMetricsTrackerTest`
- `PerformanceMetricsSnapshotPresenterTest`
- `PerformanceMetricsExportFormatterTest`
- `RssWebInterceptDeciderTest`
- `RssHtmlHeadInjectorTest`
- `ReadViewTouchBoundsTest`
