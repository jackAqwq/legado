package io.legado.app.ui.compose

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.legado.app.R
import io.legado.app.lib.theme.accentColor
import io.legado.app.lib.theme.backgroundColor
import io.legado.app.lib.theme.isDarkTheme
import io.legado.app.lib.theme.primaryColor
import io.legado.app.lib.theme.primaryTextColor
import io.legado.app.lib.theme.secondaryTextColor
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.getCompatColor

@Immutable
data class LegadoComposePalette(
    val darkTheme: Boolean,
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val surface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val outline: Color,
    val error: Color,
)

@Composable
fun rememberLegadoComposePalette(): LegadoComposePalette {
    val context = LocalContext.current
    val primaryColor = context.primaryColor
    val primaryTextColor = Color(context.primaryTextColor)
    val secondaryTextColor = Color(context.secondaryTextColor)
    return remember(
        context.isDarkTheme,
        primaryColor,
        context.backgroundColor,
        context.accentColor,
        primaryTextColor,
        secondaryTextColor,
        context.getCompatColor(R.color.background_card),
        context.getCompatColor(R.color.error),
    ) {
        LegadoComposePalette(
            darkTheme = context.isDarkTheme,
            primary = Color(primaryColor),
            onPrimary = if (ColorUtils.isColorLight(primaryColor)) {
                Color(0xDE000000)
            } else {
                Color.White
            },
            background = Color(context.backgroundColor),
            surface = Color(context.getCompatColor(R.color.background_card)),
            primaryText = primaryTextColor,
            secondaryText = secondaryTextColor,
            outline = secondaryTextColor.copy(alpha = 0.28f),
            error = Color(context.getCompatColor(R.color.error)),
        )
    }
}

@Composable
fun LegadoComposeTheme(content: @Composable () -> Unit) {
    val palette = rememberLegadoComposePalette()
    val colorScheme = remember(palette) {
        if (palette.darkTheme) {
            darkColorScheme(
                primary = palette.primary,
                onPrimary = palette.onPrimary,
                background = palette.background,
                onBackground = palette.primaryText,
                surface = palette.surface,
                onSurface = palette.primaryText,
                onSurfaceVariant = palette.secondaryText,
                surfaceVariant = palette.surface,
                outline = palette.outline,
                error = palette.error,
                onError = Color.White,
            )
        } else {
            lightColorScheme(
                primary = palette.primary,
                onPrimary = palette.onPrimary,
                background = palette.background,
                onBackground = palette.primaryText,
                surface = palette.surface,
                onSurface = palette.primaryText,
                onSurfaceVariant = palette.secondaryText,
                surfaceVariant = palette.surface,
                outline = palette.outline,
                error = palette.error,
                onError = Color.White,
            )
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Shapes(
            small = RoundedCornerShape(3.dp),
            medium = RoundedCornerShape(3.dp),
            large = RoundedCornerShape(3.dp),
        ),
        content = content,
    )
}
