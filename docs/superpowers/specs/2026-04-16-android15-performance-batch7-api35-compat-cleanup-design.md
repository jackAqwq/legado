# Android15+ Performance Batch-7 (API35 Compatibility Cleanup) Design

## Context

- Project baseline is Android 15+ (`minSdk 35`), so branches for `SDK_INT < M/N/O` are dead code.
- Current compatibility branches still exist in:
  - `app/src/main/java/io/legado/app/utils/SystemUtils.kt`
  - `app/src/main/java/io/legado/app/utils/ViewExtensions.kt`
  - `app/src/main/java/io/legado/app/ui/main/MainViewModel.kt`

## Goal

Remove dead low-version compatibility branches and simplify runtime paths under Android 15+ constraints.

## Scope

1. `SystemUtils.ignoreBatteryOptimization`:
   - remove obsolete `SDK_INT < M` early return.
2. `View.disableAutoFill`:
   - remove obsolete `SDK_INT >= O` guard and keep direct assignment.
3. `MainViewModel`:
   - remove obsolete `SDK_INT >= N` branch forks in `postUpBooksLiveData` and `cacheBook`.
   - centralize the resulting logic in small pure helper methods for testability.
4. Add JVM unit tests covering the extracted pure helper logic in `MainViewModel`.

## Non-goals

- No behavior changes unrelated to API-level branching.
- No thread model changes or broader refactors outside listed files.

## Design Decisions

1. Prefer direct simplification over broad rewrites:
   - keep method signatures and call sites intact.
2. For `MainViewModel`, extract pure helper methods:
   - `updatingBookCount(waitSize, onUpSize)`
   - `shouldEnableCacheBook(waitIsEmpty, onUpIsEmpty)`
   - these are deterministic and JVM-testable.
3. Keep tests focused on extracted helper behavior:
   - avoid Robolectric dependency for this batch.

## Risks and Mitigations

- Risk: accidental behavior drift in count or cache-enable logic.
  - Mitigation: add explicit unit tests for helper functions.
- Risk: missing hidden low-version assumptions.
  - Mitigation: keep changes tightly scoped; run existing targeted unit tests and CI full build.

## Validation

- Run new helper tests for `MainViewModel`.
- Run regression tests from previous batch:
  - `OkHttpUtilsBlockingTest`
  - `RssWebInterceptDeciderTest`
  - `RssHtmlHeadInjectorTest`
- Push to `main` and wait for GitHub Actions `Test Build` success.
