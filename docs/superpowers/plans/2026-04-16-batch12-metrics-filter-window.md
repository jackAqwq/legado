# Batch-12 Metrics Filter/Window/Clear Implementation Plan

**Goal:** Improve in-app metrics operability with grouped viewing, recent-window copy, and cleanup action.

## Task 1: Tracker Selection API + Tests

**Files**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Modify: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`

- [x] Add `exportLines(namePrefix, limit)` selection behavior.
- [x] Add `clearMetrics()` API.
- [x] Add tests for prefix+limit and clear behavior.

## Task 2: Log Dialog Menu Wiring

**Files**
- Modify: `app/src/main/java/io/legado/app/ui/about/AppLogDialog.kt`
- Modify: `app/src/main/res/menu/app_log.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-zh/strings.xml`

- [x] Add grouped view entries (`startup/read/rss`).
- [x] Add copy recent 20 entry.
- [x] Add clear metrics entry.

## Task 3: Verification

- [x] Run:
  - `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.* --tests io.legado.app.ui.rss.read.* --tests io.legado.app.ui.book.read.page.ReadViewTouchBoundsTest -Pksp.incremental=false`
