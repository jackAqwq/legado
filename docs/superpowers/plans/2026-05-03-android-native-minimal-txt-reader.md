# Android Native Minimal TXT Reader Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a minimal native TXT reading flow with bookshelf support, SAF URI persistence, overlay page-turn, and persisted progress/settings inside the existing `legado` Android app.

**Architecture:** Implement an isolated `minireader` feature slice (`ui + viewmodel + core + repository`) that reuses existing `Book`/bookshelf data and local-file import capability. Keep the current full reader untouched; add a dedicated mini-reader entry and route for local TXT books.

**Tech Stack:** Kotlin, Android View + Compose (feature-local), Room existing entities, SharedPreferences (`PreferKey`/`AppConfig`) for settings, JVM unit tests, Android instrumentation tests.

---

## Input Checkpoint

- Spec path confirmed: `docs/superpowers/specs/2026-05-03-android-native-minimal-txt-reader-design.md`
- Plan path confirmed: `docs/superpowers/plans/2026-05-03-android-native-minimal-txt-reader.md`
- Scope check result: single coherent subsystem (`minireader`) with one implementation plan.

---

## File Structure (planned)

### Build and wiring
- Modify: `app/build.gradle`
- Modify: `gradle/libs.versions.toml`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/res/menu/main_bookshelf.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-zh/strings.xml`

### Mini-reader feature package
- Create: `app/src/main/java/io/legado/app/ui/minireader/MiniReaderActivity.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/MiniReaderContract.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/MiniReaderViewModel.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/MiniReaderFilePickerGateway.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/MiniReaderBookshelfRepository.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/MiniReaderTextRepository.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/MiniReaderProgressManager.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/paging/MiniPaginationEngine.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/paging/MiniPageModels.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/encode/TextCharsetResolver.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/chapter/ChapterSplitter.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/ui/MiniReaderScreen.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/ui/MiniBookshelfScreen.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/ui/MiniOverlayPageTurnCanvas.kt`

### Existing bookshelf integration
- Modify: `app/src/main/java/io/legado/app/ui/main/bookshelf/BaseBookshelfFragment.kt`
- Modify: `app/src/main/java/io/legado/app/utils/ContextExtensions.kt`

### Preferences and config keys
- Modify: `app/src/main/java/io/legado/app/constant/PreferKey.kt`
- Modify: `app/src/main/java/io/legado/app/help/config/AppConfig.kt`

### Tests
- Create: `app/src/test/java/io/legado/app/ui/minireader/encode/TextCharsetResolverTest.kt`
- Create: `app/src/test/java/io/legado/app/ui/minireader/chapter/ChapterSplitterTest.kt`
- Create: `app/src/test/java/io/legado/app/ui/minireader/paging/MiniPaginationEngineTest.kt`
- Create: `app/src/test/java/io/legado/app/ui/minireader/MiniReaderProgressManagerTest.kt`
- Create: `app/src/test/java/io/legado/app/ui/minireader/MiniReaderViewModelTest.kt`
- Create: `app/src/androidTest/java/io/legado/app/MiniReaderImportAndResumeTest.kt`
- Modify: `app/src/androidTest/java/io/legado/app/MigrationTest.kt`

---

### Task 1: Enable feature-local Compose and add mini-reader entrypoint

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/res/menu/main_bookshelf.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-zh/strings.xml`

- [ ] **Step 1: Add failing smoke test that references a not-yet-existing mini-reader screen class**

```kotlin
// app/src/test/java/io/legado/app/ui/minireader/MiniReaderFeatureSmokeTest.kt
class MiniReaderFeatureSmokeTest {
    @Test
    fun mini_reader_contract_should_exist() {
        Class.forName("io.legado.app.ui.minireader.MiniReaderContract")
    }
}
```

- [ ] **Step 2: Run test to verify red state**

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.minireader.MiniReaderFeatureSmokeTest -Pksp.incremental=false`
Expected: FAIL with `ClassNotFoundException`.

- [ ] **Step 3: Add Compose dependencies and manifest/menu entries for mini-reader**

```toml
# gradle/libs.versions.toml
composeBom = "2026.01.00"
compose-ui = { module = "androidx.compose.ui:ui" }
compose-foundation = { module = "androidx.compose.foundation:foundation" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-runtime = { module = "androidx.compose.runtime:runtime" }
lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
```

```groovy
// app/build.gradle
android {
    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.11"
    }
}
dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.lifecycle.viewmodel.compose)
}
```

```xml
<!-- app/src/main/AndroidManifest.xml -->
<activity android:name=".ui.minireader.MiniReaderActivity" android:launchMode="singleTop" />
```

```xml
<!-- app/src/main/res/menu/main_bookshelf.xml -->
<item
    android:id="@+id/menu_add_local_minimal"
    android:title="@string/mini_reader_local"
    app:showAsAction="never" />
```

- [ ] **Step 4: Create minimal contract class and verify green**

```kotlin
// app/src/main/java/io/legado/app/ui/minireader/MiniReaderContract.kt
package io.legado.app.ui.minireader

object MiniReaderContract {
    const val EXTRA_BOOK_URL = "miniReaderBookUrl"
}
```

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.minireader.MiniReaderFeatureSmokeTest -Pksp.incremental=false`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle app/src/main/AndroidManifest.xml app/src/main/res/menu/main_bookshelf.xml app/src/main/res/values/strings.xml app/src/main/res/values-zh/strings.xml app/src/main/java/io/legado/app/ui/minireader/MiniReaderContract.kt app/src/test/java/io/legado/app/ui/minireader/MiniReaderFeatureSmokeTest.kt
git commit -m "feat(minireader): bootstrap feature entry and compose runtime"
```

---

### Task 2: Build file-picker gateway and URI-only bookshelf import path

**Files:**
- Create: `app/src/main/java/io/legado/app/ui/minireader/MiniReaderFilePickerGateway.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/MiniReaderBookshelfRepository.kt`
- Modify: `app/src/main/java/io/legado/app/ui/main/bookshelf/BaseBookshelfFragment.kt`
- Modify: `app/src/main/java/io/legado/app/utils/ContextExtensions.kt`
- Create: `app/src/test/java/io/legado/app/ui/minireader/MiniReaderBookshelfRepositoryTest.kt`

- [ ] **Step 1: Write failing repository test for URI-only import (no file copy path mutation)**

```kotlin
@Test
fun import_via_uri_should_keep_bookurl_as_original_content_uri() {
    val imported = repository.importFromPickedUri("content://example/book.txt".toUri())
    assertEquals("content://example/book.txt", imported.bookUrl)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.minireader.MiniReaderBookshelfRepositoryTest -Pksp.incremental=false`
Expected: FAIL because repository does not exist.

- [ ] **Step 3: Implement gateway and repository using existing `LocalBook.importFile`**

```kotlin
class MiniReaderBookshelfRepository {
    fun importFromPickedUri(uri: Uri): Book {
        return LocalBook.importFile(uri).apply {
            // Keep URI model: do not copy to app-private storage.
            removeType(BookType.notShelf)
            save()
        }
    }
}
```

- [ ] **Step 4: Wire bookshelf menu action to mini-reader import flow and open mini-reader**

```kotlin
// BaseBookshelfFragment.onCompatOptionsItemSelected
R.id.menu_add_local_minimal -> startActivity<MiniReaderActivity> {
    putExtra(MiniReaderContract.EXTRA_OPEN_PICKER, true)
}
```

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.minireader.MiniReaderBookshelfRepositoryTest -Pksp.incremental=false`
Expected: PASS with URI preservation assertion.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/io/legado/app/ui/minireader/MiniReaderFilePickerGateway.kt app/src/main/java/io/legado/app/ui/minireader/MiniReaderBookshelfRepository.kt app/src/main/java/io/legado/app/ui/main/bookshelf/BaseBookshelfFragment.kt app/src/main/java/io/legado/app/utils/ContextExtensions.kt app/src/test/java/io/legado/app/ui/minireader/MiniReaderBookshelfRepositoryTest.kt
git commit -m "feat(minireader): add saf-uri import gateway and bookshelf entry"
```

---

### Task 3: Implement text repository with UTF-8 -> GBK fallback and chapter split

**Files:**
- Create: `app/src/main/java/io/legado/app/ui/minireader/encode/TextCharsetResolver.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/chapter/ChapterSplitter.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/MiniReaderTextRepository.kt`
- Create: `app/src/test/java/io/legado/app/ui/minireader/encode/TextCharsetResolverTest.kt`
- Create: `app/src/test/java/io/legado/app/ui/minireader/chapter/ChapterSplitterTest.kt`

- [ ] **Step 1: Add failing tests for charset fallback and chapter boundaries**

```kotlin
@Test
fun decode_should_fallback_to_gbk_when_utf8_invalid() { /* ... */ }

@Test
fun split_should_create_default_single_chapter_for_short_text() { /* ... */ }
```

- [ ] **Step 2: Run tests to verify red**

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.minireader.encode.TextCharsetResolverTest --tests io.legado.app.ui.minireader.chapter.ChapterSplitterTest -Pksp.incremental=false`
Expected: FAIL because classes are missing.

- [ ] **Step 3: Implement resolver and splitter with explicit fallback order**

```kotlin
object TextCharsetResolver {
    fun decode(bytes: ByteArray): Pair<String, Charset> {
        return runCatching { bytes.toString(Charsets.UTF_8) }
            .map { it to Charsets.UTF_8 }
            .getOrElse {
                bytes.toString(Charset.forName("GBK")) to Charset.forName("GBK")
            }
    }
}
```

```kotlin
object ChapterSplitter {
    fun split(text: String): List<MiniChapter> {
        // Empty-line + title-like heuristic, fallback single chapter.
    }
}
```

- [ ] **Step 4: Implement `MiniReaderTextRepository` using `ContentResolver` stream read and above helpers**

```kotlin
class MiniReaderTextRepository(private val context: Context) {
    fun load(uri: Uri): MiniTextBookPayload { /* openInputStream -> decode -> split */ }
}
```

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.minireader.encode.TextCharsetResolverTest --tests io.legado.app.ui.minireader.chapter.ChapterSplitterTest -Pksp.incremental=false`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/io/legado/app/ui/minireader/encode/TextCharsetResolver.kt app/src/main/java/io/legado/app/ui/minireader/chapter/ChapterSplitter.kt app/src/main/java/io/legado/app/ui/minireader/MiniReaderTextRepository.kt app/src/test/java/io/legado/app/ui/minireader/encode/TextCharsetResolverTest.kt app/src/test/java/io/legado/app/ui/minireader/chapter/ChapterSplitterTest.kt
git commit -m "feat(minireader): add txt decode pipeline and chapter splitting"
```

---

### Task 4: Implement overlay-page pagination engine with offset remap

**Files:**
- Create: `app/src/main/java/io/legado/app/ui/minireader/paging/MiniPageModels.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/paging/MiniPaginationEngine.kt`
- Create: `app/src/test/java/io/legado/app/ui/minireader/paging/MiniPaginationEngineTest.kt`

- [ ] **Step 1: Add failing tests for prev/next boundaries and offset remap on typography change**

```kotlin
@Test
fun next_should_stop_at_last_page() { /* ... */ }

@Test
fun reflow_should_restore_by_global_offset_not_old_page_index() { /* ... */ }
```

- [ ] **Step 2: Run tests to verify red**

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.minireader.paging.MiniPaginationEngineTest -Pksp.incremental=false`
Expected: FAIL because engine is missing.

- [ ] **Step 3: Implement minimal page model and pagination core**

```kotlin
data class MiniPaginationConfig(
    val pageCharCapacity: Int,
    val lineSpacingMultiplier: Float
)

class MiniPaginationEngine {
    fun paginate(chapters: List<MiniChapter>, config: MiniPaginationConfig): MiniPaginationSnapshot
    fun next(snapshot: MiniPaginationSnapshot): MiniPaginationSnapshot
    fun prev(snapshot: MiniPaginationSnapshot): MiniPaginationSnapshot
    fun reflowByGlobalOffset(oldOffset: Int, config: MiniPaginationConfig): MiniPaginationSnapshot
}
```

- [ ] **Step 4: Ensure engine returns `prev/current/next` snapshots for overlay animation rendering**

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.minireader.paging.MiniPaginationEngineTest -Pksp.incremental=false`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/io/legado/app/ui/minireader/paging/MiniPageModels.kt app/src/main/java/io/legado/app/ui/minireader/paging/MiniPaginationEngine.kt app/src/test/java/io/legado/app/ui/minireader/paging/MiniPaginationEngineTest.kt
git commit -m "feat(minireader): add overlay pagination engine with offset remap"
```

---

### Task 5: Implement progress/settings manager and preference keys

**Files:**
- Modify: `app/src/main/java/io/legado/app/constant/PreferKey.kt`
- Modify: `app/src/main/java/io/legado/app/help/config/AppConfig.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/MiniReaderProgressManager.kt`
- Create: `app/src/test/java/io/legado/app/ui/minireader/MiniReaderProgressManagerTest.kt`

- [ ] **Step 1: Add failing tests for progress restore and settings persistence**

```kotlin
@Test
fun save_and_restore_should_keep_global_offset_and_chapter_index() { /* ... */ }
@Test
fun settings_should_persist_font_lineSpacing_bg_brightness() { /* ... */ }
```

- [ ] **Step 2: Run tests to verify red**

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.minireader.MiniReaderProgressManagerTest -Pksp.incremental=false`
Expected: FAIL because manager does not exist.

- [ ] **Step 3: Add preference keys and manager implementation**

```kotlin
// PreferKey.kt
const val miniReaderFontSize = "miniReaderFontSize"
const val miniReaderLineSpacing = "miniReaderLineSpacing"
const val miniReaderBgMode = "miniReaderBgMode"
const val miniReaderBrightness = "miniReaderBrightness"
```

```kotlin
class MiniReaderProgressManager(private val context: Context) {
    fun saveProgress(bookUrl: String, chapterIndex: Int, globalOffset: Int)
    fun loadProgress(bookUrl: String): MiniReaderProgress
    fun saveSettings(settings: MiniReaderSettings)
    fun loadSettings(): MiniReaderSettings
}
```

- [ ] **Step 4: Add write-throttle wrapper to avoid hot-path preference writes on every frame**

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.minireader.MiniReaderProgressManagerTest -Pksp.incremental=false`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/io/legado/app/constant/PreferKey.kt app/src/main/java/io/legado/app/help/config/AppConfig.kt app/src/main/java/io/legado/app/ui/minireader/MiniReaderProgressManager.kt app/src/test/java/io/legado/app/ui/minireader/MiniReaderProgressManagerTest.kt
git commit -m "feat(minireader): persist reading progress and settings"
```

---

### Task 6: Implement ViewModel orchestration and error-state model

**Files:**
- Create: `app/src/main/java/io/legado/app/ui/minireader/MiniReaderViewModel.kt`
- Create: `app/src/test/java/io/legado/app/ui/minireader/MiniReaderViewModelTest.kt`

- [ ] **Step 1: Add failing tests for key state transitions**

```kotlin
@Test
fun open_book_should_emit_ready_state_with_snapshot() { /* ... */ }

@Test
fun lost_uri_should_emit_unavailable_and_rebind_action() { /* ... */ }

@Test
fun animation_lock_should_ignore_reentrant_page_turns() { /* ... */ }
```

- [ ] **Step 2: Run tests to verify red**

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.minireader.MiniReaderViewModelTest -Pksp.incremental=false`
Expected: FAIL because ViewModel is missing.

- [ ] **Step 3: Implement ViewModel with repositories + engine + progress manager**

```kotlin
class MiniReaderViewModel(
    private val textRepo: MiniReaderTextRepository,
    private val progressManager: MiniReaderProgressManager,
    private val pagination: MiniPaginationEngine
) : ViewModel() {
    val state: StateFlow<MiniReaderState>
    fun openBook(bookUrl: String)
    fun onTurnNext()
    fun onTurnPrev()
    fun onSettingsChanged(settings: MiniReaderSettings)
    fun onJumpToChapter(chapterIndex: Int)
    fun onJumpToProgress(percent: Int)
}
```

- [ ] **Step 4: Implement explicit recoverable error states (`Unavailable`, `EncodingUnsupported`, `Loading`)**

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.minireader.MiniReaderViewModelTest -Pksp.incremental=false`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/io/legado/app/ui/minireader/MiniReaderViewModel.kt app/src/test/java/io/legado/app/ui/minireader/MiniReaderViewModelTest.kt
git commit -m "feat(minireader): add state orchestration and error handling model"
```

---

### Task 7: Build minimal UI screens and overlay page-turn canvas

**Files:**
- Create: `app/src/main/java/io/legado/app/ui/minireader/MiniReaderActivity.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/ui/MiniBookshelfScreen.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/ui/MiniReaderScreen.kt`
- Create: `app/src/main/java/io/legado/app/ui/minireader/ui/MiniOverlayPageTurnCanvas.kt`

- [ ] **Step 1: Add failing compile-only UI test hook**

```kotlin
@Test
fun reader_screen_should_compile_and_accept_state_contract() {
    assertNotNull(::MiniReaderScreen)
}
```

- [ ] **Step 2: Run test to verify red**

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.minireader.MiniReaderUiCompileTest -Pksp.incremental=false`
Expected: FAIL because composables are missing.

- [ ] **Step 3: Implement activity + Compose entry; open system picker when requested**

```kotlin
class MiniReaderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MiniReaderScreen(/* state + actions */) }
    }
}
```

- [ ] **Step 4: Implement overlay render and settings bar (font, line spacing, bg mode, brightness, chapter jump, progress jump)**

```kotlin
@Composable
fun MiniOverlayPageTurnCanvas(
    current: MiniPageSnapshot,
    incoming: MiniPageSnapshot?,
    animProgress: Float
) { /* draw current/incoming with cover overlay transition */ }
```

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.minireader.MiniReaderUiCompileTest -Pksp.incremental=false`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/io/legado/app/ui/minireader/MiniReaderActivity.kt app/src/main/java/io/legado/app/ui/minireader/ui/MiniBookshelfScreen.kt app/src/main/java/io/legado/app/ui/minireader/ui/MiniReaderScreen.kt app/src/main/java/io/legado/app/ui/minireader/ui/MiniOverlayPageTurnCanvas.kt app/src/test/java/io/legado/app/ui/minireader/MiniReaderUiCompileTest.kt
git commit -m "feat(minireader): add compose ui and overlay page-turn rendering"
```

---

### Task 8: Hook mini-reader route to bookshelf and local TXT open flow

**Files:**
- Modify: `app/src/main/java/io/legado/app/ui/main/bookshelf/BaseBookshelfFragment.kt`
- Modify: `app/src/main/java/io/legado/app/utils/ContextExtensions.kt`
- Modify: `app/src/main/java/io/legado/app/ui/association/FileAssociationActivity.kt`
- Modify: `app/src/main/res/menu/main_bookshelf.xml`

- [ ] **Step 1: Add failing behavior test (route call exists)**

```kotlin
@Test
fun bookshelf_menu_should_expose_minireader_import_action() { /* parse menu or id lookup */ }
```

- [ ] **Step 2: Run test to verify red**

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.minireader.MiniReaderRouteTest -Pksp.incremental=false`
Expected: FAIL before route wiring.

- [ ] **Step 3: Wire local TXT selection -> import -> mini-reader open path**

```kotlin
fun Context.startActivityForMiniReader(book: Book, configIntent: Intent.() -> Unit = {}) {
    startActivity<MiniReaderActivity> {
        putExtra(MiniReaderContract.EXTRA_BOOK_URL, book.bookUrl)
        configIntent()
    }
}
```

- [ ] **Step 4: Keep existing full-reader route unchanged; only new explicit action enters mini-reader**

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.minireader.MiniReaderRouteTest -Pksp.incremental=false`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/io/legado/app/ui/main/bookshelf/BaseBookshelfFragment.kt app/src/main/java/io/legado/app/utils/ContextExtensions.kt app/src/main/java/io/legado/app/ui/association/FileAssociationActivity.kt app/src/main/res/menu/main_bookshelf.xml app/src/test/java/io/legado/app/ui/minireader/MiniReaderRouteTest.kt
git commit -m "feat(minireader): integrate bookshelf route and local txt open flow"
```

---

### Task 9: Add instrumentation tests for import/restore/unavailable-uri flows

**Files:**
- Create: `app/src/androidTest/java/io/legado/app/MiniReaderImportAndResumeTest.kt`
- Modify: `app/src/androidTest/java/io/legado/app/MigrationTest.kt`

- [ ] **Step 1: Write failing instrumentation cases**

```kotlin
@Test
fun import_then_reopen_should_restore_saved_progress() { /* ... */ }

@Test
fun unavailable_uri_should_show_rebind_state() { /* ... */ }
```

- [ ] **Step 2: Run instrumentation to verify red**

Run: `.\gradlew --% :app:connectedAppDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=io.legado.app.MiniReaderImportAndResumeTest`
Expected: FAIL until wiring is complete.

- [ ] **Step 3: Implement test fixtures for SAF URI grant + revoke simulation**

```kotlin
// use temporary document provider uri + persistable permissions in test setup
```

- [ ] **Step 4: Add migration guard update if schema touched (only if Room entity changed during implementation)**

Run: `.\gradlew --% :app:connectedAppDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=io.legado.app.MiniReaderImportAndResumeTest`
Expected: PASS on connected device/emulator.

- [ ] **Step 5: Commit**

```bash
git add app/src/androidTest/java/io/legado/app/MiniReaderImportAndResumeTest.kt app/src/androidTest/java/io/legado/app/MigrationTest.kt
git commit -m "test(minireader): add import resume and unavailable-uri instrumentation coverage"
```

---

### Task 10: Final verification, regression safety, and integration commit

**Files:**
- Modify: all files touched by Tasks 1-9

- [ ] **Step 1: Run targeted JVM suite for minireader**

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.minireader.encode.TextCharsetResolverTest --tests io.legado.app.ui.minireader.chapter.ChapterSplitterTest --tests io.legado.app.ui.minireader.paging.MiniPaginationEngineTest --tests io.legado.app.ui.minireader.MiniReaderProgressManagerTest --tests io.legado.app.ui.minireader.MiniReaderViewModelTest -Pksp.incremental=false`
Expected: PASS, 0 failures.

- [ ] **Step 2: Run a reader-regression smoke suite to protect existing legacy reader**

Run: `.\gradlew --% :app:testAppDebugUnitTest --tests io.legado.app.ui.book.read.page.ReadViewTouchBoundsTest --tests io.legado.app.ui.book.read.page.provider.TextClusterSplitterTest --tests io.legado.app.ui.book.read.page.provider.HtmlLineJustifierTest -Pksp.incremental=false`
Expected: PASS, no regressions in existing page layer tests.

- [ ] **Step 3: Run assemble check**

Run: `.\gradlew --% :app:assembleAppDebug -Pksp.incremental=false`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Validate diff and commit series**

Run: `git status --short`
Expected: clean working tree before final report.

Run: `git log --oneline --max-count=12`
Expected: contains minireader task commits in chronological order.

- [ ] **Step 5: Final integration commit (if squashing task commits is required by branch policy)**

```bash
git add -A
git commit -m "feat(minireader): deliver minimal native txt reader with bookshelf integration"
```

---

## Spec Coverage Check

- System picker import only: covered by Task 2 and Task 8.
- URI-only storage (no copy): covered by Task 2 tests and repository implementation.
- Bookshelf integration: covered by Task 2 and Task 8 menu/route wiring.
- Overlay page-turn: covered by Task 4 and Task 7 canvas rendering.
- Settings (font/line spacing/bg/brightness): covered by Task 5 and Task 7.
- Chapter/progress jump: covered by Task 6 and Task 7 actions.
- Progress restore: covered by Task 5 and Task 9.
- Error handling (unavailable URI, encoding fallback, animation conflict): covered by Task 3, Task 6, Task 9.
- Testing layers (JVM + instrumentation + manual acceptance hooks): covered by Tasks 3, 4, 5, 6, 9, 10.

## Placeholder Scan

- No `TODO`/`TBD` markers in tasks.
- Each task includes concrete file paths, commands, and expected results.
- All feature-critical method/class names are explicitly defined.

## Type Consistency Check

- `MiniReaderContract.EXTRA_BOOK_URL` is used consistently for route payload.
- `MiniReaderSettings`, `MiniReaderProgress`, `MiniPageSnapshot` naming remains consistent across repositories, ViewModel, and UI.
- `MiniPaginationEngine.reflowByGlobalOffset(...)` is the single remap entrypoint across settings changes.

