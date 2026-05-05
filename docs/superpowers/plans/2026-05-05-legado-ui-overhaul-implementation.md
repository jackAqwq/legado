# Legado One-shot UI Overhaul Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 一次性完成 legado 的新深色沉浸 UI 体系替换，覆盖主壳、主页面、阅读域和弹窗体系，并通过本地与 CI 门禁。

**Architecture:** 采用“设计系统层 -> 主壳层 -> 业务表层 -> 兼容桥接层”四层重构。先建立 token 与主题快照，再替换主壳和基础组件，最后切换主页面、阅读域与弹窗域，保留降级开关保证一次上线可控。

**Tech Stack:** Android (Kotlin), AppCompat + Material Components, XML View system, ThemeStore/ThemeConfig, Gradle + GitHub Actions.

---

## File Structure Map (before tasks)

- 新建设计系统与壳层文件：
  - `app/src/main/java/io/legado/app/lib/theme/system/UiThemeSnapshot.kt`
  - `app/src/main/java/io/legado/app/lib/theme/system/UiThemeEngine.kt`
  - `app/src/main/java/io/legado/app/lib/theme/system/UiThemeDefaults.kt`
  - `app/src/main/java/io/legado/app/ui/main/shell/MainShellNavigator.kt`
  - `app/src/main/java/io/legado/app/ui/main/shell/MainShellTabHost.kt`
- 新建通用 UI 组件：
  - `app/src/main/java/io/legado/app/ui/widget/surface/AppSurfaceStyle.kt`
  - `app/src/main/java/io/legado/app/ui/widget/surface/AppCardStyle.kt`
  - `app/src/main/java/io/legado/app/ui/widget/dialog/AppDialogShellStyle.kt`
- 新建/修改主题资源：
  - `app/src/main/res/values/themes_ui_overhaul.xml`
  - `app/src/main/res/values-night/themes_ui_overhaul.xml`
  - `app/src/main/res/values/colors_ui_overhaul.xml`
  - `app/src/main/res/values-night/colors_ui_overhaul.xml`
  - `app/src/main/res/color/main_nav_item_colors.xml`
  - `app/src/main/res/drawable/bg_main_shell_nav.xml`
  - `app/src/main/res/drawable/bg_app_card_surface.xml`
- 主壳与主页面：
  - `app/src/main/res/layout/activity_main.xml`（重构）
  - `app/src/main/java/io/legado/app/ui/main/MainActivity.kt`（重构）
  - `app/src/main/res/layout/fragment_bookshelf1.xml`
  - `app/src/main/res/layout/fragment_bookshelf2.xml`
  - `app/src/main/res/layout/fragment_explore.xml`
  - `app/src/main/res/layout/fragment_rss.xml`
  - `app/src/main/res/layout/fragment_my_config.xml`
- 阅读与弹窗：
  - `app/src/main/res/layout/activity_read_book.xml`（若项目使用同名布局则改；否则改对应阅读主布局）
  - `app/src/main/java/io/legado/app/ui/book/read/ReadBookActivity.kt`
  - `app/src/main/java/io/legado/app/ui/widget/dialog/TextDialog.kt`
  - `app/src/main/java/io/legado/app/ui/widget/dialog/TextListDialog.kt`
  - `app/src/main/java/io/legado/app/ui/widget/dialog/VariableDialog.kt`
  - `app/src/main/java/io/legado/app/ui/widget/dialog/UrlOptionDialog.kt`
  - `app/src/main/java/io/legado/app/ui/widget/dialog/BottomWebViewDialog.kt`
  - `app/src/main/java/io/legado/app/ui/widget/dialog/WaitDialog.kt`
- 图标与字体资源：
  - `app/src/main/res/drawable/ic_bottom_books*.xml`
  - `app/src/main/res/drawable/ic_bottom_explore*.xml`
  - `app/src/main/res/drawable/ic_bottom_rss_feed*.xml`
  - `app/src/main/res/drawable/ic_bottom_person*.xml`
  - `app/src/main/res/font/`（新增字体资源时）
- 测试与守护：
  - `app/src/test/java/io/legado/app/ui/theme/UiThemeEngineTest.kt`
  - `app/src/test/java/io/legado/app/ui/main/MainShellNavigationTest.kt`
  - `app/src/test/java/io/legado/app/ui/dialog/AppDialogStyleContractTest.kt`
  - `app/src/test/java/io/legado/app/ui/read/ReadUiStyleContractTest.kt`
  - `app/src/test/java/io/legado/app/manifest/UiOverhaulSourceGuardTest.kt`

---

### Task 1: 建立 Design Token 与主题快照引擎

**Files:**
- Create: `app/src/main/java/io/legado/app/lib/theme/system/UiThemeSnapshot.kt`
- Create: `app/src/main/java/io/legado/app/lib/theme/system/UiThemeEngine.kt`
- Create: `app/src/main/java/io/legado/app/lib/theme/system/UiThemeDefaults.kt`
- Modify: `app/src/main/java/io/legado/app/lib/theme/MaterialValueHelper.kt`
- Test: `app/src/test/java/io/legado/app/ui/theme/UiThemeEngineTest.kt`

- [ ] **Step 1: 写失败测试（token 解析/回退）**

```kotlin
// app/src/test/java/io/legado/app/ui/theme/UiThemeEngineTest.kt
class UiThemeEngineTest {

    @Test
    fun `buildSnapshot should return deep style defaults when input missing`() {
        val snapshot = UiThemeEngine.buildSnapshot(
            UiThemeEngine.Input(
                primary = null,
                accent = null,
                background = null,
                bottomBackground = null,
                isDark = true,
                isEInk = false
            )
        )
        assertEquals("deep", snapshot.styleName)
        assertTrue(snapshot.surface != 0)
        assertTrue(snapshot.onSurface != 0)
    }

    @Test
    fun `buildSnapshot should preserve explicit accent when provided`() {
        val snapshot = UiThemeEngine.buildSnapshot(
            UiThemeEngine.Input(
                primary = 0xFF102840.toInt(),
                accent = 0xFF4DA3FF.toInt(),
                background = 0xFF0F1622.toInt(),
                bottomBackground = 0xFF121A27.toInt(),
                isDark = true,
                isEInk = false
            )
        )
        assertEquals(0xFF4DA3FF.toInt(), snapshot.accent)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.ui.theme.UiThemeEngineTest"`
Expected: FAIL，提示 `UiThemeEngine` 或 `UiThemeSnapshot` 未定义。

- [ ] **Step 3: 最小实现 token 快照与引擎**

```kotlin
// UiThemeSnapshot.kt
package io.legado.app.lib.theme.system

data class UiThemeSnapshot(
    val styleName: String,
    val primary: Int,
    val accent: Int,
    val surface: Int,
    val surfaceVariant: Int,
    val onSurface: Int,
    val outline: Int,
    val readerTitleSizeSp: Float,
    val readerBodySizeSp: Float,
    val radiusM: Float,
)

// UiThemeEngine.kt
package io.legado.app.lib.theme.system

object UiThemeEngine {
    data class Input(
        val primary: Int?,
        val accent: Int?,
        val background: Int?,
        val bottomBackground: Int?,
        val isDark: Boolean,
        val isEInk: Boolean,
    )

    fun buildSnapshot(input: Input): UiThemeSnapshot {
        val defaults = UiThemeDefaults.deepImmerse()
        return defaults.copy(
            primary = input.primary ?: defaults.primary,
            accent = input.accent ?: defaults.accent,
            surface = input.background ?: defaults.surface,
            surfaceVariant = input.bottomBackground ?: defaults.surfaceVariant,
        )
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `./gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.ui.theme.UiThemeEngineTest"`
Expected: PASS。

- [ ] **Step 5: 提交**

```bash
git add app/src/main/java/io/legado/app/lib/theme/system app/src/test/java/io/legado/app/ui/theme/UiThemeEngineTest.kt app/src/main/java/io/legado/app/lib/theme/MaterialValueHelper.kt
git commit -m "feat(ui): add semantic theme snapshot engine"
```

---

### Task 2: 主壳容器与导航壳替换（MainActivity + main layout）

**Files:**
- Modify: `app/src/main/res/layout/activity_main.xml`
- Modify: `app/src/main/java/io/legado/app/ui/main/MainActivity.kt`
- Create: `app/src/main/java/io/legado/app/ui/main/shell/MainShellNavigator.kt`
- Create: `app/src/main/java/io/legado/app/ui/main/shell/MainShellTabHost.kt`
- Modify: `app/src/main/java/io/legado/app/lib/theme/view/ThemeBottomNavigationVIew.kt`
- Test: `app/src/test/java/io/legado/app/ui/main/MainShellNavigationTest.kt`

- [ ] **Step 1: 写失败测试（tab 路由与返回链）**

```kotlin
class MainShellNavigationTest {
    @Test
    fun `reselect bookshelf should trigger top scroll event`() {
        val nav = MainShellNavigator()
        nav.onTabReselected(MainShellNavigator.Tab.BOOKSHELF)
        assertTrue(nav.consumeBookshelfReselectEvent())
    }

    @Test
    fun `back from non-home tab should navigate to home first`() {
        val nav = MainShellNavigator()
        nav.select(MainShellNavigator.Tab.RSS)
        assertEquals(MainShellNavigator.BackResult.SWITCHED_HOME, nav.onBackPressed())
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.ui.main.MainShellNavigationTest"`
Expected: FAIL，`MainShellNavigator` 未定义。

- [ ] **Step 3: 实现主壳导航与布局替换最小实现**

```kotlin
// MainShellNavigator.kt
class MainShellNavigator {
    enum class Tab { BOOKSHELF, DISCOVERY, RSS, MY }
    enum class BackResult { SWITCHED_HOME, SHOULD_EXIT }

    private var current = Tab.BOOKSHELF
    private var bookshelfReselect = false

    fun select(tab: Tab) { current = tab }
    fun onTabReselected(tab: Tab) { if (tab == Tab.BOOKSHELF) bookshelfReselect = true }
    fun consumeBookshelfReselectEvent(): Boolean = bookshelfReselect.also { bookshelfReselect = false }
    fun onBackPressed(): BackResult = if (current != Tab.BOOKSHELF) {
        current = Tab.BOOKSHELF
        BackResult.SWITCHED_HOME
    } else BackResult.SHOULD_EXIT
}
```

并在 `activity_main.xml` 中引入壳容器层次（top container/content container/nav container），在 `MainActivity` 接入 `MainShellNavigator`。

- [ ] **Step 4: 运行测试确认通过**

Run: `./gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.ui.main.MainShellNavigationTest"`
Expected: PASS。

- [ ] **Step 5: 提交**

```bash
git add app/src/main/res/layout/activity_main.xml app/src/main/java/io/legado/app/ui/main/MainActivity.kt app/src/main/java/io/legado/app/ui/main/shell app/src/main/java/io/legado/app/lib/theme/view/ThemeBottomNavigationVIew.kt app/src/test/java/io/legado/app/ui/main/MainShellNavigationTest.kt
git commit -m "feat(ui-shell): replace main shell navigation container"
```

---

### Task 3: 主题资源与全局组件底座（颜色/圆角/卡片/按钮）

**Files:**
- Create: `app/src/main/res/values/colors_ui_overhaul.xml`
- Create: `app/src/main/res/values-night/colors_ui_overhaul.xml`
- Create: `app/src/main/res/values/themes_ui_overhaul.xml`
- Create: `app/src/main/res/values-night/themes_ui_overhaul.xml`
- Create: `app/src/main/res/color/main_nav_item_colors.xml`
- Create: `app/src/main/res/drawable/bg_main_shell_nav.xml`
- Create: `app/src/main/res/drawable/bg_app_card_surface.xml`
- Modify: `app/src/main/res/values/styles.xml`
- Modify: `app/src/main/res/values-night/styles.xml`
- Test: `app/src/test/java/io/legado/app/manifest/UiOverhaulSourceGuardTest.kt`

- [ ] **Step 1: 写失败测试（资源引用守护）**

```kotlin
class UiOverhaulSourceGuardTest {
    @Test
    fun `styles should reference ui overhaul theme overlays`() {
        val styles = File("src/main/res/values/styles.xml").readText()
        assertTrue(styles.contains("themes_ui_overhaul"))
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.manifest.UiOverhaulSourceGuardTest"`
Expected: FAIL，`themes_ui_overhaul` 尚未接入。

- [ ] **Step 3: 添加新主题资源并接入 styles**

```xml
<!-- values/colors_ui_overhaul.xml -->
<resources>
    <color name="ui_surface">#0F1724</color>
    <color name="ui_surface_variant">#151F30</color>
    <color name="ui_on_surface">#E6EEFF</color>
    <color name="ui_primary">#5BA2FF</color>
    <color name="ui_outline">#2A3A55</color>
</resources>
```

并在 `styles.xml` 将关键通用样式（toolbar/popup/menu item/card）引用新 token 资源。

- [ ] **Step 4: 运行测试确认通过**

Run: `./gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.manifest.UiOverhaulSourceGuardTest"`
Expected: PASS。

- [ ] **Step 5: 提交**

```bash
git add app/src/main/res/values/colors_ui_overhaul.xml app/src/main/res/values-night/colors_ui_overhaul.xml app/src/main/res/values/themes_ui_overhaul.xml app/src/main/res/values-night/themes_ui_overhaul.xml app/src/main/res/color/main_nav_item_colors.xml app/src/main/res/drawable/bg_main_shell_nav.xml app/src/main/res/drawable/bg_app_card_surface.xml app/src/main/res/values/styles.xml app/src/main/res/values-night/styles.xml app/src/test/java/io/legado/app/manifest/UiOverhaulSourceGuardTest.kt
git commit -m "feat(ui): add deep-immersive design tokens and base styles"
```

---

### Task 4: 主入口四页统一（书架/发现/RSS/我的）

**Files:**
- Modify: `app/src/main/res/layout/fragment_bookshelf1.xml`
- Modify: `app/src/main/res/layout/fragment_bookshelf2.xml`
- Modify: `app/src/main/res/layout/fragment_explore.xml`
- Modify: `app/src/main/res/layout/fragment_rss.xml`
- Modify: `app/src/main/res/layout/fragment_my_config.xml`
- Modify: `app/src/main/java/io/legado/app/ui/main/bookshelf/style1/BookshelfFragment1.kt`
- Modify: `app/src/main/java/io/legado/app/ui/main/bookshelf/style2/BookshelfFragment2.kt`
- Modify: `app/src/main/java/io/legado/app/ui/main/explore/ExploreFragment.kt`
- Modify: `app/src/main/java/io/legado/app/ui/main/rss/RssFragment.kt`
- Modify: `app/src/main/java/io/legado/app/ui/main/my/MyFragment.kt`

- [ ] **Step 1: 写 UI 样式契约测试（源码守护）**

```kotlin
class MainSurfaceStyleContractTest {
    @Test
    fun `main fragments should use app surface background token`() {
        val files = listOf(
            "src/main/res/layout/fragment_bookshelf1.xml",
            "src/main/res/layout/fragment_bookshelf2.xml",
            "src/main/res/layout/fragment_explore.xml",
            "src/main/res/layout/fragment_rss.xml",
            "src/main/res/layout/fragment_my_config.xml",
        )
        files.forEach { path ->
            val text = File(path).readText()
            assertTrue(text.contains("@color/ui_surface") || text.contains("@drawable/bg_app_card_surface"))
        }
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.ui.main.MainSurfaceStyleContractTest"`
Expected: FAIL。

- [ ] **Step 3: 统一主入口页面视觉与顶栏/列表样式**

在上述 fragment 布局替换背景、卡片、分割线、间距，确保统一 token；在 fragment 代码中统一 toolbar/tint/insets 行为。

- [ ] **Step 4: 运行测试确认通过**

Run: `./gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.ui.main.MainSurfaceStyleContractTest"`
Expected: PASS。

- [ ] **Step 5: 提交**

```bash
git add app/src/main/res/layout/fragment_bookshelf1.xml app/src/main/res/layout/fragment_bookshelf2.xml app/src/main/res/layout/fragment_explore.xml app/src/main/res/layout/fragment_rss.xml app/src/main/res/layout/fragment_my_config.xml app/src/main/java/io/legado/app/ui/main/bookshelf/style1/BookshelfFragment1.kt app/src/main/java/io/legado/app/ui/main/bookshelf/style2/BookshelfFragment2.kt app/src/main/java/io/legado/app/ui/main/explore/ExploreFragment.kt app/src/main/java/io/legado/app/ui/main/rss/RssFragment.kt app/src/main/java/io/legado/app/ui/main/my/MyFragment.kt app/src/test/java/io/legado/app/ui/main/MainSurfaceStyleContractTest.kt
git commit -m "feat(ui-main): unify four main surfaces with new design system"
```

---

### Task 5: 阅读域视觉与交互壳统一

**Files:**
- Modify: `app/src/main/java/io/legado/app/ui/book/read/ReadBookActivity.kt`
- Modify: `app/src/main/java/io/legado/app/ui/book/read/ReadBookMenuLayer.kt`
- Modify: `app/src/main/java/io/legado/app/ui/book/read/TextActionMenu.kt`
- Modify: `app/src/main/java/io/legado/app/ui/book/read/config/ReadStyleDialog.kt`
- Modify: `app/src/main/res/layout/activity_read_book.xml`（或项目当前阅读主布局文件）
- Test: `app/src/test/java/io/legado/app/ui/read/ReadUiStyleContractTest.kt`

- [ ] **Step 1: 写失败测试（阅读域样式契约）**

```kotlin
class ReadUiStyleContractTest {
    @Test
    fun `read menu should use ui token naming`() {
        val code = File("src/main/java/io/legado/app/ui/book/read/ReadBookMenuLayer.kt").readText()
        assertTrue(code.contains("UiThemeSnapshot") || code.contains("ui_surface"))
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.ui.read.ReadUiStyleContractTest"`
Expected: FAIL。

- [ ] **Step 3: 实现阅读层视觉统一与交互重排**

把阅读菜单、文本操作菜单、样式配置入口接入新 token；统一按钮层级、菜单间距与状态色。

- [ ] **Step 4: 运行测试确认通过**

Run: `./gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.ui.read.ReadUiStyleContractTest"`
Expected: PASS。

- [ ] **Step 5: 提交**

```bash
git add app/src/main/java/io/legado/app/ui/book/read/ReadBookActivity.kt app/src/main/java/io/legado/app/ui/book/read/ReadBookMenuLayer.kt app/src/main/java/io/legado/app/ui/book/read/TextActionMenu.kt app/src/main/java/io/legado/app/ui/book/read/config/ReadStyleDialog.kt app/src/main/res/layout/activity_read_book.xml app/src/test/java/io/legado/app/ui/read/ReadUiStyleContractTest.kt
git commit -m "feat(ui-read): overhaul reading surface and action layers"
```

---

### Task 6: 弹窗体系统一（Text/List/Variable/Url/Web/Wait）

**Files:**
- Modify: `app/src/main/java/io/legado/app/ui/widget/dialog/TextDialog.kt`
- Modify: `app/src/main/java/io/legado/app/ui/widget/dialog/TextListDialog.kt`
- Modify: `app/src/main/java/io/legado/app/ui/widget/dialog/VariableDialog.kt`
- Modify: `app/src/main/java/io/legado/app/ui/widget/dialog/UrlOptionDialog.kt`
- Modify: `app/src/main/java/io/legado/app/ui/widget/dialog/BottomWebViewDialog.kt`
- Modify: `app/src/main/java/io/legado/app/ui/widget/dialog/WaitDialog.kt`
- Modify: `app/src/main/res/layout/dialog_text_view.xml`
- Modify: `app/src/main/res/layout/dialog_variable.xml`
- Modify: `app/src/main/res/layout/dialog_url_option_edit.xml`
- Modify: `app/src/main/res/layout/dialog_wait.xml`
- Test: `app/src/test/java/io/legado/app/ui/dialog/AppDialogStyleContractTest.kt`

- [ ] **Step 1: 写失败测试（弹窗样式契约）**

```kotlin
class AppDialogStyleContractTest {
    @Test
    fun `text dialog should use app dialog shell style`() {
        val layout = File("src/main/res/layout/dialog_text_view.xml").readText()
        assertTrue(layout.contains("@style/AppDialogShell") || layout.contains("ui_surface"))
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.ui.dialog.AppDialogStyleContractTest"`
Expected: FAIL。

- [ ] **Step 3: 统一弹窗布局与代码样式注入**

把弹窗背景、标题、按钮、内容区、间距全部接入统一样式；`TextDialog` 等代码删除散落色值，改走 token。

- [ ] **Step 4: 运行测试确认通过**

Run: `./gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.ui.dialog.AppDialogStyleContractTest"`
Expected: PASS。

- [ ] **Step 5: 提交**

```bash
git add app/src/main/java/io/legado/app/ui/widget/dialog/TextDialog.kt app/src/main/java/io/legado/app/ui/widget/dialog/TextListDialog.kt app/src/main/java/io/legado/app/ui/widget/dialog/VariableDialog.kt app/src/main/java/io/legado/app/ui/widget/dialog/UrlOptionDialog.kt app/src/main/java/io/legado/app/ui/widget/dialog/BottomWebViewDialog.kt app/src/main/java/io/legado/app/ui/widget/dialog/WaitDialog.kt app/src/main/res/layout/dialog_text_view.xml app/src/main/res/layout/dialog_variable.xml app/src/main/res/layout/dialog_url_option_edit.xml app/src/main/res/layout/dialog_wait.xml app/src/test/java/io/legado/app/ui/dialog/AppDialogStyleContractTest.kt
git commit -m "feat(ui-dialog): unify high-frequency dialog system"
```

---

### Task 7: 图标与字体系统统一

**Files:**
- Modify: `app/src/main/res/drawable/ic_bottom_books.xml`
- Modify: `app/src/main/res/drawable/ic_bottom_books_s.xml`
- Modify: `app/src/main/res/drawable/ic_bottom_books_e.xml`
- Modify: `app/src/main/res/drawable/ic_bottom_explore.xml`
- Modify: `app/src/main/res/drawable/ic_bottom_rss_feed.xml`
- Modify: `app/src/main/res/drawable/ic_bottom_person.xml`
- Create/Modify: `app/src/main/res/font/*`（按最终字体方案）
- Modify: `app/src/main/res/values/styles.xml`

- [ ] **Step 1: 写失败测试（图标色彩/状态契约）**

```kotlin
class IconStyleContractTest {
    @Test
    fun `bottom nav icons should use unified color mapping`() {
        val icon = File("src/main/res/drawable/ic_bottom_books_s.xml").readText()
        assertTrue(icon.contains("ui_primary") || icon.contains("#5BA2FF"))
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.ui.theme.IconStyleContractTest"`
Expected: FAIL。

- [ ] **Step 3: 替换底栏图标与全局字体映射**

统一底栏图标线性风格和选中/未选中颜色语义；在 styles 中接入字体族与标题/正文级别。

- [ ] **Step 4: 运行测试确认通过**

Run: `./gradlew.bat :app:testAppDebugUnitTest --tests "io.legado.app.ui.theme.IconStyleContractTest"`
Expected: PASS。

- [ ] **Step 5: 提交**

```bash
git add app/src/main/res/drawable/ic_bottom_books*.xml app/src/main/res/drawable/ic_bottom_explore*.xml app/src/main/res/drawable/ic_bottom_rss_feed*.xml app/src/main/res/drawable/ic_bottom_person*.xml app/src/main/res/font app/src/main/res/values/styles.xml app/src/test/java/io/legado/app/ui/theme/IconStyleContractTest.kt
git commit -m "feat(ui-assets): unify iconography and typography system"
```

---

### Task 8: 一次性替换收口验证 + CI + 文档

**Files:**
- Modify: `docs/superpowers/specs/2026-05-05-legado-ui-overhaul-design.md`（如需补充最终偏差）
- Create: `docs/superpowers/reports/2026-05-05-ui-overhaul-regression-checklist.md`

- [ ] **Step 1: 运行全量单测门禁**

Run: `./gradlew.bat :app:testAppDebugUnitTest`
Expected: BUILD SUCCESSFUL。

- [ ] **Step 2: 运行 release 构建门禁**

Run: `./gradlew.bat :app:assembleAppRelease`
Expected: BUILD SUCCESSFUL，R8/资源压缩流程通过。

- [ ] **Step 3: 生成手工回归清单并记录结果**

在 `docs/superpowers/reports/2026-05-05-ui-overhaul-regression-checklist.md` 记录：
- 主入口四页检查
- 阅读链路检查
- 弹窗链路检查
- 主题切换检查
- eInk 模式检查

- [ ] **Step 4: 推送并跟踪 CI 到成功**

Run: `git push origin <working-branch>:main`
Expected: Push 成功；GitHub Actions `Test Build` 最终 `completed success`。

- [ ] **Step 5: 提交收口文档**

```bash
git add docs/superpowers/reports/2026-05-05-ui-overhaul-regression-checklist.md docs/superpowers/specs/2026-05-05-legado-ui-overhaul-design.md
git commit -m "docs(ui): add overhaul regression checklist and final notes"
```

---

## Spec Coverage Check

- Design System Layer -> Task 1, Task 3
- UI Shell Layer -> Task 2
- Feature Surface Layer -> Task 4, Task 5, Task 6
- Legacy Bridge Layer -> Task 1, Task 2, Task 3（通过 `ThemeEngine + shell bridge + styles bridge`）
- Data Flow -> Task 1 + Task 2
- Interaction Model -> Task 2 + Task 5
- Error Handling -> Task 1 + Task 2 + Task 8
- Testing Strategy -> Task 1~8 每任务门禁 + Task 8 收口

无未覆盖项。

## Self-review Results

- Placeholder scan: 已完成，无 TODO/TBD/“后续补充”描述。
- Type consistency: `UiThemeSnapshot`, `UiThemeEngine`, `MainShellNavigator` 命名一致。
- Task granularity: 每任务包含测试失败 -> 最小实现 -> 测试通过 -> 提交，满足可执行粒度。

## Execution Notes

- 所有命令在 `D:\legado` 目录运行。
- 本地命令前注入环境：
  - `JAVA_HOME=D:\tools\jdk17`
  - `ANDROID_HOME=D:\Android`
  - `ANDROID_SDK_ROOT=D:\Android`
- 若 GitHub API 限流，按既有策略使用 Actions 运行页 HTML 标记确认结果。
