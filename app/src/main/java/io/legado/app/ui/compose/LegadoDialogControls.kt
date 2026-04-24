package io.legado.app.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LegadoDialogSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(bottom = 8.dp),
    )
}

@Composable
fun LegadoDialogActionRow(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    cancelText: String,
    confirmText: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onCancel) {
            Text(text = cancelText)
        }
        TextButton(onClick = onConfirm) {
            Text(text = confirmText)
        }
    }
}

@Composable
fun LegadoSliderRow(
    title: String,
    value: Int,
    valueRange: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueText: (Int) -> String = { it.toString() },
) {
    val coercedValue = value.coerceIn(valueRange.first, valueRange.last)
    var sliderValue by remember(valueRange.first, valueRange.last) {
        mutableIntStateOf(coercedValue)
    }

    LaunchedEffect(coercedValue) {
        sliderValue = coercedValue
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = valueText(sliderValue),
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LegadoStepButton(
                text = "-",
                enabled = enabled && sliderValue > valueRange.first,
                onClick = {
                    sliderValue = (sliderValue - 1).coerceAtLeast(valueRange.first)
                    onValueChange(sliderValue)
                },
            )
            Slider(
                value = sliderValue.toFloat(),
                onValueChange = { sliderValue = it.toInt() },
                onValueChangeFinished = { onValueChange(sliderValue) },
                valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
                steps = (valueRange.last - valueRange.first - 1).coerceAtLeast(0),
                modifier = Modifier.weight(1f),
                enabled = enabled,
            )
            LegadoStepButton(
                text = "+",
                enabled = enabled && sliderValue < valueRange.last,
                onClick = {
                    sliderValue = (sliderValue + 1).coerceAtMost(valueRange.last)
                    onValueChange(sliderValue)
                },
            )
        }
    }
}

@Composable
private fun LegadoStepButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (enabled) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
    ) {
        TextButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(36.dp),
        ) {
            Text(text = text)
        }
    }
}
