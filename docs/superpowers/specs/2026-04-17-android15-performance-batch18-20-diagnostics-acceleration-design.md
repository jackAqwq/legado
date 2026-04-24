# Android15+ Performance Batch-18~20 (Diagnostics Acceleration) Design

## Context

- Batch-16/17 added RSS source-level and result-level diagnostics in log dialog.
- To compress delivery cycle, the next slices are combined into one accelerated rollout.

## Goal

Deliver three diagnostics slices in one integration cycle:

1. Batch-18: source+result two-dimensional RSS summary
2. Batch-19: failure-focused slow Top-20 view/copy in log dialog
3. Batch-20: include source+result summary + failure slow Top-20 in About export package

## Scope

1. Tracker capabilities:
   - add `buildSourceResultSummaries(namePrefix = "rss.")`
   - add `exportSourceResultSummaryLines(namePrefix = "rss.")`
   - keep source/result filter combinability for export/slow/summary APIs
2. Presenter capabilities:
   - add source+result summary text builder
3. AppLogDialog capabilities:
   - add menu action: RSS source+result summary
   - add menu actions: RSS failure slowest 20 view/copy
4. About export capabilities:
   - extend batch export builder with two additional files:
     - `performance_metrics_rss_source_result_summary.txt`
     - `performance_metrics_rss_failure_slowest_20.txt`
   - wire About save-log export to include the two files
5. JVM tests:
   - tracker: source+result grouping
   - presenter: source+result summary text
   - batch export builder: file naming/count + content expectations for new files

## Non-goals

- No change to metric collection points.
- No new persistence store.
- No new UI screen/fragment beyond existing log dialog menu actions.

## Validation

Run:

- `PerformanceMetricsTrackerTest`
- `PerformanceMetricsSnapshotPresenterTest`
- `PerformanceMetricsBatchExportBuilderTest`
- `PerformanceMetricsExportFormatterTest`
- `RssWebInterceptDeciderTest`
- `RssHtmlHeadInjectorTest`
- `ReadViewTouchBoundsTest`
