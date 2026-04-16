# Android15+ Performance Batch-9 (Metrics Baseline) Design

## Context

- App baseline is Android 15+ (`minSdk 35`).
- Batch-8 optimized hotspots but lacks built-in baseline latency samples for follow-up tuning.

## Goal

Add a behavior-neutral, lightweight metrics layer with runtime switch to sample:

1. app startup to main UI ready
2. reading page flip latency
3. RSS interception rewrite latency

## Scope

1. Add in-memory ring-buffer tracker with export lines API.
2. Add settings switch (`performanceMetrics`, default off).
3. Wire metrics points:
   - `App` + `MainActivity` for startup
   - `ReadView` for page flip
   - `ReadRssActivity` and `BottomWebViewDialog` for RSS interception
4. Add JVM unit tests for tracker behavior.

## Non-goals

- No UX redesign for metrics visualization/export entry.
- No behavior changes to read/web interception flows.
- No external analytics upload.

## Design Decisions

1. Keep tracker independent and pure enough for JVM tests:
   - `PerformanceMetricsTracker`
   - `PerformanceMetricRecord`
2. Preserve behavior neutrality:
   - switch default is off
   - collection only records metadata + elapsed duration
3. Keep logging soft:
   - optional debug log line via sink
   - bounded ring buffer (`MAX_RECORDS = 200`)

## Risks and Mitigations

- Risk: extra overhead in hot path.
  - Mitigation: O(1) timestamp capture + short synchronized section + fixed-size buffer.
- Risk: stale marker causing wrong duration.
  - Mitigation: marker consumed/cleared on completion.

## Validation

Run:

- `PerformanceMetricsTrackerTest`
- `RssWebInterceptDeciderTest`
- `RssHtmlHeadInjectorTest`
- `ReadViewTouchBoundsTest`

And verify `:app:testAppDebugUnitTest` command scope passes.
