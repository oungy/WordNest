package io.github.oungy.wordnest.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import io.github.oungy.wordnest.R
import io.github.oungy.wordnest.data.local.study.DailyLimits
import io.github.oungy.wordnest.ui.theme.ControlShape

@Composable
internal fun DailyLimitSheet(
    limit: Int,
    canConfirm: Boolean,
    useGlass: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var draft by rememberSaveable { mutableStateOf(limit.toString()) }
    val selected = draft.toIntOrNull()?.takeIf { it in DailyLimits }
    val keyboard = LocalSoftwareKeyboardController.current
    val inputMode = LocalInputModeManager.current
    val inputFocus = remember { FocusRequester() }
    val confirm = {
        if (selected != null && canConfirm) {
            keyboard?.hide()
            onConfirm(selected)
        }
    }
    WordNestSheet(
        title = stringResource(R.string.daily_goal),
        useGlass = useGlass,
        onDismiss = onDismiss,
        onExpanded = {
            if (inputMode.inputMode == InputMode.Keyboard) inputFocus.requestFocus()
        },
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.fillMaxWidth().focusRequester(inputFocus),
            label = { Text(stringResource(R.string.daily_goal_input)) },
            supportingText = { Text(stringResource(R.string.daily_goal_range, DailyLimits.first, DailyLimits.last)) },
            shape = ControlShape,
            singleLine = true,
            isError = selected == null && draft.isNotEmpty(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { confirm() }),
        )
        StudyButton(
            label = stringResource(R.string.apply_selection),
            modifier = Modifier.fillMaxWidth(),
            enabled = selected != null && canConfirm,
            onClick = confirm,
        )
    }
}
