package io.github.oungy.wordnest.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.oungy.wordnest.R
import io.github.oungy.wordnest.data.local.settings.AppearancePreferences
import io.github.oungy.wordnest.data.local.settings.ThemeMode
import io.github.oungy.wordnest.ui.components.ChoiceRow
import io.github.oungy.wordnest.ui.theme.ControlShape

@Composable
internal fun AppearanceScreen(
    modifier: Modifier,
    appearance: AppearancePreferences,
    saveFailed: Boolean,
    onBack: () -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onTransparencyChange: (Boolean) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var switchFocused by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        OutlinedButton(onClick = onBack, shape = ControlShape) {
            Text(stringResource(R.string.go_back))
        }
        Text(
            text = stringResource(R.string.appearance),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.theme_label), style = MaterialTheme.typography.titleMedium)
            ThemeMode.entries.forEach { mode ->
                val title = when (mode) {
                    ThemeMode.System -> R.string.theme_system
                    ThemeMode.Light -> R.string.theme_light
                    ThemeMode.Dark -> R.string.theme_dark
                }
                ChoiceRow(
                    title = stringResource(title),
                    selected = appearance.theme == mode,
                    onSelect = { onThemeChange(mode) },
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(ControlShape)
                .onFocusChanged { switchFocused = it.isFocused }
                .border(
                    width = if (switchFocused) 2.dp else 1.dp,
                    color = if (switchFocused) colors.primary else colors.outlineVariant,
                    shape = ControlShape,
                )
                .toggleable(
                    value = appearance.reduceTransparency,
                    role = Role.Switch,
                    onValueChange = onTransparencyChange,
                )
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.reduce_transparency),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(R.string.reduce_transparency_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
            Switch(checked = appearance.reduceTransparency, onCheckedChange = null)
        }
        if (saveFailed) {
            Text(
                text = stringResource(R.string.settings_save_failed),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.error,
            )
        }
    }
}
