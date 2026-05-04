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
1. AppCompat menu internals (`SubMenuBuilder`, `MenuBuilder`, `Toolbar#mNavButtonView`) ✅ implemented
- Previous issue: keep rules existed only to support reflection and class-name string checks in menu/navigation icon code paths.
- Landed change:
  - `Menu.applyOpenTint()` switched from reflection + class-name string checks to typed `MenuBuilder` path plus normal menu iteration.
  - `ChangeBookSourceDialog.initNavigationView()` switched from `Toolbar#mNavButtonView` reflection to direct `navigationIcon` tinting.
  - Removed keep rules for `Toolbar#mNavButtonView`, `SubMenuBuilder`, and `MenuBuilder` reflective methods.
- Verification: source-guard tests added for `MenuExtensions.kt` and `ChangeBookSourceDialog.kt`, plus keep regression test and release build verification.

2. Throwable member keep ✅ implemented
- Previous issue: `-keepclassmembernames,allowobfuscation class * extends java.lang.Throwable{*;}` preserved member names for all exception types without a matching reflection dependency.
- Landed change: removed Throwable member keep and retained only `-keepnames class * extends java.lang.Throwable` for crash/log readability.
- Verification: `:app:testAppDebugUnitTest --tests "io.legado.app.manifest.ProguardViewKeepRulesTest"` plus release build verification.

3. GSYVideoPlayer full package keep ✅ implemented
- Previous issue: broad package keep on the external video library.
- Landed change: removed the package-wide `com.shuyu.gsyvideoplayer.**` keep and kept only app-owned wrapper classes.
- Verification: `:app:testAppDebugUnitTest --tests "io.legado.app.manifest.ProguardViewKeepRulesTest"` plus release build verification.

4. LiveEventBus + Sora broad keeps ✅ implemented
- Previous issue:
  - App-level keep on `androidx.lifecycle.LiveData` / `SafeIterableMap` internals.
  - Broad keeps on `org.eclipse.tm4e.**` and `org.joni.**`.
- Landed change:
  - Removed the LiveData/SafeIterableMap app-level keep block.
  - Removed broad `tm4e/joni` keeps.
  - Kept library-driven consumer rules (LiveEventBus internal `ExternalLiveData` reflection path and Sora TextMate `proguard.txt` targeted keeps).
- Verification: keep regression test + release build verification.

5. `@Keep`-annotated app classes + ExoPlayer legacy reflection keep ✅ implemented
- Previous issue:
  - Explicit keeps duplicated existing `@Keep` annotation coverage:
    - `**.help.http.CookieStore`
    - `**.help.CacheManager`
    - `**.help.http.StrResponse`
    - `io.legado.app.api.ReturnData`
  - Legacy keep for `CacheDataSource$Factory#upstreamDataSourceFactory` remained after reflection code was removed.
- Landed change:
  - Removed the four explicit app keep rules above and rely on `androidx.annotation.Keep` consumer rules.
  - Removed `CacheDataSource$Factory#upstreamDataSourceFactory` keep and cleaned stale reflection comments in `ExoPlayerHelper`.
  - Added guard tests to keep these deletions from regressing.
- Verification:
  - `:app:testAppDebugUnitTest --tests "io.legado.app.manifest.ProguardViewKeepRulesTest" --tests "io.legado.app.manifest.ExoPlayerHelperSourceGuardTest"`
  - `:app:assembleAppRelease`

6. Hutool keep rules (core/crypto) narrowed from package-wide to used classes ✅ implemented
- Previous issue:
  - Broad keeps retained large swaths of Hutool:
    - `cn.hutool.core.util.**`
    - `cn.hutool.core.codec.**`
    - `cn.hutool.crypto.**`
- Landed change:
  - Replaced package-wide keeps with class-level keeps for currently used APIs in app code paths:
    - core: `Base64`, `PercentCodec`, `RFC3986`, `URLDecoder`, `URLEncodeUtil`, `HexUtil`, `Validator`
    - crypto: `KeyUtil`, `DigestUtil`, `Digester`, `HMac`, `KeyType`, `AsymmetricCrypto`, `Sign`, `AES`, `SymmetricCrypto`
  - Added keep regression assertions to ensure broad Hutool rules do not regress.
- Verification:
  - `:app:testAppDebugUnitTest --tests "io.legado.app.manifest.ProguardViewKeepRulesTest"`
  - `:app:assembleAppRelease`

7. XML custom-view keep rules narrowed from package-level to explicit class list ✅ implemented
- Previous issue:
  - Broad package keeps retained all classes in:
    - `io.legado.app.ui.widget.**`
    - `io.legado.app.ui.book.read.**`
    - `io.legado.app.ui.book.read.page.**`
    - `io.legado.app.ui.book.manga.recyclerview.**`
    - `io.legado.app.lib.theme.view.**`
- Landed change:
  - Replaced package-level keeps with explicit class-level keeps generated from actual XML tags under `res/layout` and `res/xml` (50 classes total).
  - Preserved existing `"{ *; }"` member retention per class for behavior neutrality in this phase.
  - Updated keep regression assertions to lock out broad package keeps and validate representative explicit classes.
- Verification:
  - `:app:testAppDebugUnitTest --tests "io.legado.app.manifest.ProguardViewKeepRulesTest"`
  - `:app:assembleAppRelease`

## Suggested Execution Order
1. Validate key runtime smoke paths on device/emulator:
   - main/bookshelf/explore/rss tabs
   - read/read-rss/search/searchContent
   - source edit/import/file picker/document tree
   - video/audio playback
2. Evaluate further `org.jsoup.**` narrowing with staged tests (candidate: split by package/API surfaces actually referenced by built-in JS rules and app code).
3. Re-evaluate remaining app package-wide keeps (`io.legado.app.ui.widget/read/page/...`) with source-coupled XML/reflective usage checks.
4. If stable, continue narrowing package-level keeps to class/member-level where possible.

## Verification Advice
- Focus test coverage on: import/open/read path, source parsing, rss read, file picker/document tree, media playback.
- Run UI automation on screens using custom reflection hooks before finalizing keep rule cleanup.
