# Batch-16 RSS Source Summary + Source Filter View Implementation Plan

**Goal:** Add source-level RSS summary and source-filtered in-app viewing for performance metrics.

## Task 1: Tracker Source-Aware APIs + Tests

**Files**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Modify: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`

- [x] Add optional `source` filter in export/slow/summary APIs.
- [x] Add `buildSourceSummaries(namePrefix = "rss.")` grouped by detail `source`.
- [x] Add tests for source-filtered export and source summary grouping.

## Task 2: Presenter + Log Dialog Source Actions

**Files**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenter.kt`
- Modify: `app/src/main/java/io/legado/app/ui/about/AppLogDialog.kt`
- Modify: `app/src/main/res/menu/app_log.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-zh/strings.xml`
- Modify: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenterTest.kt`

- [x] Add source-summary text builder.
- [x] Add log-dialog action for RSS source summary.
- [x] Add log-dialog source-filtered view actions for `ReadRssActivity` and `BottomWebViewDialog`.
- [x] Add presenter source-summary rendering test.

## Task 3: Verification

- [x] Run:
  - `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.* --tests io.legado.app.ui.rss.read.* --tests io.legado.app.ui.book.read.page.ReadViewTouchBoundsTest -Pksp.incremental=false`
