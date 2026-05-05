package io.legado.app.lib.theme.system

data class UiThemeSnapshot(
    val styleName: String,
    val primaryColor: Int,
    val accentColor: Int,
    val surfaceColor: Int,
    val surfaceVariantColor: Int,
    val onSurfaceColor: Int,
    val outlineColor: Int,
    val readerTitleSizeSp: Float,
    val readerBodySizeSp: Float,
    val radiusM: Float,
    val isDark: Boolean,
    val isEInk: Boolean,
)

data class UiThemeSnapshotInput(
    val primaryColor: Int? = null,
    val accentColor: Int? = null,
    val backgroundColor: Int? = null,
    val bottomBackgroundColor: Int? = null,
    val isDark: Boolean? = null,
    val isEInk: Boolean? = null,
)
