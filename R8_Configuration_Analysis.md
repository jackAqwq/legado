# R8 Configuration Analysis (legado)

## Build Configuration Snapshot
- AGP: 8.13.2 (from `gradle/libs.versions.toml`)
- Gradle wrapper: 8.14.4 (from `gradle/wrapper/gradle-wrapper.properties`)
- minSdk/targetSdk: 35/36 (from `app/build.gradle`)
- Shrink config: release uses `minifyEnabled true`, `shrinkResources true`, and `proguard-rules.pro` + `cronet-proguard-rules.pro`.
- `gradle.properties` currently includes `ksp.incremental=false` (local workspace change)

## Actionable Keep Rule Findings

### High-priority refinements
1. `-keep public class * extends android.view.View { ... }` ✅ implemented
- Previous issue: broad global keep on all custom views, including constructors/getters/setters for every view subclass.
- Landed change: replaced global keep with scoped package keeps for XML-inflated custom view families:
  - `io.legado.app.ui.widget.**`
  - `io.legado.app.ui.book.read.**`
  - `io.legado.app.ui.book.read.page.**`
  - `io.legado.app.ui.book.manga.recyclerview.**`
  - `io.legado.app.lib.theme.view.**`
  - plus specific video widgets: `FloatingPlayer` / `VideoPlayer`
- Verification: `:app:assembleAppRelease` passed after replacement.

2. `-keep class org.jsoup.** { *; }` ✅ partially optimized
- Issue: broad package-wide keep for external library.
- Constraint found: runtime JS rules in `assets/defaultData/*.json` reference `org.jsoup.*` APIs by class/member names; direct removal can break script compatibility.
- Landed change: switched to `-keep,allowoptimization class org.jsoup.** { *; }` to keep runtime string-based API compatibility while allowing R8 method-level optimization.
- Verification: `:app:testAppDebugUnitTest --tests "io.legado.app.manifest.ProguardViewKeepRulesTest"` and `:app:assembleAppRelease` passed.

3. `-keep class okhttp3.* { *; }` + `-keep class okio.* { *; }` ✅ implemented
- Landed change: removed package-wide keeps.
- Verification: `:app:assembleAppRelease` passed.

4. `-keep class com.jayway.jsonpath.* { *; }` ✅ implemented
- Landed change: removed package-wide keep.
- Verification: `:app:assembleAppRelease` passed.

5. `-keep class androidx.documentfile.provider.TreeDocumentFile { <init>(...); }` ✅ implemented
- Previous issue: framework internals keep for constructor reflection.
- Landed change: `FileDoc.asDocumentFile()` now calls public `DocumentFile.fromTreeUri(...)` and the reflective constructor lookup was removed, so the keep rule was deleted.
- Verification: source-guard test added for `FileDocExtensions.kt` plus release build verification.

### Medium-priority refinements
1. AppCompat menu internals (`SubMenuBuilder`, `MenuBuilder`, `Toolbar#mNavButtonView`)
- Action: preserve only where reflection utilities are used; isolate into dedicated block comments with code references.

2. Throwable name preservation
- Action: verify crash/reporting pipeline requirement; if not mandatory, consider relaxing to allow better shrinking.

3. GSYVideoPlayer full package keep ✅ implemented
- Previous issue: broad package keep on the external video library.
- Landed change: removed the package-wide `com.shuyu.gsyvideoplayer.**` keep and kept only app-owned wrapper classes.
- Verification: `:app:testAppDebugUnitTest --tests "io.legado.app.manifest.ProguardViewKeepRulesTest"` plus release build verification.

## Suggested Execution Order
1. Validate key runtime smoke paths on device/emulator:
   - main/bookshelf/explore/rss tabs
   - read/read-rss/search/searchContent
   - source edit/import/file picker/document tree
   - video/audio playback
2. Evaluate further `org.jsoup.**` narrowing with staged tests (candidate: split by package/API surfaces actually referenced by built-in JS rules and app code).
3. Re-evaluate appcompat internals (`Toolbar/MenuBuilder`) and remaining broad library keeps (`GSYVideoPlayer`, if further shrinkage is still worth the risk).
4. If stable, continue narrowing package-level keeps to class/member-level where possible.

## Verification Advice
- Focus test coverage on: import/open/read path, source parsing, rss read, file picker/document tree, media playback.
- Run UI automation on screens using custom reflection hooks before finalizing keep rule cleanup.
