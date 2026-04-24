# Batch-9 Performance Metrics Baseline Implementation Plan

**Goal:** Build a lightweight, behavior-neutral latency sampling layer for startup/page-flip/RSS interception with a runtime switch.

**Architecture:** Introduce `PerformanceMetricsTracker` (bounded in-memory records + export lines) and wire minimal timing hooks at app/read/rss boundaries.

**Tech Stack:** Kotlin, Android (`SystemClock.elapsedRealtime`), JUnit4.

---

### Task 1: Tracker + Unit Tests

**Files:**
- Create: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Create: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`

- [x] Add tracker with ring buffer and export lines API.
- [x] Add tests for enable/disable behavior, startup/page-flip metrics, ring-buffer bound, and export output.

### Task 2: Settings Switch

**Files:**
- Modify: `app/src/main/java/io/legado/app/constant/PreferKey.kt`
- Modify: `app/src/main/java/io/legado/app/help/config/AppConfig.kt`
- Modify: `app/src/main/res/xml/pref_config_other.xml`
- Modify: `app/src/main/res/values/strings.xml`

- [x] Add `performanceMetrics` preference key.
- [x] Add `AppConfig.recordPerformanceMetrics` binding.
- [x] Add settings switch entry (default off).
- [x] Add title/summary strings.

### Task 3: Startup + Page Flip Hooks

**Files:**
- Modify: `app/src/main/java/io/legado/app/App.kt`
- Modify: `app/src/main/java/io/legado/app/ui/main/MainActivity.kt`
- Modify: `app/src/main/java/io/legado/app/ui/book/read/page/ReadView.kt`

- [x] Mark app create start in `App.onCreate`.
- [x] Mark main UI ready in `MainActivity.onPostCreate`.
- [x] Mark page-flip gesture start and completion in `ReadView`.

### Task 4: RSS Interception Hooks + Verification

**Files:**
- Modify: `app/src/main/java/io/legado/app/ui/rss/read/ReadRssActivity.kt`
- Modify: `app/src/main/java/io/legado/app/ui/widget/dialog/BottomWebViewDialog.kt`

- [x] Record interception duration (success/failure) in both rewrite paths.
- [x] Run verification:
  - `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsTrackerTest --tests io.legado.app.ui.rss.read.* --tests io.legado.app.ui.book.read.page.ReadViewTouchBoundsTest -Pksp.incremental=false`

