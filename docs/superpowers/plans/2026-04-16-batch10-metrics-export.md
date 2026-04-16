# Batch-10 Metrics Export Implementation Plan

**Goal:** Land a practical export entry for performance metrics using existing About-page log export flow.

## Task 1: Formatter + Tests

**Files**
- Create: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsExportFormatter.kt`
- Create: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsExportFormatterTest.kt`

- [x] Add pure formatter producing header, timestamp, and body.
- [x] Add empty-data hint output.
- [x] Add JVM tests for non-empty and empty cases.

## Task 2: Wire About Export

**Files**
- Modify: `app/src/main/java/io/legado/app/ui/about/AboutFragment.kt`

- [x] Generate `performance_metrics.txt` from tracker export lines.
- [x] Include metrics text file into `logs.zip`.
- [x] Keep failure path non-fatal and log errors via `AppLog`.

## Task 3: Verification

- [x] Run:
  - `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsExportFormatterTest --tests io.legado.app.help.perf.PerformanceMetricsTrackerTest --tests io.legado.app.ui.rss.read.* --tests io.legado.app.ui.book.read.page.ReadViewTouchBoundsTest -Pksp.incremental=false`
