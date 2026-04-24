# Android15+ Performance Batch-21~30 (Parallel Optimization) Design

## Context

- Batch-9~20 established a lightweight performance metrics stack for startup, reading, and RSS interception.
- Recent batches focused heavily on RSS diagnostics; startup and reading still have only coarse-grained timing visibility.
- The current request is to compress the next ten micro-batches into one coordinated execution wave while staying low-risk and prioritizing high-yield internal optimizations.

## Goal

Ship ten behavior-neutral or behavior-stabilizing micro-batches across three lines:

1. RSS diagnostics and interception cleanup
2. Startup-path observability and scheduling cleanup
3. Reading-path metrics accuracy and render invalidation/layout helper cleanup

## Scope

### RSS line

1. Batch-21: structure performance metric details so downstream filters and summaries stop re-parsing ad-hoc strings.
2. Batch-22: enrich RSS interception metrics with failure context that improves diagnosis without changing request behavior.
3. Batch-23: expose failure-cause summaries and filters through existing log dialog and export flows.
4. Batch-24: extract duplicated RSS main-document rewrite flow from `ReadRssActivity` and `BottomWebViewDialog` into a shared helper.

### Startup line

1. Batch-25: split startup timing into explicit stages instead of one coarse `startup.main_ui_ready` metric.
2. Batch-26: centralize `MainActivity` startup follow-up scheduling into a planner/helper so UI startup work is easier to reason about and test.
3. Batch-27: centralize `App`-level startup guards for deferred/background tasks into a small policy/helper.

### Reading line

1. Batch-28: tighten page-flip metric lifecycle so taps/long-press/cancel paths do not pollute flip timing.
2. Batch-29: coalesce render invalidation triggers around pre-render and animation paths to reduce redundant UI invalidations.
3. Batch-30: extract HTML line justification math from `TextChapterLayout` into a pure helper with focused tests.

## Constraints

1. Keep default user-visible behavior unchanged unless the change only removes clearly redundant work or fixes clearly incorrect metrics.
2. Prefer pure helpers and JVM tests before touching UI classes.
3. Reuse existing log dialog, tracker, export, and presenter entry points instead of adding new screens or persistence.
4. Keep write scopes narrow enough that independent batches can be parallelized safely.

## Non-goals

- No new persistence for metrics.
- No new settings toggles.
- No redesign of startup UX or reader interaction model.
- No network stack replacement beyond sharing already-equivalent interception logic.

## Batch Map

### Batch-21: Metric details model

- Add a structured representation for metric detail key-values.
- Keep export format backward-compatible by still producing line-based output.
- Use the structured model internally for filtering and future summaries.

### Batch-22: RSS failure detail enrichment

- Add extra RSS intercept detail fields such as failure kind and status code where available.
- Preserve existing success/failure categorization.
- Avoid any retry or request-policy changes.

### Batch-23: RSS failure cause summaries

- Add tracker/presenter/export support for grouped failure-cause summaries.
- Extend `AppLogDialog` and About export through existing menu/export flows only.

### Batch-24: Shared RSS injected-page fetcher

- Move duplicated cookie/header/request/body rewrite logic into one helper.
- Keep both call sites behaviorally equivalent and retain source-specific metric source names.

### Batch-25: Startup stage metrics

- Record finer startup boundaries for app bootstrap and main-screen readiness.
- Keep current startup metric consumers working while enabling richer grouped views.

### Batch-26: Main startup task planner

- Move delayed `MainActivity` follow-up tasks into a planner/helper that emits deterministic actions.
- Leave actual task execution in `MainActivity`.

### Batch-27: App startup policy

- Move app-level deferred task guards into a pure helper/policy object.
- Keep current thresholds/conditions unchanged.

### Batch-28: Read metric lifecycle fix

- Start flip timing only when a flip candidate is actually formed.
- Finish or clear timing on completion/cancel/abort paths to avoid stale measurements.

### Batch-29: Render invalidation gate

- Deduplicate `postInvalidate()` / delegate invalidation triggers on the render path.
- Preserve animation correctness over aggressive suppression.

### Batch-30: HTML line justification helper

- Extract justification calculations from `TextChapterLayout` into a pure helper.
- Keep line output compatible with current rendering semantics.

## Dependencies

1. Batch-21 unblocks Batch-22 and Batch-23.
2. Batch-22 should land before Batch-23 so grouped failure summaries have richer input.
3. Batch-24 is independent of Batch-21~23 at code-structure level.
4. Batch-25, Batch-26, and Batch-27 can share the startup line but should be merged carefully because `App.kt` and `MainActivity.kt` overlap.
5. Batch-28 should precede any deeper read-path metric interpretation.
6. Batch-29 and Batch-30 are independent of each other if write scopes stay separate.

## Validation

Run targeted JVM/unit verification after each affected slice and a combined suite before push:

- `:app:testAppDebugUnitTest --tests io.legado.app.help.perf.*`
- `:app:testAppDebugUnitTest --tests io.legado.app.ui.rss.read.*`
- `:app:testAppDebugUnitTest --tests io.legado.app.ui.book.read.page.*`
- `:app:testAppDebugUnitTest --tests io.legado.app.ui.main.*`

Then build and push, and track GitHub Actions to success before reporting completion.
