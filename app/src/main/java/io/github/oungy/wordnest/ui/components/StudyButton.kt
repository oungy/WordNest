package io.github.oungy.wordnest.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.oungy.wordnest.ui.theme.ControlShape

@Composable
internal fun StudyButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val colors = MaterialTheme.colorScheme
    val size = modifier.heightIn(min = 48.dp).focusProperties { canFocus = enabled }
    if (primary) {
        Button(
            onClick = onClick,
            modifier = size,
            enabled = enabled,
            shape = ControlShape,
            border = if (focused) BorderStroke(2.dp, colors.onPrimary) else null,
            interactionSource = interaction,
        ) {
            Text(label, textAlign = TextAlign.Center)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = size,
            enabled = enabled,
            shape = ControlShape,
            border = when {
                !enabled -> BorderStroke(1.dp, colors.outlineVariant)
                focused -> BorderStroke(2.dp, colors.primary)
                else -> BorderStroke(1.dp, colors.outline)
            },
            interactionSource = interaction,
        ) {
            Text(label, textAlign = TextAlign.Center)
        }
    }
}
