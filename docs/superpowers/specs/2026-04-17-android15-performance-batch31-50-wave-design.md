# Android15+ Performance Batch-31~50 (Wave Compression) Design

## Context

- Batch-9~30 established a lightweight in-memory metrics stack covering startup, reading, and RSS interception.
- Batch-21~30 also introduced the first structured detail model, startup helpers, read invalidation helper, and shared RSS injected-page fetcher.
- The current log dialog and export pipeline can already show/copy/export grouped RSS and performance snapshots, but the query model is starting to sprawl and the menu surface is growing flat and noisy.
- The next request is to compress twenty additional batches into one coordinated wave that keeps progress balanced across startup, reading, and RSS while allowing moderate runtime changes where the payoff is clear.

## Goal

Deliver Batch-31~50 as one coordinated Android15+ performance and observability wave that:

1. Keeps progress balanced across startup, reading, and RSS.
2. Strengthens the metrics query and aggregation foundation so new summaries do not duplicate ad-hoc filtering logic.
3. Extends user-facing diagnostics through existing log and export entry points rather than adding new screens.
4. Applies a small number of moderate runtime optimizations where they are measurable and low enough risk to validate with focused JVM tests plus CI.

## Constraints

1. Keep the work centered on the existing metrics, log, export, startup, reading, and RSS infrastructure.
2. Do not add new settings toggles; continue using the existing performance metrics enable switch.
3. Do not add new pages or dedicated diagnostics screens; log dialog and export remain the primary user-facing entry points.
4. Preserve export line compatibility where possible so existing tooling and inspection habits do not break.
5. Prefer pure helpers, planner/policy objects, and tracker-side query methods before wiring UI consumers.
6. Accept moderate runtime changes only where behavior remains understandable, testable, and reviewable.

## Non-goals

- No persistent metrics storage.
- No database schema changes.
- No redesign of reader UX or startup UX.
- No network stack replacement.
- No broad refactor outside the touched startup, reading, RSS, and performance modules.

## Architecture

Batch-31~50 stays on the existing architecture and extends it in four layers.

### 1. Metrics Core

`PerformanceMetricsTracker` and `PerformanceMetricDetails` remain the source of truth for in-memory metric records. This wave adds a shared query/filter layer so summary, Top-N, and grouped views can be expressed once and reused by startup, reading, and RSS consumers.

The query layer absorbs dimensions that are already emerging across previous batches:

- metric prefix
- source
- result
- failure bucket
- startup stage/phase/group
- recent-window / max-age
- limit / Top-N

This keeps future summaries from proliferating specialized tracker methods that differ only in filter shape.

### 2. Metrics Presentation

`PerformanceMetricsSnapshotPresenter` stays responsible for turning tracker outputs into readable text for `AppLogDialog` and clipboard flows. New summaries should consume tracker aggregates or query results directly and avoid re-implementing grouping or filtering inside the UI layer.

### 3. Metrics Export

`PerformanceMetricsBatchExportBuilder` and the existing `AboutFragment.saveLog()` export path remain the export surface. New files should be generated from the same tracker query outputs used by the log dialog so view/copy/export stay consistent.

### 4. Runtime Producers

Startup producers stay in `App.kt`, `MainActivity`, `MainStartupTaskPlanner`, and `AppStartupPolicy`.

Reading producers stay in `ReadView`, page delegates, render invalidation helpers, and layout helpers.

RSS producers stay in `ReadRssActivity`, `BottomWebViewDialog`, and `RssInjectedPageFetcher`.

Moderate runtime changes in this wave are limited to:

- better startup task deferral/grouping
- tighter reading flip/invalidation phase metrics and invalidation batching
- RSS injection/path optimizations for clearly low-value branches such as non-HTML or injection-skipped responses

## Components

### Metrics Core

- `PerformanceMetricDetails`
  - keeps structured key-value details
  - may grow helper accessors or safer parse/query helpers
- `PerformanceMetricsTracker`
  - stores records
  - performs filtering, grouping, summary, Top-N, overview, and time-window operations
  - owns compatibility conversion back to existing export lines
- new internal query/filter model
  - centralizes selection dimensions instead of extending multiple method signatures independently

### Metrics Presentation

- `PerformanceMetricsSnapshotPresenter`
  - builds overview text
  - builds startup/read/rss grouped summary text
  - builds comparison and Top-N text from tracker outputs

### Metrics Export

- `PerformanceMetricsBatchExportBuilder`
  - emits per-domain exports
  - emits summary and overview files
  - keeps file naming and payload layout stable enough for manual inspection
- `AboutFragment`
  - remains the single export trigger

### Startup Pipeline

- `AppStartupPolicy`
  - decides which startup-side background work is allowed in which context
- `MainStartupTaskPlanner`
  - turns startup intent into ordered and delayed tasks with richer grouping metadata
- `App.kt` and `MainActivity`
  - stay thin producer/wiring points

### Reading Runtime

- `ReadView`
  - continues to own touch and page-flip lifecycle wiring
- render invalidation helper(s)
  - continue to centralize invalidate decisions
- read metrics helpers
  - normalize phase and result details for flip/layout/invalidate paths

### RSS Runtime

- `RssInjectedPageFetcher`
  - remains the shared interception/rewrite helper
  - grows more explicit injection outcome reporting
- RSS metric detail helpers inside tracker/query layer
  - support richer failure and content classification without UI parsing

## Data Flow

The wave keeps one consistent data flow.

### Producer

Startup, reading, and RSS code emit metrics only at stable boundary points:

- startup lifecycle and planned task boundaries
- page-flip, render, layout, and invalidate boundaries
- RSS intercept, fetch, classify, and inject boundaries

### Normalize

All new dimensions are normalized into structured `PerformanceMetricDetails` entries rather than appended as free-form strings. This is required so future groupings do not need to re-parse UI text or exported lines.

### Aggregate

`PerformanceMetricsTracker` performs:

- record filtering
- grouped summaries
- Top-N selection
- recent-window selection
- cross-domain overview generation
- domain-specific aggregations such as RSS source+bucket and startup phase/group summaries

### Present

`PerformanceMetricsSnapshotPresenter` turns tracker outputs into:

- log dialog preview text
- clipboard text
- summary text blocks
- domain overview text

### Export

`PerformanceMetricsBatchExportBuilder` turns the same tracker outputs into:

- domain files
- summary files
- overview file(s)

### Act

Moderate runtime improvements feed new details back into the same tracker so before/after behavior remains inspectable through the existing surfaces.

## Batch Map

### Wave 1: Foundation and Overview

#### Batch-31: Startup stage summaries

- add startup summary/view/export grouped by `stage`
- keep existing `startup.*` line output intact

#### Batch-32: RSS source+bucket summaries

- add grouped RSS failure summaries by `source + failure bucket`
- expose them through log and export flows

#### Batch-33: Read flip detail enrichment

- record richer read flip detail such as `direction`, `anim`, `result`, and `cancelReason`

#### Batch-34: Shared metrics query/filter layer

- introduce an internal shared query/filter model for tracker selection
- migrate existing tracker consumers to it

#### Batch-35: Cross-domain overview snapshot

- produce one overview summary spanning startup, reading, and RSS
- expose it in log and export flows

### Wave 2: Startup Pipeline

#### Batch-36: Startup task phase/group model

- extend `MainStartupTaskPlanner` to produce richer task metadata such as `phase`, `group`, or `label`
- keep task execution in `MainActivity`

#### Batch-37: Startup task deferral optimization

- move lower-priority startup work deeper into warm/idle windows
- keep core startup correctness unchanged

#### Batch-38: Context-aware startup policy

- extend `AppStartupPolicy` so decisions are clearer across first-open, upgrade, and resume-like contexts where applicable

#### Batch-39: Startup phase/group summaries in log/export

- expose startup phase/group summaries and recent windows through existing diagnostics surfaces

#### Batch-40: Startup metric naming and producer cleanup

- normalize startup metric naming and producer boundaries across `App.kt` and `MainActivity`

### Wave 3: Reading Runtime

#### Batch-41: Read lifecycle result refinement

- distinguish read flip outcomes such as `completed`, `cancelled`, and `aborted`
- capture stable cancel reason details

#### Batch-42: Read anim/result summaries

- expose reading summaries grouped by animation mode and result

#### Batch-43: Read invalidation batching cleanup

- reduce duplicate invalidate cascades by centralizing batchable invalidation decisions

#### Batch-44: Read phase metrics

- add explicit metrics for phases such as `layout`, `pre_render`, and `invalidate`

#### Batch-45: Read slow-path Top-N and phase summaries

- expose read slow-path Top-N plus read phase summaries through log and export

### Wave 4: RSS and Entry-Point Consolidation

#### Batch-46: RSS detail enrichment round two

- add stable aggregatable fields such as `statusClass`, `contentType`, `bodySizeBucket`, and `injectionOutcome`

#### Batch-47: RSS classified summaries

- expose summaries by `statusClass`, `contentType`, and selected combined dimensions

#### Batch-48: RSS injection path optimization

- skip clearly unnecessary injection work for responses where injection is known to be low-value or inapplicable
- record the chosen injection outcome for diagnosis

#### Batch-49: Log dialog performance menu consolidation

- reduce flat menu sprawl in `AppLogDialog`
- regroup diagnostics entry points without creating new screens

#### Batch-50: Export package consolidation

- add overview export files and align startup/read/rss summary files with the reorganized diagnostics model

## Dependencies

1. Batch-34 is foundational for later grouped and windowed views.
2. Batch-33 should land before Batch-42 and Batch-45 so read summaries have richer input.
3. Batch-36 and Batch-38 should precede Batch-39 and Batch-40.
4. Batch-46 should precede Batch-47 and Batch-48.
5. Batch-49 and Batch-50 should land last as the main user-facing consolidation step.

## Error Handling

1. Empty snapshots must remain safe and produce empty-state text rather than crashes.
2. Missing detail fields must degrade to `unknown`/empty-state grouping rather than parsing errors.
3. Export generation must continue to fail soft inside the existing `runCatching` path in `AboutFragment`.
4. Query/filter helpers must treat unknown filters as non-matching rather than throwing.
5. Runtime producer changes must not introduce new retries or stateful caches beyond current behavior.

## Testing

Validation remains layered and JVM-first.

### Tracker / Query tests

- extend `PerformanceMetricsTrackerTest`
- validate shared query/filter semantics
- validate overview, group, Top-N, recent-window, and combined-dimension summaries
- validate backward-compatible export line behavior

### Presenter / Export tests

- extend `PerformanceMetricsSnapshotPresenterTest`
- extend `PerformanceMetricsBatchExportBuilderTest`
- lock text and export payload shape for startup/read/rss/overview summaries

### Startup helper tests

- extend `MainStartupTaskPlannerTest`
- extend `AppStartupPolicyTest`
- verify task grouping, deferral, and policy decisions in pure JVM tests

### Reading helper tests

- extend existing read helper tests and add new tests where phase/result logic moves into helpers
- validate flip result enrichment and invalidation batching semantics

### RSS helper tests

- extend `RssInjectedPageFetcherTest`
- extend perf tracker tests for enriched RSS detail dimensions and injection outcomes

### Combined verification

Run targeted Gradle test slices for:

- `io.legado.app.help.perf.*`
- `io.legado.app.ui.main.*`
- `io.legado.app.AppStartupPolicyTest`
- `io.legado.app.ui.book.read.page.*`
- `io.legado.app.ui.rss.read.*`

Then run the combined targeted verification suite before push, followed by GitHub Actions tracking to success.

## Risks and Mitigations

### Query-layer regression risk

Risk:
- a shared query/filter abstraction could unintentionally change existing summary/export semantics

Mitigation:
- keep export line format unchanged
- add regression coverage for all current filters and summaries before migrating consumers

### Startup behavior drift risk

Risk:
- task deferral and policy cleanup could shift when refresh/update work happens

Mitigation:
- keep planner/policy pure and testable
- limit runtime changes to delay/group decisions, not business intent

### Reading interaction risk

Risk:
- flip lifecycle and invalidation cleanup could change cancellation or redraw timing

Mitigation:
- introduce result/cancel reason semantics first
- verify helper behavior before touching `ReadView` wiring

### RSS over-classification risk

Risk:
- adding too many RSS detail dimensions could create noisy or unstable summaries

Mitigation:
- add only fields that are stable and aggregatable
- avoid free-form text fields in summaries

### Diagnostics surface sprawl risk

Risk:
- continuing to add flat menu items and export files could make diagnostics harder to use

Mitigation:
- explicitly reserve Batch-49 and Batch-50 for consolidation
- prefer grouped entry points and consistent file families over more one-off outputs

## Scope Summary

- balanced across startup, reading, and RSS
- moderate runtime changes allowed, but only in bounded, testable slices
- no new screens, no persistent metrics storage, no new settings
- one export/log vocabulary shared by tracker, presenter, and export builder
