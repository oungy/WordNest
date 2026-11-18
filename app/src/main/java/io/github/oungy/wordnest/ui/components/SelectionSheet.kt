package io.github.oungy.wordnest.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.oungy.wordnest.R
import io.github.oungy.wordnest.ui.theme.ControlShape
import io.github.oungy.wordnest.ui.theme.SheetShape
import kotlinx.coroutines.flow.first

internal data class SelectionOption(
    val id: String,
    val title: String,
    val description: String,
    val enabled: Boolean = true,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectionSheet(
    title: String,
    options: List<SelectionOption>,
    selection: String?,
    useGlass: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = MaterialTheme.colorScheme
    val keyboard = LocalSoftwareKeyboardController.current
    val inputMode = LocalInputModeManager.current
    val dismiss = {
        keyboard?.hide()
        onDismiss()
    }
    var draft by rememberSaveable { mutableStateOf(selection) }
    var query by rememberSaveable { mutableStateOf("") }
    val visibleOptions = options.filter { option ->
        option.title.contains(query.trim(), ignoreCase = true) ||
            option.description.contains(query.trim(), ignoreCase = true)
    }
    val enabledOptions = visibleOptions.filter(SelectionOption::enabled)
    val focus = remember(options) { options.associate { it.id to FocusRequester() } }
    val searchFocus = remember { FocusRequester() }
    val selectedInView = remember { BringIntoViewRequester() }

    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.currentValue }.first { it == SheetValue.Expanded }
        if (inputMode.inputMode == InputMode.Keyboard) {
            val target = enabledOptions.firstOrNull { it.id == draft } ?: enabledOptions.firstOrNull()
            (focus[target?.id] ?: searchFocus).requestFocus()
        } else {
            selectedInView.bringIntoView()
        }
    }

    ModalBottomSheet(
        onDismissRequest = dismiss,
        sheetState = sheetState,
        shape = SheetShape,
        containerColor = colors.surfaceContainerHigh.copy(alpha = if (useGlass) 0.92f else 1f),
        contentColor = colors.onSurface,
        scrimColor = Color.Black.copy(alpha = 0.39f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onPreviewKeyEvent {
                    if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) {
                        dismiss()
                        true
                    } else {
                        false
                    }
                }
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                TextButton(onClick = dismiss) { Text(stringResource(R.string.cancel)) }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().focusRequester(searchFocus),
                label = { Text(stringResource(R.string.search_books)) },
                shape = ControlShape,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
            )
            if (visibleOptions.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_books_found),
                    modifier = Modifier.padding(vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                visibleOptions.forEach { option ->
                    ChoiceRow(
                        title = option.title,
                        description = option.description,
                        enabled = option.enabled,
                        selected = option.id == draft,
                        onSelect = { draft = option.id },
                        modifier = (if (option.id == draft) {
                            Modifier.bringIntoViewRequester(selectedInView)
                        } else {
                            Modifier
                        }).focusRequester(focus.getValue(option.id)).onPreviewKeyEvent { event ->
                            val index = enabledOptions.indexOf(option)
                            val target = when (event.key) {
                                Key.DirectionUp -> enabledOptions.getOrNull(index - 1)
                                Key.DirectionDown -> enabledOptions.getOrNull(index + 1)
                                Key.MoveHome -> enabledOptions.firstOrNull()
                                Key.MoveEnd -> enabledOptions.lastOrNull()
                                else -> null
                            }
                            if (event.type == KeyEventType.KeyDown && target != null) {
                                focus.getValue(target.id).requestFocus()
                                true
                            } else {
                                false
                            }
                        },
                    )
                }
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = draft != null,
                shape = ControlShape,
                onClick = {
                    draft?.let {
                        keyboard?.hide()
                        onConfirm(it)
                    }
                },
            ) {
                Text(stringResource(R.string.apply_selection))
            }
        }
    }
}
