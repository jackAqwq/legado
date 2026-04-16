# Batch-21~30 Parallel Optimization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver ten low-risk, high-yield internal optimization batches across RSS diagnostics, startup scheduling, and reading-path cleanup.

**Architecture:** Start with foundational metrics/detail changes that unblock richer diagnostics, then execute disjoint helper extractions and scheduler cleanups in parallel-safe slices. Keep user-visible behavior stable by routing new behavior through existing tracker, dialog, export, startup, and reader entry points.

**Tech Stack:** Kotlin, Android app module, JVM unit tests, existing `PerformanceMetricsTracker`/reader/WebView infrastructure.

---

### Task 1: Write Batch-21~30 Design Artifacts

**Files:**
- Create: `docs/superpowers/specs/2026-04-17-android15-performance-batch21-30-parallel-optimization-design.md`
- Create: `docs/superpowers/plans/2026-04-17-batch21-30-parallel-optimization.md`

- [ ] Step 1: Save the approved design document.
- [ ] Step 2: Save this implementation plan.
- [ ] Step 3: Keep both docs in the final commit for traceability.

Run: `git -C D:\legado diff -- docs/superpowers/specs docs/superpowers/plans`
Expected: New design/plan files for Batch-21~30 are present.

### Task 2: Batch-21 Foundation - Structured Metric Details

**Files:**
- Create: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricDetails.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`

- [ ] Step 1: Write failing tests for detail encoding/decoding, filtering, and grouping using structured details.
- [ ] Step 2: Implement the detail model plus tracker integration while preserving line export compatibility.
- [ ] Step 3: Re-run the targeted perf tracker tests.

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsTrackerTest -Pksp.incremental=false`
Expected: PASS with tracker tests covering structured detail behavior.

### Task 3: Batch-22 RSS Failure Enrichment

**Files:**
- Modify: `app/src/main/java/io/legado/app/ui/rss/read/ReadRssActivity.kt`
- Modify: `app/src/main/java/io/legado/app/ui/widget/dialog/BottomWebViewDialog.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`

- [ ] Step 1: Add failing tests for richer RSS failure detail aggregation.
- [ ] Step 2: Capture failure type/status context from both RSS interception call sites without changing request handling.
- [ ] Step 3: Re-run tracker and RSS tests.

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsTrackerTest --tests io.legado.app.ui.rss.read.* -Pksp.incremental=false`
Expected: PASS with enriched RSS detail coverage.

### Task 4: Batch-23 RSS Failure Summary UI/Export

**Files:**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenter.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilder.kt`
- Modify: `app/src/main/java/io/legado/app/ui/about/AppLogDialog.kt`
- Modify: `app/src/main/java/io/legado/app/ui/about/AboutFragment.kt`
- Modify: `app/src/main/res/menu/app_log.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-zh/strings.xml`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenterTest.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilderTest.kt`

- [ ] Step 1: Add failing tests for failure-cause summary text/export output.
- [ ] Step 2: Extend tracker consumers, log dialog, and export builder via existing entry points only.
- [ ] Step 3: Re-run perf presenter/export tests.

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsSnapshotPresenterTest --tests io.legado.app.help.perf.PerformanceMetricsBatchExportBuilderTest -Pksp.incremental=false`
Expected: PASS with failure-cause summary coverage.

### Task 5: Batch-24 Shared RSS Injected Page Fetcher

**Files:**
- Create: `app/src/main/java/io/legado/app/help/webView/RssInjectedPageFetcher.kt`
- Modify: `app/src/main/java/io/legado/app/ui/rss/read/ReadRssActivity.kt`
- Modify: `app/src/main/java/io/legado/app/ui/widget/dialog/BottomWebViewDialog.kt`
- Test: `app/src/test/java/io/legado/app/ui/rss/read/RssInjectedPageFetcherTest.kt`

- [ ] Step 1: Add failing tests for shared cookie/header/html injection behavior.
- [ ] Step 2: Extract common rewrite flow into the helper and rewire both call sites.
- [ ] Step 3: Re-run RSS helper tests.

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.rss.read.* -Pksp.incremental=false`
Expected: PASS with shared fetcher behavior covered.

### Task 6: Batch-25 Startup Stage Metrics

**Files:**
- Modify: `app/src/main/java/io/legado/app/App.kt`
- Modify: `app/src/main/java/io/legado/app/ui/main/MainActivity.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`

- [ ] Step 1: Add failing tests for staged startup timing recording.
- [ ] Step 2: Record additional startup boundaries while preserving current startup readiness reporting.
- [ ] Step 3: Re-run tracker tests.

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsTrackerTest -Pksp.incremental=false`
Expected: PASS with staged startup metrics.

### Task 7: Batch-26 Main Startup Task Planner

**Files:**
- Create: `app/src/main/java/io/legado/app/ui/main/MainStartupTaskPlanner.kt`
- Modify: `app/src/main/java/io/legado/app/ui/main/MainActivity.kt`
- Test: `app/src/test/java/io/legado/app/ui/main/MainStartupTaskPlannerTest.kt`

- [ ] Step 1: Add failing tests for task ordering and delay selection.
- [ ] Step 2: Move delayed startup follow-up scheduling decisions into the planner and keep execution in `MainActivity`.
- [ ] Step 3: Re-run planner tests.

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.main.MainStartupTaskPlannerTest -Pksp.incremental=false`
Expected: PASS with deterministic planner behavior.

### Task 8: Batch-27 App Startup Policy

**Files:**
- Create: `app/src/main/java/io/legado/app/AppStartupPolicy.kt`
- Modify: `app/src/main/java/io/legado/app/App.kt`
- Test: `app/src/test/java/io/legado/app/AppStartupPolicyTest.kt`

- [ ] Step 1: Add failing tests for startup guard rules.
- [ ] Step 2: Move app-level deferred/background gate logic into a policy helper without changing thresholds.
- [ ] Step 3: Re-run policy tests.

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.AppStartupPolicyTest -Pksp.incremental=false`
Expected: PASS with startup policy coverage.

### Task 9: Batch-28 Read Metric Lifecycle Fix

**Files:**
- Modify: `app/src/main/java/io/legado/app/ui/book/read/page/ReadView.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`

- [ ] Step 1: Add failing tests for abandoned/cancelled page-flip metrics.
- [ ] Step 2: Tighten metric start/finish/reset behavior around actual flip lifecycle.
- [ ] Step 3: Re-run tracker and read tests.

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsTrackerTest --tests io.legado.app.ui.book.read.page.* -Pksp.incremental=false`
Expected: PASS with read metric lifecycle coverage.

### Task 10: Batch-29 Render Invalidation Gate

**Files:**
- Create: `app/src/main/java/io/legado/app/ui/book/read/page/RenderInvalidateGate.kt`
- Modify: `app/src/main/java/io/legado/app/ui/book/read/page/ContentTextView.kt`
- Modify: `app/src/main/java/io/legado/app/ui/book/read/page/delegate/PageDelegate.kt`
- Test: `app/src/test/java/io/legado/app/ui/book/read/page/RenderInvalidateGateTest.kt`

- [ ] Step 1: Add failing tests for redundant invalidate suppression rules.
- [ ] Step 2: Route pre-render/animation invalidation decisions through the helper.
- [ ] Step 3: Re-run render gate tests.

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.book.read.page.RenderInvalidateGateTest -Pksp.incremental=false`
Expected: PASS with gated invalidation behavior.

### Task 11: Batch-30 HTML Justification Helper

**Files:**
- Create: `app/src/main/java/io/legado/app/ui/book/read/page/provider/HtmlLineJustifier.kt`
- Modify: `app/src/main/java/io/legado/app/ui/book/read/page/provider/TextChapterLayout.kt`
- Test: `app/src/test/java/io/legado/app/ui/book/read/page/provider/HtmlLineJustifierTest.kt`

- [ ] Step 1: Add failing tests for spacing distribution and edge cases.
- [ ] Step 2: Extract justification math into the helper and keep layout output stable.
- [ ] Step 3: Re-run provider tests.

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.book.read.page.provider.* -Pksp.incremental=false`
Expected: PASS with HTML justification helper coverage.

### Task 12: Combined Verification

**Files:**
- Modify: only files touched by Tasks 2-11

- [ ] Step 1: Run the combined perf/RSS/read/main unit test slice.
- [ ] Step 2: Review changed files and git diff for accidental scope drift.
- [ ] Step 3: Commit, push, and track GitHub Actions to success before reporting back.

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.* --tests io.legado.app.ui.rss.read.* --tests io.legado.app.ui.book.read.page.* --tests io.legado.app.ui.main.* --tests io.legado.app.AppStartupPolicyTest -Pksp.incremental=false`
Expected: PASS with all targeted batch coverage green.
