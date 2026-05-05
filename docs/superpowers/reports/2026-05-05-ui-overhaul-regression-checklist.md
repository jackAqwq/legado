# 2026-05-05 UI Overhaul Regression Checklist

## Scope
- Branch: `feat/minireader-20260503`
- Goal: complete one-shot UI unification for main shell + reading domain + high-frequency dialogs + bottom nav icon semantics

## Automated Gates
- [x] `:app:testAppDebugUnitTest --tests io.legado.app.ui.theme.UiThemeEngineTest`
- [x] `:app:testAppDebugUnitTest --tests io.legado.app.ui.main.MainShellNavigationTest`
- [x] `:app:testAppDebugUnitTest --tests io.legado.app.ui.read.ReadUiStyleContractTest`
- [x] `:app:testAppDebugUnitTest --tests io.legado.app.ui.dialog.AppDialogStyleContractTest`
- [x] `:app:testAppDebugUnitTest --tests io.legado.app.ui.theme.IconStyleContractTest`
- [x] `:app:assembleAppDebug`

## Main Entry Surfaces
- [x] `fragment_bookshelf1.xml` uses `@color/ui_surface` / `@drawable/bg_app_card_surface`
- [x] `fragment_bookshelf2.xml` uses `@color/ui_surface` / `@drawable/bg_app_card_surface`
- [x] `fragment_explore.xml` uses `@color/ui_surface` / `@drawable/bg_app_card_surface`
- [x] `fragment_rss.xml` uses `@color/ui_surface` / `@drawable/bg_app_card_surface`
- [x] `fragment_my_config.xml` uses `@color/ui_surface` / `@drawable/bg_app_card_surface`

## Reading Domain
- [x] `activity_book_read.xml` root uses `@color/ui_surface`
- [x] `activity_book_read.xml` navigation bar uses `@color/ui_surface_variant`
- [x] `ReadMenu.kt` uses `UiThemeEngine + UiThemeSnapshotInput`
- [x] `SearchMenu.kt` uses `UiThemeEngine + UiThemeSnapshotInput`
- [x] `view_read_menu.xml` no remaining `@color/primaryText` in active nodes
- [x] `view_search_menu.xml` no remaining `@color/primaryText` in active nodes

## Dialog System (High Frequency)
- [x] `dialog_text_view.xml` uses `@color/ui_surface` + `@color/ui_on_surface`
- [x] `dialog_variable.xml` uses `@color/ui_surface` + `@drawable/bg_app_card_surface`
- [x] `dialog_url_option_edit.xml` uses `@color/ui_surface` + `@drawable/bg_app_card_surface`
- [x] `dialog_wait.xml` uses `@drawable/bg_app_card_surface` + `@color/ui_on_surface`
- [x] `popup_action_menu.xml` uses `@drawable/bg_app_card_surface` + `@color/ui_on_surface`
- [x] `TextDialog.kt` toolbar color from snapshot `uiSnapshot.primaryColor`
- [x] `VariableDialog.kt` toolbar color from snapshot `uiSnapshot.primaryColor`

## Iconography
- [x] bottom nav selected icons (`*_s.xml`) use `@color/ui_primary`
- [x] bottom nav unselected icons (`*_e.xml`) use `@color/ui_on_surface_muted`
- [x] removed hardcoded `#2f45a6` in bottom nav icon vectors

## Risks / Notes
- This checklist currently validates source-level contracts and build gates.
- Manual screenshot-based visual QA on physical devices (normal + eInk) can be run as a follow-up pass.
