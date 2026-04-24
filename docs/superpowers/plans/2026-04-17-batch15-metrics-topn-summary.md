# Batch-15 Metrics Top-N + Log Summary Implementation Plan

**Goal:** Add slow-metric Top-N actions and summary visibility in log-dialog metrics operations.

## Task 1: Tracker API + Tests

**Files**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Modify: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`

- [x] Add `exportSlowLines(limit, namePrefix)` for duration-desc Top-N lines.
- [x] Add `buildSummary(namePrefix, limit)` for `count/avg/p95`.
- [x] Add tests for Top-N ordering and summary computation.

## Task 2: Presenter + Log Dialog Wiring

**Files**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenter.kt`
- Modify: `app/src/main/java/io/legado/app/ui/about/AppLogDialog.kt`
- Modify: `app/src/main/res/menu/app_log.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-zh/strings.xml`
- Modify: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenterTest.kt`

- [x] Add optional summary support in snapshot presenter.
- [x] Inject summary into existing log-dialog metrics preview/copy flow.
- [x] Add `view/copy slowest 20` menu actions.
- [x] Add presenter summary rendering test.

## Task 3: Verification

- [x] Run:
  - `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.* --tests io.legado.app.ui.rss.read.* --tests io.legado.app.ui.book.read.page.ReadViewTouchBoundsTest -Pksp.incremental=false`
