# Batch-7 API35 Compatibility Cleanup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove dead low-version (`M/N/O`) compatibility branches under Android 15+ baseline while preserving behavior.

**Architecture:** Simplify obsolete API-level conditions directly in utility and view-model code; for critical logic in `MainViewModel`, extract pure helper methods and cover them with JVM unit tests.

**Tech Stack:** Kotlin, Android framework APIs, JUnit4, Gradle.

---

### Task 1: Remove Dead API Guards In Utilities

**Files:**
- Modify: `app/src/main/java/io/legado/app/utils/SystemUtils.kt`
- Modify: `app/src/main/java/io/legado/app/utils/ViewExtensions.kt`

- [ ] **Step 1: Simplify `ignoreBatteryOptimization`**

Remove obsolete `SDK_INT < M` guard; keep battery optimization check and intent launch flow unchanged.

- [ ] **Step 2: Simplify `disableAutoFill`**

Remove obsolete `SDK_INT >= O` guard and directly assign:

```kotlin
this.importantForAutofill = IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
```

- [ ] **Step 3: Verify obsolete imports removed**

Run: `rg -n "Build\\.VERSION|VERSION_CODES|@SuppressLint\\(\"ObsoleteSdkInt\"\\)" app/src/main/java/io/legado/app/utils/SystemUtils.kt app/src/main/java/io/legado/app/utils/ViewExtensions.kt`  
Expected: no low-version branch artifacts remain in these two files.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/io/legado/app/utils/SystemUtils.kt app/src/main/java/io/legado/app/utils/ViewExtensions.kt
git commit -m "refactor: drop obsolete api guards for android15 baseline"
```

### Task 2: Simplify MainViewModel API Branches

**Files:**
- Modify: `app/src/main/java/io/legado/app/ui/main/MainViewModel.kt`

- [ ] **Step 1: Add pure helper methods**

Add private helpers:

```kotlin
private fun updatingBookCount(waitSize: Int, onUpSize: Int): Int = waitSize + onUpSize
private fun shouldEnableCacheBook(waitIsEmpty: Boolean, onUpIsEmpty: Boolean): Boolean =
    waitIsEmpty && onUpIsEmpty
```

- [ ] **Step 2: Replace `SDK_INT >= N` branches**

Use:

```kotlin
onUpBooksLiveData.postValue(updatingBookCount(waitUpTocBooks.size, onUpTocBooks.size))
```

and

```kotlin
CacheBook.setWorkingState(shouldEnableCacheBook(waitUpTocBooks.isEmpty(), onUpTocBooks.isEmpty()))
```

- [ ] **Step 3: Remove `Build` import**

Run: `rg -n "Build\\.|VERSION_CODES" app/src/main/java/io/legado/app/ui/main/MainViewModel.kt`  
Expected: no matches.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/io/legado/app/ui/main/MainViewModel.kt
git commit -m "refactor: remove obsolete sdk branch logic in main view model"
```

### Task 3: Add Regression Tests For Extracted Helper Logic

**Files:**
- Create: `app/src/test/java/io/legado/app/ui/main/MainViewModelApi35LogicTest.kt`

- [ ] **Step 1: Write tests for helper logic**

Add tests for:
- updating count combination
- cache-enable decision truth table

- [ ] **Step 2: Run tests (RED/GREEN if needed)**

Run: `.\gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.ui.main.MainViewModelApi35LogicTest"`  
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/io/legado/app/ui/main/MainViewModelApi35LogicTest.kt app/src/main/java/io/legado/app/ui/main/MainViewModel.kt
git commit -m "test: cover api35 main view model helper logic"
```

### Task 4: Final Verification + Delivery

**Files:**
- Modify: `docs/superpowers/specs/2026-04-16-android15-performance-batch7-api35-compat-cleanup-design.md` (if needed)

- [ ] **Step 1: Run targeted regression suite**

Run: `.\gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.ui.main.MainViewModelApi35LogicTest" --tests "io.legado.app.help.http.OkHttpUtilsBlockingTest" --tests "io.legado.app.ui.rss.read.RssWebInterceptDeciderTest" --tests "io.legado.app.ui.rss.read.RssHtmlHeadInjectorTest"`  
Expected: PASS.

- [ ] **Step 2: Commit plan/spec docs**

```bash
git add docs/superpowers/specs/2026-04-16-android15-performance-batch7-api35-compat-cleanup-design.md docs/superpowers/plans/2026-04-16-batch7-api35-compat-cleanup.md
git commit -m "docs: add batch-7 api35 compatibility cleanup plan"
```

- [ ] **Step 3: Push + merge flow**

Run: `git push -u origin perf/batch-7-api35-compat-cleanup && git push origin HEAD:main`  
Expected: push success.

- [ ] **Step 4: Track CI until success**

Poll `Test Build` run for pushed main SHA.  
Expected: `conclusion=success`.

- [ ] **Step 5: Update memory before final user response**

Update:
- `~/self-improving/projects/legado-build-success-log.md`
- `~/proactivity/session-state.md`
- `~/proactivity/log.md`
- `~/self-improving/projects/legado.md`
