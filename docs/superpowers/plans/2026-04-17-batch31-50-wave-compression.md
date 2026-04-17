# Batch-31~50 Wave Compression Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver Batch-31~50 as one balanced startup/read/RSS optimization wave with a shared metrics query foundation, richer summaries, moderate runtime cleanup, and consolidated diagnostics surfaces.

**Architecture:** Execute foundational tracker/query work first, then advance startup, reading, and RSS slices in dependency order, and finally consolidate log/export entry points so all new diagnostics ride on shared tracker outputs instead of UI-local logic.

**Tech Stack:** Kotlin, Android app module, JVM unit tests, existing `PerformanceMetricsTracker` / `PerformanceMetricsSnapshotPresenter` / `PerformanceMetricsBatchExportBuilder` / startup helpers / reader helpers / RSS interception helpers.

---

## File Map

### Core metrics files

- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricDetails.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenter.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilder.kt`
- Create: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsQuery.kt`

### Startup files

- Modify: `app/src/main/java/io/legado/app/App.kt`
- Modify: `app/src/main/java/io/legado/app/AppStartupPolicy.kt`
- Modify: `app/src/main/java/io/legado/app/ui/main/MainActivity.kt`
- Modify: `app/src/main/java/io/legado/app/ui/main/MainStartupTaskPlanner.kt`

### Reading files

- Modify: `app/src/main/java/io/legado/app/ui/book/read/page/ReadView.kt`
- Modify: `app/src/main/java/io/legado/app/ui/book/read/page/RenderInvalidateGate.kt`
- Modify: `app/src/main/java/io/legado/app/ui/book/read/page/ContentTextView.kt`
- Modify: `app/src/main/java/io/legado/app/ui/book/read/page/delegate/PageDelegate.kt`

### RSS files

- Modify: `app/src/main/java/io/legado/app/help/webView/RssInjectedPageFetcher.kt`
- Modify: `app/src/main/java/io/legado/app/ui/rss/read/ReadRssActivity.kt`
- Modify: `app/src/main/java/io/legado/app/ui/widget/dialog/BottomWebViewDialog.kt`

### Diagnostics surface files

- Modify: `app/src/main/java/io/legado/app/ui/about/AppLogDialog.kt`
- Modify: `app/src/main/java/io/legado/app/ui/about/AboutFragment.kt`
- Modify: `app/src/main/res/menu/app_log.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-zh/strings.xml`

### Test files

- Modify: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`
- Modify: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenterTest.kt`
- Modify: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilderTest.kt`
- Modify: `app/src/test/java/io/legado/app/ui/main/MainStartupTaskPlannerTest.kt`
- Modify: `app/src/test/java/io/legado/app/AppStartupPolicyTest.kt`
- Modify: `app/src/test/java/io/legado/app/ui/book/read/page/RenderInvalidateGateTest.kt`
- Modify: `app/src/test/java/io/legado/app/ui/rss/read/RssInjectedPageFetcherTest.kt`

### Plan artifacts

- Create: `docs/superpowers/plans/2026-04-17-batch31-50-wave-compression.md`
- Reference: `docs/superpowers/specs/2026-04-17-android15-performance-batch31-50-wave-design.md`

---

### Task 1: Save the Batch-31~50 implementation plan

**Files:**
- Create: `docs/superpowers/plans/2026-04-17-batch31-50-wave-compression.md`
- Reference: `docs/superpowers/specs/2026-04-17-android15-performance-batch31-50-wave-design.md`

- [ ] Step 1: Save this approved implementation plan next to the spec.
- [ ] Step 2: Keep the plan scoped to the existing startup/read/RSS/metrics architecture.
- [ ] Step 3: Keep the plan document in the final branch so execution history remains traceable.

Run: `git -C D:\legado diff -- docs/superpowers/specs/2026-04-17-android15-performance-batch31-50-wave-design.md docs/superpowers/plans/2026-04-17-batch31-50-wave-compression.md`
Expected: The spec is unchanged and the new Batch-31~50 plan file is present.

### Task 2: Batch-34 foundation - shared metrics query/filter layer

**Files:**
- Create: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsQuery.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`

- [ ] Step 1: Add failing tracker tests for reusable selection dimensions: `namePrefix`, `source`, `result`, `failureBucket`, `stage`, `group`, `limit`, and `maxAgeMs`.

```kotlin
@Test
fun export_lines_should_support_stage_group_and_max_age_filters()
```

- [ ] Step 2: Introduce a reusable query object and migrate tracker selectors to use it instead of extending method signatures one-by-one.

```kotlin
internal data class PerformanceMetricsQuery(
    val namePrefix: String? = null,
    val source: String? = null,
    val result: String? = null,
    val failureBucket: String? = null,
    val stage: String? = null,
    val group: String? = null,
    val limit: Int? = null,
    val maxAgeMs: Long? = null
)
```

- [ ] Step 3: Keep existing public tracker helpers working by routing them through the new query layer rather than changing callers all at once.
- [ ] Step 4: Re-run the tracker suite and confirm legacy filters still pass.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsTrackerTest -Pksp.incremental=false`
Expected: PASS with query-based filtering, existing summaries, and export line compatibility covered.

### Task 3: Batch-31 startup stage summaries

**Files:**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenter.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenterTest.kt`

- [ ] Step 1: Add failing tests for startup summaries grouped by `stage`.

```kotlin
@Test
fun build_stage_summaries_should_group_startup_metrics_by_stage()
```

- [ ] Step 2: Add tracker-side stage summary builders/export lines and presenter-side text builders using the structured `stage` detail instead of parsing metric names in the UI.
- [ ] Step 3: Keep `startup.main_ui_ready` and other current export lines unchanged.
- [ ] Step 4: Re-run perf tracker and presenter tests.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsTrackerTest --tests io.legado.app.help.perf.PerformanceMetricsSnapshotPresenterTest -Pksp.incremental=false`
Expected: PASS with startup stage aggregation text and summary ordering covered.

### Task 4: Batch-32 RSS source+bucket summaries

**Files:**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenter.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilder.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenterTest.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilderTest.kt`

- [ ] Step 1: Add failing tests for RSS failure summaries grouped by `source + failure bucket`.
- [ ] Step 2: Implement tracker aggregations that emit lines like `source=ReadRssActivity|bucket=http_500|count=...`.
- [ ] Step 3: Extend presenter/export builder support for the new grouped summary lines.
- [ ] Step 4: Re-run perf tracker/presenter/export tests.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsTrackerTest --tests io.legado.app.help.perf.PerformanceMetricsSnapshotPresenterTest --tests io.legado.app.help.perf.PerformanceMetricsBatchExportBuilderTest -Pksp.incremental=false`
Expected: PASS with RSS source+bucket summary lines and export payloads covered.

### Task 5: Batch-33 read flip detail enrichment

**Files:**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Modify: `app/src/main/java/io/legado/app/ui/book/read/page/ReadView.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`

- [ ] Step 1: Add failing tracker tests for read flip details carrying `direction`, `anim`, `result`, and `cancelReason`.

```kotlin
@Test
fun record_page_flip_should_preserve_direction_anim_and_cancel_reason_details()
```

- [ ] Step 2: Extend read-page metric recording so successful and cancelled paths both write stable detail keys through the tracker.
- [ ] Step 3: Keep current timing semantics intact while enriching the recorded details.
- [ ] Step 4: Re-run tracker-focused tests.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsTrackerTest -Pksp.incremental=false`
Expected: PASS with read flip details preserved for both success and cancellation cases.

### Task 6: Batch-35 cross-domain overview snapshot

**Files:**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenter.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilder.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenterTest.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilderTest.kt`

- [ ] Step 1: Add failing tests for an overview snapshot that summarizes startup, read, and RSS in one payload.
- [ ] Step 2: Implement tracker-side overview summary generation without duplicating per-domain aggregation logic.
- [ ] Step 3: Add presenter/export text builders for overview output and keep empty-state behavior intact.
- [ ] Step 4: Re-run tracker/presenter/export tests before moving into startup work.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsTrackerTest --tests io.legado.app.help.perf.PerformanceMetricsSnapshotPresenterTest --tests io.legado.app.help.perf.PerformanceMetricsBatchExportBuilderTest -Pksp.incremental=false`
Expected: PASS with overview text/export behavior covered.

### Task 7: Batch-36 startup task phase/group model

**Files:**
- Modify: `app/src/main/java/io/legado/app/ui/main/MainStartupTaskPlanner.kt`
- Modify: `app/src/main/java/io/legado/app/ui/main/MainActivity.kt`
- Test: `app/src/test/java/io/legado/app/ui/main/MainStartupTaskPlannerTest.kt`

- [ ] Step 1: Add failing planner tests for richer task metadata such as `phase`, `group`, and `label`.

```kotlin
internal data class MainStartupTask(
    val type: MainStartupTaskType,
    val delayMs: Long,
    val phase: String,
    val group: String
)
```

- [ ] Step 2: Extend the planner to emit richer task metadata while keeping execution ownership in `MainActivity`.
- [ ] Step 3: Thread the richer metadata through startup metric recording points without changing which tasks actually run.
- [ ] Step 4: Re-run startup planner tests.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.main.MainStartupTaskPlannerTest -Pksp.incremental=false`
Expected: PASS with deterministic phase/group metadata and current task ordering preserved.

### Task 8: Batch-37 startup task deferral optimization

**Files:**
- Modify: `app/src/main/java/io/legado/app/ui/main/MainStartupTaskPlanner.kt`
- Modify: `app/src/main/java/io/legado/app/ui/main/MainActivity.kt`
- Test: `app/src/test/java/io/legado/app/ui/main/MainStartupTaskPlannerTest.kt`

- [ ] Step 1: Add failing tests that describe which tasks are allowed to move into warmer/idle windows and which must stay early.
- [ ] Step 2: Adjust planner delays/grouping so lower-priority startup work moves later without dropping any task.
- [ ] Step 3: Keep `MainActivity` execution thin by consuming the planner result instead of hardcoding timing logic inline.
- [ ] Step 4: Re-run planner tests and inspect the resulting startup delay values in code review.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.main.MainStartupTaskPlannerTest -Pksp.incremental=false`
Expected: PASS with explicit deferral rules and no dropped startup actions.

### Task 9: Batch-38 context-aware startup policy

**Files:**
- Modify: `app/src/main/java/io/legado/app/AppStartupPolicy.kt`
- Modify: `app/src/main/java/io/legado/app/App.kt`
- Test: `app/src/test/java/io/legado/app/AppStartupPolicyTest.kt`

- [ ] Step 1: Add failing tests for context-aware policy decisions such as first-open / upgrade / normal-start scenarios.
- [ ] Step 2: Extend `AppStartupPolicy` with small, explicit decision helpers instead of adding more inline conditionals to `App.kt`.
- [ ] Step 3: Rewire `App.kt` to consume the policy helper while keeping current thresholds/intent stable.
- [ ] Step 4: Re-run policy tests.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.AppStartupPolicyTest -Pksp.incremental=false`
Expected: PASS with policy branches covered and no behavior hidden in `App.kt`.

### Task 10: Batches 39-40 startup log/export summaries and producer cleanup

**Files:**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenter.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilder.kt`
- Modify: `app/src/main/java/io/legado/app/ui/about/AppLogDialog.kt`
- Modify: `app/src/main/java/io/legado/app/ui/about/AboutFragment.kt`
- Modify: `app/src/main/java/io/legado/app/App.kt`
- Modify: `app/src/main/java/io/legado/app/ui/main/MainActivity.kt`
- Modify: `app/src/main/res/menu/app_log.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-zh/strings.xml`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenterTest.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilderTest.kt`

- [ ] Step 1: Add failing tests for startup phase/group summaries and recent-window startup exports.
- [ ] Step 2: Normalize startup metric producer names/details in `App.kt` and `MainActivity` so diagnostics group cleanly by stage/phase/group.
- [ ] Step 3: Expose startup phase/group views through the existing log dialog and export pipeline.
- [ ] Step 4: Re-run perf tracker/presenter/export tests for startup summaries.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsTrackerTest --tests io.legado.app.help.perf.PerformanceMetricsSnapshotPresenterTest --tests io.legado.app.help.perf.PerformanceMetricsBatchExportBuilderTest --tests io.legado.app.ui.main.MainStartupTaskPlannerTest --tests io.legado.app.AppStartupPolicyTest -Pksp.incremental=false`
Expected: PASS with startup summaries, producer naming, planner metadata, and export text aligned.

### Task 11: Batch-41 read lifecycle result refinement

**Files:**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Modify: `app/src/main/java/io/legado/app/ui/book/read/page/ReadView.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`

- [ ] Step 1: Add failing tests for distinct read results such as `completed`, `cancelled`, and `aborted`.
- [ ] Step 2: Update `ReadView` to record stable result/cancel-reason details on all terminal paths instead of collapsing them to one cancel call.
- [ ] Step 3: Keep the flip timing lifecycle intact and avoid adding UI-only parsing logic.
- [ ] Step 4: Re-run tracker tests.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsTrackerTest -Pksp.incremental=false`
Expected: PASS with read lifecycle outcomes and cancel reasons covered.

### Task 12: Batch-43 read invalidation batching cleanup

**Files:**
- Modify: `app/src/main/java/io/legado/app/ui/book/read/page/RenderInvalidateGate.kt`
- Modify: `app/src/main/java/io/legado/app/ui/book/read/page/ContentTextView.kt`
- Modify: `app/src/main/java/io/legado/app/ui/book/read/page/delegate/PageDelegate.kt`
- Test: `app/src/test/java/io/legado/app/ui/book/read/page/RenderInvalidateGateTest.kt`

- [ ] Step 1: Add failing gate tests for batched invalidation decisions across pre-render, content update, and delegate redraw paths.
- [ ] Step 2: Extend `RenderInvalidateGate` so it can express “batch now / defer / skip” decisions without scattering invalidate conditions across view classes.
- [ ] Step 3: Rewire `ContentTextView` and `PageDelegate` to honor the gate rather than layering direct `invalidate()` calls.
- [ ] Step 4: Re-run read gate tests.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.book.read.page.RenderInvalidateGateTest -Pksp.incremental=false`
Expected: PASS with duplicate redraw suppression and delegate invalidation rules covered.

### Task 13: Batch-44 read phase metrics

**Files:**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Modify: `app/src/main/java/io/legado/app/ui/book/read/page/ReadView.kt`
- Modify: `app/src/main/java/io/legado/app/ui/book/read/page/ContentTextView.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`

- [ ] Step 1: Add failing tests for read phase metrics such as `layout`, `pre_render`, and `invalidate`.
- [ ] Step 2: Add tracker helpers for read phase recording and wire stable producer boundaries in `ReadView` / `ContentTextView`.
- [ ] Step 3: Keep metric names and detail keys normalized so later summaries do not parse raw text.
- [ ] Step 4: Re-run tracker tests.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsTrackerTest -Pksp.incremental=false`
Expected: PASS with read phase metric emission covered.

### Task 14: Batches 42 and 45 read summaries, Top-N, and export wiring

**Files:**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenter.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilder.kt`
- Modify: `app/src/main/java/io/legado/app/ui/about/AppLogDialog.kt`
- Modify: `app/src/main/java/io/legado/app/ui/about/AboutFragment.kt`
- Modify: `app/src/main/res/menu/app_log.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-zh/strings.xml`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenterTest.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilderTest.kt`

- [ ] Step 1: Add failing tests for read summaries grouped by animation/result and read slow-path Top-N plus phase summaries.
- [ ] Step 2: Implement tracker-side group/slow-line builders for read diagnostics using the enriched details and phase metrics.
- [ ] Step 3: Expose the new read diagnostics through the current log dialog and export flow without creating new screens.
- [ ] Step 4: Re-run perf tracker/presenter/export tests.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsTrackerTest --tests io.legado.app.help.perf.PerformanceMetricsSnapshotPresenterTest --tests io.legado.app.help.perf.PerformanceMetricsBatchExportBuilderTest -Pksp.incremental=false`
Expected: PASS with read summary text, slow-path outputs, and export entries covered.

### Task 15: Batch-46 RSS detail enrichment round two

**Files:**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Modify: `app/src/main/java/io/legado/app/help/webView/RssInjectedPageFetcher.kt`
- Modify: `app/src/main/java/io/legado/app/ui/rss/read/ReadRssActivity.kt`
- Modify: `app/src/main/java/io/legado/app/ui/widget/dialog/BottomWebViewDialog.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`
- Test: `app/src/test/java/io/legado/app/ui/rss/read/RssInjectedPageFetcherTest.kt`

- [ ] Step 1: Add failing tests for RSS details carrying `statusClass`, `contentType`, `bodySizeBucket`, and `injectionOutcome`.
- [ ] Step 2: Extend the RSS fetcher/outcome model so these fields are emitted once and shared by both RSS call sites.
- [ ] Step 3: Record the richer detail set through the tracker without changing retry behavior or navigation flow.
- [ ] Step 4: Re-run RSS helper and tracker tests.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsTrackerTest --tests io.legado.app.ui.rss.read.RssInjectedPageFetcherTest -Pksp.incremental=false`
Expected: PASS with richer RSS detail fields and shared fetcher outputs covered.

### Task 16: Batch-48 RSS injection path optimization

**Files:**
- Modify: `app/src/main/java/io/legado/app/help/webView/RssInjectedPageFetcher.kt`
- Modify: `app/src/main/java/io/legado/app/ui/rss/read/ReadRssActivity.kt`
- Modify: `app/src/main/java/io/legado/app/ui/widget/dialog/BottomWebViewDialog.kt`
- Test: `app/src/test/java/io/legado/app/ui/rss/read/RssInjectedPageFetcherTest.kt`

- [ ] Step 1: Add failing tests for skip paths such as non-HTML or clearly non-injectable bodies.
- [ ] Step 2: Optimize the fetcher so low-value branches skip injection work while still returning an explicit `injectionOutcome`.
- [ ] Step 3: Keep response classification and metric source names consistent for both RSS entry points.
- [ ] Step 4: Re-run the RSS fetcher tests.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.rss.read.RssInjectedPageFetcherTest -Pksp.incremental=false`
Expected: PASS with injection skip rules and outcome reporting covered.

### Task 17: Batch-47 RSS classified summaries

**Files:**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsTracker.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenter.kt`
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilder.kt`
- Modify: `app/src/main/java/io/legado/app/ui/about/AppLogDialog.kt`
- Modify: `app/src/main/java/io/legado/app/ui/about/AboutFragment.kt`
- Modify: `app/src/main/res/menu/app_log.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-zh/strings.xml`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsTrackerTest.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenterTest.kt`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilderTest.kt`

- [ ] Step 1: Add failing tests for classified RSS summaries by `statusClass`, `contentType`, and selected combined dimensions.
- [ ] Step 2: Implement tracker-side classified summary builders that consume the enriched details from Task 15.
- [ ] Step 3: Wire the new summaries into the existing log/export flow without adding dedicated screens.
- [ ] Step 4: Re-run perf tracker/presenter/export tests.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsTrackerTest --tests io.legado.app.help.perf.PerformanceMetricsSnapshotPresenterTest --tests io.legado.app.help.perf.PerformanceMetricsBatchExportBuilderTest -Pksp.incremental=false`
Expected: PASS with RSS classified summaries and export text covered.

### Task 18: Batch-49 log dialog performance menu consolidation

**Files:**
- Modify: `app/src/main/java/io/legado/app/ui/about/AppLogDialog.kt`
- Modify: `app/src/main/res/menu/app_log.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-zh/strings.xml`
- Test: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsSnapshotPresenterTest.kt`

- [ ] Step 1: Replace flat menu sprawl with grouped selection flow or grouped menu sections while preserving the same underlying diagnostics coverage.
- [ ] Step 2: Keep `AppLogDialog` as a thin router that asks the tracker/presenter for prepared data rather than adding new aggregation logic there.
- [ ] Step 3: Re-run presenter tests and manually review the menu XML diff for accidental surface regressions.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsSnapshotPresenterTest -Pksp.incremental=false`
Expected: PASS with menu-triggered text generation unchanged and menu surface simplified in diff review.

### Task 19: Batch-50 export package consolidation

**Files:**
- Modify: `app/src/main/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilder.kt`
- Modify: `app/src/main/java/io/legado/app/ui/about/AboutFragment.kt`
- Modify: `app/src/test/java/io/legado/app/help/perf/PerformanceMetricsBatchExportBuilderTest.kt`

- [ ] Step 1: Add failing tests for the final export package layout, including overview output and reorganized startup/read/rss summary files.
- [ ] Step 2: Update the builder and `AboutFragment` export wiring so log/export use the same final diagnostics families.
- [ ] Step 3: Keep empty-state behavior and generated timestamp formatting stable across all files.
- [ ] Step 4: Re-run export builder tests.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.PerformanceMetricsBatchExportBuilderTest -Pksp.incremental=false`
Expected: PASS with the consolidated export file set and overview payload covered.

### Task 20: Wave-level verification and commit boundaries

**Files:**
- Modify: only files touched by Tasks 2-19

- [ ] Step 1: Re-run the combined perf/startup/read/RSS unit slice after Wave 1 changes and commit the foundation/overview slice.
- [ ] Step 2: Re-run the combined perf/startup slice after Wave 2 changes and commit the startup slice.
- [ ] Step 3: Re-run the combined perf/read slice after Wave 3 changes and commit the reading slice.
- [ ] Step 4: Re-run the combined perf/RSS slice after Wave 4 changes and commit the RSS/consolidation slice.
- [ ] Step 5: Review `git diff --stat` and `git log --oneline --max-count=8` to ensure the wave commits map back to the spec.

Run: `$env:JAVA_HOME='C:\Users\CodexSandboxOffline\.codex\.sandbox\cwd\d362613ce7b20a4e\tools\jdk17'; $env:ANDROID_HOME='D:\Android'; $env:ANDROID_SDK_ROOT='D:\Android'; .\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.help.perf.* --tests io.legado.app.ui.main.* --tests io.legado.app.AppStartupPolicyTest --tests io.legado.app.ui.book.read.page.* --tests io.legado.app.ui.rss.read.* -Pksp.incremental=false`
Expected: PASS with all targeted wave batches green before final push.

### Task 21: Push and CI verification

**Files:**
- Modify: only files touched by Tasks 2-19

- [ ] Step 1: Push the completed wave branch to `origin/main`.
- [ ] Step 2: Track GitHub Actions `Test Build` until the new run reaches `success`.
- [ ] Step 3: Update memory/state with the commit, run id, what changed, and how the implementation was structured before reporting completion.

Run: `git -C D:\legado push origin main`
Expected: Push succeeds and the new commit set is visible on `origin/main`.

Run: `gh run list --repo jackAqwq/legado --workflow "Test Build" --limit 1`
Expected: Shows the newest `Test Build` run for the pushed commit so it can be tracked to success.
