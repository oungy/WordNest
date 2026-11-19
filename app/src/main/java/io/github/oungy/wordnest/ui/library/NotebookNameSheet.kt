package io.github.oungy.wordnest.ui.library

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import io.github.oungy.wordnest.R
import io.github.oungy.wordnest.data.local.study.LibrarySnapshot
import io.github.oungy.wordnest.data.local.study.Notebook
import io.github.oungy.wordnest.domain.library.NotebookRules
import io.github.oungy.wordnest.ui.components.StudyButton
import io.github.oungy.wordnest.ui.components.WordNestSheet
import io.github.oungy.wordnest.ui.theme.ControlShape

@Composable
internal fun NotebookNameSheet(
    snapshot: LibrarySnapshot,
    editing: Notebook?,
    useGlass: Boolean,
    busy: Boolean,
    saveError: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var draft by rememberSaveable(editing?.id) { mutableStateOf(editing?.name.orEmpty()) }
    val inputFocus = remember { FocusRequester() }
    val inputMode = LocalInputModeManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val normalized = NotebookRules.normalizeName(draft)
    val nameKey = NotebookRules.nameKey(draft)
    val valid = NotebookRules.isValidName(draft)
    val duplicate = valid && snapshot.notebooks.any { it.id != editing?.id && NotebookRules.nameKey(it.name) == nameKey }
    val error = when {
        duplicate -> stringResource(R.string.library_name_duplicate)
        draft.isNotEmpty() && !valid -> stringResource(R.string.library_name_length)
        else -> null
    }
    val canSave = !busy && valid && !duplicate && normalized != editing?.name
    val submit = {
        if (canSave) {
            keyboard?.hide()
            onSave(normalized)
        }
    }
    WordNestSheet(
        title = stringResource(if (editing == null) R.string.library_create else R.string.library_rename),
        useGlass = useGlass, onDismiss = onDismiss,
        onExpanded = { if (inputMode.inputMode == InputMode.Keyboard) inputFocus.requestFocus() },
    ) {
        OutlinedTextField(
            value = draft, onValueChange = { draft = it },
            label = { Text(stringResource(R.string.library_name)) },
            singleLine = true, shape = ControlShape, enabled = !busy,
            isError = error != null, supportingText = { Text(error ?: stringResource(R.string.library_name_length)) },
            modifier = Modifier.fillMaxWidth().focusRequester(inputFocus),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
        )
        if (saveError != null) Text(saveError, color = MaterialTheme.colorScheme.error)
        StudyButton(stringResource(R.string.library_save_name), submit, Modifier.fillMaxWidth(), enabled = canSave)
    }
}
