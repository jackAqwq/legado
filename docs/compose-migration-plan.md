# Compose Migration Plan

## Baseline

- `app/src/main/res/layout`: 215 XML files
- Layout categories:
  - `activity_*`: 40
  - `dialog_*`: 60
  - `fragment_*`: 10
  - `item_*`: 67
  - `view_*`: 25
  - Other popup/video/floating/layout variants: 13
- UI entry classes in `app/src/main/java/io/legado/app/ui`: the project is still primarily `BaseActivity` / `VMBaseActivity` / `BaseFragment` / `BaseDialogFragment` driven.

## Migration Strategy

### Phase 0: Foundation

- Reuse the existing runtime theme system (`ThemeStore`, `backgroundColor`, `primaryColor`, `accentColor`) instead of inventing a parallel Compose design system.
- Add shared Compose hosting infrastructure for dialogs first, then activities/fragments.
- Keep migration incremental and interoperable. A migrated screen must be able to coexist with legacy View code.

### Phase 1: Low-Risk Dialogs

- Target simple confirmation/loading/form dialogs first.
- Completed:
  - `PageKeyDialog`
  - `WaitDialog`
  - `OpenUrlConfirmDialog`
  - `CheckSourceConfig`
  - `MangaEpaperDialog`
  - `MangaColorFilterDialog`
  - `AudioSkipCredits`
  - `AutoReadDialog`
  - `PaddingConfigDialog`
  - `TipConfigDialog`
  - `GroupEditDialog`
  - `ReadAloudDialog`
  - `SearchScopeDialog`
  - `ReadStyleDialog`
  - `ChangeBookSourceDialog`
  - `ChangeChapterSourceDialog`
  - `RssSourceEditActivity`
  - `BookSourceEditActivity`
  - `UpdateDialog`
  - `VerificationCodeDialog`
  - `CoverRuleConfigDialog`
  - `BookSourceActivity`
  - `RssSourceActivity`
  - `AddToBookshelfDialog`
  - `BookmarkDialog`
  - `SearchActivity`
  - `CacheActivity`
  - `AboutActivity`
  - `ReadRecordActivity`
  - `TextDialog`
  - `MainActivity`
  - `AudioPlayActivity`
  - `VideoPlayerActivity`
  - `WebViewActivity`
  - `FileManageActivity`
  - `ReadMangaActivity`
  - `BookInfoActivity`
  - `ReadBookActivity`
  - `SearchContentActivity`
  - `ConfigActivity`
  - `ImportBookActivity`

- Remaining high-value screens:
  - `CodeEditActivity` ✅ (migrated to ComposeBinding)
  - `SourceDebugActivity` ✅ (migrated to ComposeBinding)
  - `RssSourceDebugActivity` ✅ (migrated to ComposeBinding)
  - `TocActivity` ✅ (migrated to ComposeBinding)
  - `ReadRssActivity` ✅ (migrated to ComposeBinding)
  - `RssSortActivity` ✅ (migrated to ComposeBinding)

### Phase 2: Shared Dialog Shells and Picker Surfaces

- Replace repeated `dialog_recycler_view` usages with Compose list scaffolds while keeping existing data/viewmodel code.
- Migrate repeated picker/selection rows from XML items to Compose list rows.

### Phase 3: Simple Fragments and Activities

- Prioritize screens with limited custom drawing and minimal WebView/editor dependence.
- Candidate groups:
  - Settings/support activities
  - RSS/article management screens
  - File management and import screens

### Phase 4: Complex Editor and Source Management Screens

- `BookSourceEditActivity`, `RssSourceEditActivity`, `CodeEditActivity`, login/import/edit dialogs.
- These depend on custom editors, toolbars, menus, and large legacy adapter stacks, so they should only start after list/dialog primitives are stable.

### Phase 5: Reader/Media/Core Rendering Screens

- Recently completed in this phase: `ReadBookActivity`, `ReadMangaActivity`, `AudioPlayActivity`, `VideoPlayerActivity`, `WebViewActivity`.
- These areas combine custom rendering, touch systems, system bars, media, and WebView integration, so they still require device-side regression validation after compile verification.

## Current Infrastructure

- `app/src/main/java/io/legado/app/ui/compose/LegadoComposeTheme.kt`
  - Compose theme bridge backed by existing runtime theme colors.
- `app/src/main/java/io/legado/app/base/ComposeDialogFragment.kt`
  - Shared dialog host for new Compose dialogs.

## Constraints

- This migration is UI-only. ViewModel/business logic/database/network layers stay unchanged unless UI wiring requires a minimal change.
- RecyclerView-heavy and custom-view-heavy screens should migrate only after shared Compose primitives exist.
- WebView- and reader-core screens should be the final stages, not the first.
