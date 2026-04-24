package io.legado.app.ui.book.read.config

import android.view.KeyEvent as AndroidKeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.legado.app.R

private const val PrevFieldTag = "page_key_prev_field"
private const val NextFieldTag = "page_key_next_field"
private const val ResetButtonTag = "page_key_reset"
private const val ConfirmButtonTag = "page_key_confirm"

@Immutable
data class PageKeyDialogColors(
    val darkTheme: Boolean,
    val backgroundColor: Color,
    val primaryTextColor: Color,
    val secondaryTextColor: Color,
    val accentColor: Color,
    val buttonColor: Color,
    val buttonPressedColor: Color,
) {
    companion object {
        fun preview() = PageKeyDialogColors(
            darkTheme = false,
            backgroundColor = Color(0xFFFAFAFA),
            primaryTextColor = Color(0xDE000000),
            secondaryTextColor = Color(0x8A000000),
            accentColor = Color(0xFF039BE5),
            buttonColor = Color(0x63ACACAC),
            buttonPressedColor = Color(0x63858585),
        )
    }
}

fun appendPageKeyCode(value: String, keyCode: Int): String {
    return if (value.isEmpty() || value.endsWith(",")) {
        value + keyCode
    } else {
        "$value,$keyCode"
    }
}

@Composable
fun PageKeyDialogContent(
    prevKeys: String,
    nextKeys: String,
    colors: PageKeyDialogColors,
    onPrevKeysChange: (String) -> Unit,
    onNextKeysChange: (String) -> Unit,
    onPrevHardwareKey: (Int) -> Unit,
    onNextHardwareKey: (Int) -> Unit,
    onResetClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MaterialTheme(colorScheme = pageKeyDialogColorScheme(colors)) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = colors.backgroundColor,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                androidx.compose.material3.Text(
                    text = stringResource(R.string.custom_page_key),
                    color = colors.primaryTextColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                KeyCaptureTextField(
                    modifier = Modifier.testTag(PrevFieldTag),
                    value = prevKeys,
                    label = stringResource(R.string.prev_page_key),
                    colors = colors,
                    onValueChange = onPrevKeysChange,
                    onCaptureKeyCode = onPrevHardwareKey,
                )
                KeyCaptureTextField(
                    modifier = Modifier.testTag(NextFieldTag),
                    value = nextKeys,
                    label = stringResource(R.string.next_page_key),
                    colors = colors,
                    onValueChange = onNextKeysChange,
                    onCaptureKeyCode = onNextHardwareKey,
                )
                androidx.compose.material3.Text(
                    text = stringResource(R.string.page_key_set_help),
                    color = colors.secondaryTextColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    FilletActionButton(
                        modifier = Modifier
                            .weight(1f)
                            .testTag(ResetButtonTag),
                        text = stringResource(R.string.reset),
                        colors = colors,
                        onClick = onResetClick,
                    )
                    FilletActionButton(
                        modifier = Modifier
                            .weight(1f)
                            .testTag(ConfirmButtonTag),
                        text = stringResource(R.string.ok),
                        colors = colors,
                        onClick = onConfirmClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyCaptureTextField(
    value: String,
    label: String,
    colors: PageKeyDialogColors,
    onValueChange: (String) -> Unit,
    onCaptureKeyCode: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { event ->
                val keyCode = capturableKeyCode(event) ?: return@onPreviewKeyEvent false
                onCaptureKeyCode(keyCode)
                true
            },
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { androidx.compose.material3.Text(label) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.primaryTextColor,
            unfocusedTextColor = colors.primaryTextColor,
            focusedContainerColor = colors.backgroundColor,
            unfocusedContainerColor = colors.backgroundColor,
            focusedBorderColor = colors.accentColor,
            unfocusedBorderColor = colors.secondaryTextColor.copy(alpha = 0.6f),
            focusedLabelColor = colors.accentColor,
            unfocusedLabelColor = colors.secondaryTextColor,
            cursorColor = colors.accentColor,
        ),
    )
}

@Composable
private fun FilletActionButton(
    text: String,
    colors: PageKeyDialogColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .background(
                color = if (pressed) colors.buttonPressedColor else colors.buttonColor,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Text(
            text = text,
            color = colors.primaryTextColor,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

private fun pageKeyDialogColorScheme(colors: PageKeyDialogColors) = if (colors.darkTheme) {
    darkColorScheme(
        primary = colors.accentColor,
        background = colors.backgroundColor,
        surface = colors.backgroundColor,
        onSurface = colors.primaryTextColor,
        onSurfaceVariant = colors.secondaryTextColor,
        outline = colors.secondaryTextColor.copy(alpha = 0.6f),
    )
} else {
    lightColorScheme(
        primary = colors.accentColor,
        background = colors.backgroundColor,
        surface = colors.backgroundColor,
        onSurface = colors.primaryTextColor,
        onSurfaceVariant = colors.secondaryTextColor,
        outline = colors.secondaryTextColor.copy(alpha = 0.6f),
    )
}

private fun capturableKeyCode(event: KeyEvent): Int? {
    if (event.type != KeyEventType.KeyDown) {
        return null
    }
    val keyCode = event.key.keyCode.toInt()
    if (keyCode == AndroidKeyEvent.KEYCODE_BACK || keyCode == AndroidKeyEvent.KEYCODE_DEL) {
        return null
    }
    return keyCode
}

@Preview(showBackground = true)
@Composable
private fun PageKeyDialogContentPreview() {
    PageKeyDialogContent(
        prevKeys = "24,25",
        nextKeys = "92",
        colors = PageKeyDialogColors.preview(),
        onPrevKeysChange = {},
        onNextKeysChange = {},
        onPrevHardwareKey = {},
        onNextHardwareKey = {},
        onResetClick = {},
        onConfirmClick = {},
    )
}
