package io.github.oungy.wordnest.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.oungy.wordnest.R
import io.github.oungy.wordnest.ui.theme.ControlShape

internal data class SelectionOption(
    val id: String,
    val title: String,
    val description: String,
    val enabled: Boolean = true,
    val progress: String? = null,
)

@Composable
internal fun SelectionSheet(
    title: String,
    options: List<SelectionOption>,
    selection: List<String>,
    canConfirm: Boolean,
    useGlass: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit,
    searchLabel: String = stringResource(R.string.search_books),
    emptyLabel: String = stringResource(R.string.no_books_found),
    error: String? = null,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val inputMode = LocalInputModeManager.current
    var draft by rememberSaveable(
        stateSaver = listSaver<Set<String>, String>(save = { it.toList() }, restore = { it.toSet() }),
    ) { mutableStateOf(selection.toSet()) }
    var query by rememberSaveable { mutableStateOf("") }
    val search = query.trim()
    val visibleOptions = options.filter { option ->
        option.title.contains(search, ignoreCase = true) ||
            option.description.contains(search, ignoreCase = true)
    }
    val enabledOptions = visibleOptions.filter(SelectionOption::enabled)
    val ids = options.map(SelectionOption::id)
    val focus = remember(ids) { ids.associateWith { FocusRequester() } }
    val searchFocus = remember { FocusRequester() }
    val selectedInView = remember { BringIntoViewRequester() }

    WordNestSheet(
        title = title,
        useGlass = useGlass,
        onDismiss = onDismiss,
        onExpanded = {
            if (inputMode.inputMode == InputMode.Keyboard) {
                val target = enabledOptions.firstOrNull { it.id in draft } ?: enabledOptions.firstOrNull()
                (focus[target?.id] ?: searchFocus).requestFocus()
            } else {
                selectedInView.bringIntoView()
            }
        },
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().focusRequester(searchFocus),
            label = { Text(searchLabel) },
            shape = ControlShape,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
        )
        if (visibleOptions.isEmpty()) {
            Text(
                text = emptyLabel,
                modifier = Modifier.padding(vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val firstSelected = visibleOptions.firstOrNull { it.id in draft }?.id
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            visibleOptions.forEach { option ->
                key(option.id) {
                    ChoiceRow(
                        title = option.title,
                        description = listOfNotNull(option.description.takeIf { it.isNotBlank() }, option.progress)
                            .joinToString("\n").ifBlank { null },
                        enabled = option.enabled,
                        multiple = true,
                        selected = option.id in draft,
                        onSelect = {
                            draft = if (option.id in draft) draft - option.id else draft + option.id
                        },
                        modifier = (if (option.id == firstSelected) {
                            Modifier.bringIntoViewRequester(selectedInView)
                        } else {
                            Modifier
                        }).focusRequester(focus.getValue(option.id)).onPreviewKeyEvent { event ->
                            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                            val index = enabledOptions.indexOf(option)
                            val target = when (event.key) {
                                Key.DirectionUp -> enabledOptions.getOrNull(index - 1)
                                Key.DirectionDown -> enabledOptions.getOrNull(index + 1)
                                Key.MoveHome -> enabledOptions.firstOrNull()
                                Key.MoveEnd -> enabledOptions.lastOrNull()
                                else -> null
                            }
                            target != null && focus.getValue(target.id).requestFocus()
                        },
                    )
                }
            }
        }
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        StudyButton(
            label = stringResource(R.string.apply_selection),
            modifier = Modifier.fillMaxWidth(),
            enabled = canConfirm && (draft.isNotEmpty() || selection.isNotEmpty()),
            onClick = {
                keyboard?.hide()
                onConfirm(draft.toSet())
            },
        )
    }
}
