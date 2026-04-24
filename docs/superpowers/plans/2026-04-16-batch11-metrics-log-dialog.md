# Batch-11 Metrics Log-Dialog Entry Implementation Plan

**Goal:** Provide a quick in-app view/copy path for performance metrics from the existing log dialog.

## Task 1: Presenter + Tests

**Files**
- Create: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenter.kt`
- Create: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenterTest.kt`

- [x] Add pure presenter method for preview/copy text.
- [x] Add JVM tests for non-empty and empty boundary output.

## Task 2: Menu + Dialog Wiring

**Files**
- Modify: `app/src/main/res/menu/app_log.xml`
- Modify: `app/src/main/java/io/legado/app/ui/about/AppLogDialog.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-zh/strings.xml`

- [x] Add menu actions for view/copy metrics.
- [x] View action shows formatted text in `TextDialog`.
- [x] Copy action writes formatted text to clipboard.
- [x] Add corresponding strings.

## Task 3: Verification

- [x] Run:
  - `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsSnapshotPresenterTest --tests io.legado.app.help.perf.PerformanceMetricsExportFormatterTest --tests io.legado.app.help.perf.PerformanceMetricsTrackerTest --tests io.legado.app.ui.rss.read.* --tests io.legado.app.ui.book.read.page.ReadViewTouchBoundsTest -Pksp.incremental=false`
