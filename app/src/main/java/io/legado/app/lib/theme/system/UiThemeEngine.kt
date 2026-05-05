package io.legado.app.lib.theme.system

object UiThemeEngine {

    fun buildSnapshot(input: UiThemeSnapshotInput?): UiThemeSnapshot {
        val defaults = UiThemeDefaults.deepImmersiveSnapshot
        input ?: return defaults
        return defaults.copy(
            primaryColor = input.primaryColor ?: defaults.primaryColor,
            accentColor = input.accentColor ?: defaults.accentColor,
            surfaceColor = input.backgroundColor ?: defaults.surfaceColor,
            surfaceVariantColor = input.bottomBackgroundColor ?: defaults.surfaceVariantColor,
            isDark = input.isDark ?: defaults.isDark,
            isEInk = input.isEInk ?: defaults.isEInk,
        )
    }
}
