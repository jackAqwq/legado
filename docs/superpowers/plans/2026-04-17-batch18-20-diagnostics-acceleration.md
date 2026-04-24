# Batch-18~20 Diagnostics Acceleration Implementation Plan

**Goal:** Ship Batch-18/19/20 diagnostics improvements in one compressed cycle.

## Task 1: Tracker + Presenter (Batch-18 Core)

**Files**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenter.kt`
- Modify: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`
- Modify: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenterTest.kt`

- [x] Add source+result summary model and grouping API.
- [x] Add source+result summary text output.
- [x] Add corresponding JVM tests.

## Task 2: Log Dialog Actions (Batch-19)

**Files**
- Modify: `app/src/main/java/io/legado/app/ui/about/AppLogDialog.kt`
- Modify: `app/src/main/res/menu/app_log.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-zh/strings.xml`

- [x] Add RSS source+result summary menu action.
- [x] Add RSS failure slowest 20 view/copy actions.
- [x] Wire actions through existing presenter/dialog flow.

## Task 3: About Export Extension (Batch-20)

**Files**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilder.kt`
- Modify: `app/src/main/java/io/legado/app/ui/about/AboutFragment.kt`
- Modify: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilderTest.kt`

- [x] Add export entries for source+result summary and failure slowest 20.
- [x] Wire About save-log packaging to include new entries.
- [x] Add/update builder tests for naming and output behavior.

## Task 4: Verification

- [x] Run:
  - `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.* --tests io.legado.app.ui.rss.read.* --tests io.legado.app.ui.book.read.page.ReadViewTouchBoundsTest -Pksp.incremental=false`
