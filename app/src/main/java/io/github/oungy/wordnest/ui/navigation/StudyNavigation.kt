package io.github.oungy.wordnest.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.oungy.wordnest.R
import io.github.oungy.wordnest.ui.theme.ControlShape
import io.github.oungy.wordnest.ui.theme.PanelShape

@Composable
internal fun StudyNavigation(
    inLibrary: Boolean,
    libraryFocus: FocusRequester,
    onToday: () -> Unit,
    onLibrary: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        shape = PanelShape,
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Row(Modifier.padding(4.dp).selectableGroup(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DestinationTab(stringResource(R.string.today), !inLibrary, onToday, Modifier.weight(1f))
            DestinationTab(
                stringResource(R.string.library_title), inLibrary, onLibrary,
                Modifier.weight(1f).focusRequester(libraryFocus),
            )
        }
    }
}

@Composable
private fun DestinationTab(label: String, selected: Boolean, open: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    var focused by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier.onFocusChanged { focused = it.isFocused }
            .clip(ControlShape)
            .then(if (focused) Modifier.border(2.dp, colors.primary, ControlShape) else Modifier)
            .selectable(selected, role = Role.Tab, onClick = open),
        shape = ControlShape,
        color = if (selected) colors.primaryContainer else colors.surfaceContainer,
        contentColor = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
    ) {
        Box(Modifier.heightIn(min = 48.dp).padding(6.dp), contentAlignment = Alignment.Center) {
            Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}
