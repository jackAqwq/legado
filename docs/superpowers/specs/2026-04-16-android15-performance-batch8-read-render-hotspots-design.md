# Android15+ Performance Batch-8 (Read Render Hotspots) Design

## Context

- App baseline is Android 15+ (`minSdk 35`).
- Read-path hot loops exist in:
  - `ReadView.onTouchEvent` (per-event mandatory gesture inset check)
  - `TextChapterLayout.measureTextSplit` (line-level text cluster splitting).

## Goal

Reduce per-event/per-line overhead on reading path without changing behavior.

## Scope

1. `ReadView` touch fast-path:
   - cache mandatory system gesture bottom inset via `onApplyWindowInsets`.
   - replace per-touch `rootWindowInsets + currentWindowMetrics` query with cached bound check.
   - extract pure decision helper for JVM tests.
2. `TextChapterLayout` text cluster split:
   - extract splitting logic to pure helper with one-pass scan.
   - keep zero-width char behavior and grouping semantics unchanged.
   - add JVM tests for splitter behavior.

## Non-goals

- No change to page animation, chapter/page semantics, or selection behavior.
- No UI redesign or data-layer refactor.

## Design Decisions

1. Pure helpers for testability:
   - `ReadViewTouchBounds.shouldIgnoreTouchForMandatoryGestures(...)`
   - `TextClusterSplitter.measureTextSplit(...)`
2. Keep call sites minimal:
   - replace existing inline logic with helper calls only.
3. Behavior lock via unit tests:
   - cover boundary conditions and zero-width character handling.

## Risks and Mitigations

- Risk: touch-ignore threshold regression near bottom gesture area.
  - Mitigation: explicit boundary tests for `y` relative to `(viewHeight - insetBottom)`.
- Risk: cluster-split mismatch affecting layout spacing.
  - Mitigation: tests for mixed normal chars + zero-width chars + widths array.

## Validation

- Run:
  - `ReadViewTouchBoundsTest`
  - `TextClusterSplitterTest`
  - existing regression set:
    - `MainViewModelApi35LogicTest`
    - `OkHttpUtilsBlockingTest`
    - `RssWebInterceptDeciderTest`
    - `RssHtmlHeadInjectorTest`
- Push to `main` and track GitHub Actions `Test Build` to success.
