# Batch-8 Read Render Hotspots Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Optimize reading hot paths by reducing touch-time system queries and line-split overhead while preserving behavior.

**Architecture:** Extract two pure helpers (`ReadViewTouchBounds`, `TextClusterSplitter`) with unit tests, then wire `ReadView` and `TextChapterLayout` to these helpers.

**Tech Stack:** Kotlin, Android View insets, JUnit4, Gradle.

---

### Task 1: Add Touch-Bounds Helper + Tests

**Files:**
- Create: `app/src/main/java/io/legado/app/ui/book/read/page/ReadViewTouchBounds.kt`
- Create: `app/src/test/java/io/legado/app/ui/book/read/page/ReadViewTouchBoundsTest.kt`

- [ ] **Step 1: Add pure helper**

Implement:

```kotlin
internal object ReadViewTouchBounds {
    fun shouldIgnoreTouchForMandatoryGestures(y: Float, viewHeight: Int, insetBottom: Int): Boolean
}
```

- [ ] **Step 2: Add boundary tests**

Cover:
- inset <= 0 -> false
- y <= limit -> false
- y > limit -> true

- [ ] **Step 3: Run tests**

Run: `.\gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.ui.book.read.page.ReadViewTouchBoundsTest"`  
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/io/legado/app/ui/book/read/page/ReadViewTouchBounds.kt app/src/test/java/io/legado/app/ui/book/read/page/ReadViewTouchBoundsTest.kt
git commit -m "test: add readview mandatory-gesture touch bounds helper"
```

### Task 2: Wire ReadView To Cached Insets

**Files:**
- Modify: `app/src/main/java/io/legado/app/ui/book/read/page/ReadView.kt`

- [ ] **Step 1: Add cached inset field**

```kotlin
private var mandatoryGestureBottomInset = 0
```

- [ ] **Step 2: Update cache in `onApplyWindowInsets`**

Override and set:

```kotlin
mandatoryGestureBottomInset = insets.getInsetsIgnoringVisibility(WindowInsets.Type.mandatorySystemGestures()).bottom
```

- [ ] **Step 3: Replace per-touch heavy query logic**

Use helper:

```kotlin
if (ReadViewTouchBounds.shouldIgnoreTouchForMandatoryGestures(event.y, height, mandatoryGestureBottomInset) && ...)
```

- [ ] **Step 4: Verify obsolete branch removed**

Run: `rg -n "currentWindowMetrics|Build\\.VERSION|VERSION_CODES\\.R" app/src/main/java/io/legado/app/ui/book/read/page/ReadView.kt`  
Expected: no touch-path version/query leftovers.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/io/legado/app/ui/book/read/page/ReadView.kt
git commit -m "perf: cache readview mandatory gesture inset for touch path"
```

### Task 3: Add Text Cluster Split Helper + Tests

**Files:**
- Create: `app/src/main/java/io/legado/app/ui/book/read/page/provider/TextClusterSplitter.kt`
- Create: `app/src/test/java/io/legado/app/ui/book/read/page/provider/TextClusterSplitterTest.kt`

- [ ] **Step 1: Add one-pass splitter helper**

Implement:

```kotlin
internal object TextClusterSplitter {
    fun measureTextSplit(text: String, widthsArray: FloatArray, start: Int = 0): Pair<ArrayList<String>, ArrayList<Float>>
}
```

- [ ] **Step 2: Add tests for normal and zero-width cases**

Cover:
- basic split (`abc`)
- mixed zero-width continuation behavior
- non-zero start offset behavior

- [ ] **Step 3: Run tests**

Run: `.\gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.ui.book.read.page.provider.TextClusterSplitterTest"`  
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/io/legado/app/ui/book/read/page/provider/TextClusterSplitter.kt app/src/test/java/io/legado/app/ui/book/read/page/provider/TextClusterSplitterTest.kt
git commit -m "test: cover text cluster splitter behavior"
```

### Task 4: Wire TextChapterLayout + Final Verification

**Files:**
- Modify: `app/src/main/java/io/legado/app/ui/book/read/page/provider/TextChapterLayout.kt`
- Modify: `docs/superpowers/specs/2026-04-16-android15-performance-batch8-read-render-hotspots-design.md` (if needed)

- [ ] **Step 1: Replace local split logic with helper call**

Use:

```kotlin
return TextClusterSplitter.measureTextSplit(text, widthsArray, start)
```

- [ ] **Step 2: Run regression suite**

Run: `.\gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.ui.book.read.page.ReadViewTouchBoundsTest" --tests "io.legado.app.ui.book.read.page.provider.TextClusterSplitterTest" --tests "io.legado.app.ui.main.MainViewModelApi35LogicTest" --tests "io.legado.app.help.http.OkHttpUtilsBlockingTest" --tests "io.legado.app.ui.rss.read.RssWebInterceptDeciderTest" --tests "io.legado.app.ui.rss.read.RssHtmlHeadInjectorTest"`  
Expected: PASS.

- [ ] **Step 3: Commit docs**

```bash
git add docs/superpowers/specs/2026-04-16-android15-performance-batch8-read-render-hotspots-design.md docs/superpowers/plans/2026-04-16-batch8-read-render-hotspots.md
git commit -m "docs: add batch-8 read render hotspots plan"
```

- [ ] **Step 4: Push + CI**

Run: `git push -u origin perf/batch-8-read-render-hotspots && git push origin HEAD:main`  
Expected: push success; then track `Test Build` run to `success`.

- [ ] **Step 5: Update memory before final response**

Update:
- `~/self-improving/projects/legado-build-success-log.md`
- `~/proactivity/session-state.md`
- `~/proactivity/log.md`
- `~/self-improving/projects/legado.md`
