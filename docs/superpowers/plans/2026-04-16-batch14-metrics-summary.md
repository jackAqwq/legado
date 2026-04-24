# Batch-14 Metrics Summary + Grouped Export Path Implementation Plan

**Goal:** Add per-group metrics summary (`count/avg/p95`) and reduce About export-path overhead through grouped tracker export.

## Task 1: Formatter + Builder Summary Support

**Files**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsExportFormatter.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilder.kt`
- Modify: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsExportFormatterTest.kt`
- Modify: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilderTest.kt`

- [x] Add summary model and summary text block output.
- [x] Add builder-side summary calculation from grouped lines.
- [x] Add tests for summary output, empty-group summary, and p95 case.

## Task 2: Grouped Tracker Export Optimization

**Files**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Modify: `app/src/main/java/io/legado/app/ui/about/AboutFragment.kt`
- Modify: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`

- [x] Add `exportGroupedLines(limit?)` with single snapshot pass.
- [x] Switch About save-log path to grouped export call.
- [x] Add tracker grouped split behavior test.

## Task 3: Verification

- [x] Run:
  - `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.* --tests io.legado.app.ui.rss.read.* --tests io.legado.app.ui.book.read.page.ReadViewTouchBoundsTest -Pksp.incremental=false`
