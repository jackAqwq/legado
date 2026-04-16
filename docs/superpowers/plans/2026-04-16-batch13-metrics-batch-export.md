# Batch-13 Metrics Batch Export Implementation Plan

**Goal:** Convert performance metrics export from single-file to grouped multi-file packaging with stable naming.

## Task 1: Batch Builder + Tests

**Files**
- Create: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilder.kt`
- Create: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilderTest.kt`

- [x] Add `buildEntries(all/startup/read/rss)` with fixed file names.
- [x] Reuse existing formatter for each entry.
- [x] Add tests for naming and empty-group hint behavior.

## Task 2: About Export Wiring

**Files**
- Modify: `app/src/main/java/io/legado/app/ui/about/AboutFragment.kt`

- [x] Create temporary `performance_metrics` directory in cache.
- [x] Build and write grouped export files.
- [x] Package directory into `logs.zip`.
- [x] Cleanup temporary directory after packaging.

## Task 3: Verification

- [x] Run:
  - `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsBatchExportBuilderTest --tests io.legado.app.help.perf.* --tests io.legado.app.ui.rss.read.* --tests io.legado.app.ui.book.read.page.ReadViewTouchBoundsTest -Pksp.incremental=false`
