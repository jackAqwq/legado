# Batch-6 RSS/WebView Blocking Helper Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove `runBlocking(IO)` bridge overhead from RSS/WebView synchronous interception path while preserving behavior.

**Architecture:** Add a blocking OkHttp helper with the same builder contract and retry behavior as the suspend helper, then switch two interception callers to synchronous local functions. Keep all existing cookie propagation and HTML/JS rewrite behavior unchanged.

**Tech Stack:** Kotlin, OkHttp, WebView interception, JUnit4, MockWebServer, Gradle.

---

### Task 1: Add Test Dependency For Blocking HTTP Helper Tests

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle`

- [ ] **Step 1: Add MockWebServer library alias**

```toml
# gradle/libs.versions.toml
okhttp-mockwebserver = { module = "com.squareup.okhttp3:mockwebserver", version.ref = "okhttp" }
```

- [ ] **Step 2: Wire test dependency in app module**

```groovy
// app/build.gradle
testImplementation(libs.okhttp.mockwebserver)
```

- [ ] **Step 3: Verify dependency wiring**

Run: `.\gradlew.bat :app:dependencies --configuration testAppDebugUnitTestCompileClasspath`  
Expected: output contains `com.squareup.okhttp3:mockwebserver`.

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle
git commit -m "test: add mockwebserver for okhttp blocking helper tests"
```

### Task 2: TDD For Blocking OkHttp Retry Helper

**Files:**
- Create: `app/src/test/java/io/legado/app/help/http/OkHttpUtilsBlockingTest.kt`

- [ ] **Step 1: Write failing tests first**

```kotlin
@Test
fun blocking_helper_retries_and_succeeds_on_second_attempt() { ... }

@Test
fun blocking_helper_returns_last_response_when_all_attempts_fail() { ... }
```

- [ ] **Step 2: Run test to verify RED**

Run: `.\gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.help.http.OkHttpUtilsBlockingTest"`  
Expected: FAIL with unresolved reference to `newCallResponseBlocking`.

- [ ] **Step 3: Implement minimal helper in production code**

```kotlin
fun OkHttpClient.newCallResponseBlocking(
    retry: Int = 0,
    builder: Request.Builder.() -> Unit
): Response { ... }
```

- [ ] **Step 4: Run test to verify GREEN**

Run: `.\gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.help.http.OkHttpUtilsBlockingTest"`  
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/io/legado/app/help/http/OkHttpUtils.kt app/src/test/java/io/legado/app/help/http/OkHttpUtilsBlockingTest.kt
git commit -m "test: cover okhttp blocking retry helper"
```

### Task 3: Switch RSS/WebView Interception To Blocking Helper

**Files:**
- Modify: `app/src/main/java/io/legado/app/ui/rss/read/ReadRssActivity.kt`
- Modify: `app/src/main/java/io/legado/app/ui/widget/dialog/BottomWebViewDialog.kt`

- [ ] **Step 1: Replace `runBlocking(IO)` call sites with direct sync calls**

```kotlin
return getModifiedContentWithJs(url, request) ?: super.shouldInterceptRequest(view, request)
```

- [ ] **Step 2: Convert interception rewrite functions from `suspend` to sync**

```kotlin
private fun getModifiedContentWithJs(url: String, request: WebResourceRequest): WebResourceResponse? { ... }
```

- [ ] **Step 3: Use `newCallResponseBlocking` in the two functions**

```kotlin
val res = okHttpClient.newCallResponseBlocking { ... }
```

- [ ] **Step 4: Keep cookie and HTML injection logic unchanged**

Run: `rg -n "Set-Cookie|setCookie|JS_URL|RssHtmlHeadInjector" app/src/main/java/io/legado/app/ui/rss/read/ReadRssActivity.kt app/src/main/java/io/legado/app/ui/widget/dialog/BottomWebViewDialog.kt`  
Expected: both files still contain cookie write-back and JS injection flow.

- [ ] **Step 5: Run targeted regression tests**

Run: `.\gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.ui.rss.read.RssWebInterceptDeciderTest" --tests "io.legado.app.ui.rss.read.RssHtmlHeadInjectorTest" --tests "io.legado.app.help.http.OkHttpUtilsBlockingTest"`  
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/io/legado/app/ui/rss/read/ReadRssActivity.kt app/src/main/java/io/legado/app/ui/widget/dialog/BottomWebViewDialog.kt
git commit -m "perf: remove runBlocking bridge in rss web interceptors"
```

### Task 4: Final Verify And Deliver

**Files:**
- Modify: `docs/superpowers/specs/2026-04-15-android15-performance-batch6-rss-webview-design.md` (if final notes needed)

- [ ] **Step 1: Run final focused unit-test suite**

Run: `.\gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.help.http.OkHttpUtilsBlockingTest" --tests "io.legado.app.ui.rss.read.RssWebInterceptDeciderTest" --tests "io.legado.app.ui.rss.read.RssHtmlHeadInjectorTest"`  
Expected: PASS.

- [ ] **Step 2: Push branch and fast-forward main**

Run: `git push -u origin perf/batch-6-rss-webview-blocking-helper && git push origin HEAD:main`  
Expected: push success.

- [ ] **Step 3: Track CI until completion**

Run: poll GitHub Actions `Test Build` run for pushed `main` SHA.  
Expected: `conclusion=success`.

- [ ] **Step 4: Update memory records before user reply**

Update:
- `~/self-improving/projects/legado-build-success-log.md`
- `~/proactivity/session-state.md`
- `~/proactivity/log.md`

- [ ] **Step 5: Commit documentation changes if any**

```bash
git add docs/superpowers/specs/2026-04-15-android15-performance-batch6-rss-webview-design.md docs/superpowers/plans/2026-04-15-batch6-rss-webview-blocking-helper.md
git commit -m "docs: add batch-6 rss/webview performance plan"
```
