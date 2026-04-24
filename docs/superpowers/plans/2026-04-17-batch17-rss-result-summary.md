# Batch-17 RSS Result Summary + Result Filter View Implementation Plan

**Goal:** Add result-level RSS summary and success/failure-filtered in-app metrics viewing.

## Task 1: Tracker Result-Aware APIs + Tests

**Files**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Modify: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`

- [x] Add optional `result` filter in export/slow/summary APIs.
- [x] Add `buildResultSummaries(namePrefix = "rss.")`.
- [x] Add tests for result-filtered export and result summary grouping.

## Task 2: Presenter + Log Dialog Result Actions

**Files**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenter.kt`
- Modify: `app/src/main/java/io/legado/app/ui/about/AppLogDialog.kt`
- Modify: `app/src/main/res/menu/app_log.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-zh/strings.xml`
- Modify: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenterTest.kt`

- [x] Add result-summary text builder.
- [x] Add RSS result-summary menu action.
- [x] Add RSS success/failure filtered view actions.
- [x] Add presenter result-summary rendering test.

## Task 3: Verification

- [x] Run:
  - `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.* --tests io.legado.app.ui.rss.read.* --tests io.legado.app.ui.book.read.page.ReadViewTouchBoundsTest -Pksp.incremental=false`
