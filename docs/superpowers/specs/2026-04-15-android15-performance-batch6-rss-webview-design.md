# Android15+ Performance Batch-6 (RSS/WebView Blocking Helper) Design

## Context

- Project already targets Android 15+ (`minSdk 35`), so low-version compatibility is no longer a constraint for this batch.
- RSS/WebView interception path currently uses `runBlocking(IO)` in:
  - `app/src/main/java/io/legado/app/ui/rss/read/ReadRssActivity.kt`
  - `app/src/main/java/io/legado/app/ui/widget/dialog/BottomWebViewDialog.kt`
- Both paths call suspend `okHttpClient.newCallResponse { ... }`, creating coroutine bridge overhead inside synchronous interception callbacks.

## Goal

Reduce coroutine bridge overhead in synchronous WebView interception flow without changing behavior.

## Scope

1. Add synchronous HTTP helper in `OkHttpUtils.kt`:
   - `OkHttpClient.newCallResponseBlocking(retry, builder)`
   - Retry semantics must match existing suspend helper.
2. Convert interception content-rewrite methods from suspend to sync in:
   - `ReadRssActivity`
   - `BottomWebViewDialog`
3. Remove `runBlocking(IO)` usage in the two interception call sites.
4. Add JVM unit tests for blocking helper retry behavior.

## Non-goals

- No behavior change for resource filtering, cookie write-back, JS injection logic.
- No network stack architecture change (Cronet/OkHttp strategy unchanged).
- No large cross-module refactor in this batch.

## Design Decisions

1. Keep both async and blocking helper versions in `OkHttpUtils.kt`:
   - Existing suspend call sites remain unaffected.
   - Interception path can use blocking API directly in synchronous callback.
2. Use the same request-builder contract (`Request.Builder.() -> Unit`) for both helpers:
   - Keeps migration minimal.
3. Unit-test the new blocking helper with MockWebServer:
   - Verify retry count and success-on-retry behavior.
   - Verify terminal response behavior when retries are exhausted.

## Risks and Mitigations

- Risk: subtle behavior drift in interception response assembly.
  - Mitigation: keep body parsing, cookie propagation, and JS insertion code unchanged except call style.
- Risk: retry logic mismatch between async and blocking helpers.
  - Mitigation: dedicated unit tests around retry paths.

## Validation

- Run targeted unit tests:
  - `RssWebInterceptDeciderTest`
  - `RssHtmlHeadInjectorTest`
  - new `OkHttpUtilsBlockingTest`
- Build/test gate:
  - `:app:testAppDebugUnitTest` for targeted tests
- CI gate:
  - Push to `main` and wait for GitHub Actions `Test Build` success.
