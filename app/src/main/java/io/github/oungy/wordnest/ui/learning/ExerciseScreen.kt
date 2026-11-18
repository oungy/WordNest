package io.github.oungy.wordnest.ui.learning

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.oungy.wordnest.R
import io.github.oungy.wordnest.data.local.study.StudyMode
import io.github.oungy.wordnest.data.local.study.StudyWord
import io.github.oungy.wordnest.domain.Rating
import io.github.oungy.wordnest.ui.components.StudyButton
import io.github.oungy.wordnest.ui.theme.PanelShape

@Composable
internal fun ExerciseScreen(
    modifier: Modifier,
    session: ExerciseSession,
    word: StudyWord?,
    completed: Int,
    total: Int,
    busy: Boolean,
    error: StudyError?,
    onBack: () -> Unit,
    onRate: (String, Rating) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val inputMode = LocalInputModeManager.current
    val actionFocus = remember { FocusRequester() }
    val ratingFocus = remember { FocusRequester() }
    var revealed by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(revealed, busy) {
        if (!busy && inputMode.inputMode == InputMode.Keyboard) {
            val target = if (revealed && word != null) ratingFocus else actionFocus
            target.requestFocus()
        }
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        if (word != null) {
            StudyButton(stringResource(R.string.go_back), onBack, primary = false)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val title = when {
                word == null && session.mode == StudyMode.New -> R.string.new_study_complete
                word == null -> R.string.review_complete
                session.mode == StudyMode.New -> R.string.new_study_title
                else -> R.string.review_title
            }
            Text(
                stringResource(title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                stringResource(R.string.words_recorded, completed, total),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else completed.toFloat() / total },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        StudyStatus(error)
        if (word == null) {
            StudyButton(
                label = stringResource(R.string.back_to_today),
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().focusRequester(actionFocus),
            )
        } else {
            WordCard(word, revealed)
            if (revealed) {
                RatingActions(busy, ratingFocus) { onRate(word.id, it) }
            } else {
                StudyButton(
                    label = stringResource(R.string.reveal_definition),
                    onClick = { revealed = true },
                    modifier = Modifier.fillMaxWidth().focusRequester(actionFocus),
                    enabled = !busy,
                )
            }
        }
    }
}

@Composable
private fun WordCard(word: StudyWord, revealed: Boolean) {
    val colors = MaterialTheme.colorScheme
    val chinese = LocalConfiguration.current.locales[0]?.language == "zh"
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PanelShape,
        color = colors.surfaceContainerHigh,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    word.spelling,
                    style = MaterialTheme.typography.displaySmall,
                    color = colors.primary,
                    modifier = Modifier.semantics { heading() },
                )
                Text(word.partOfSpeech, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
            }
            if (revealed) {
                Text(
                    if (chinese) word.chineseDefinition else word.definition,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(word.example, style = MaterialTheme.typography.bodyLarge)
                if (chinese) {
                    Text(word.chineseExample, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
            } else {
                Text(
                    stringResource(R.string.recall_prompt),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun RatingActions(busy: Boolean, firstRating: FocusRequester, onRate: (Rating) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            stringResource(R.string.rate_prompt),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Rating.entries.forEach { rating ->
            val label = when (rating) {
                Rating.Forgot -> R.string.rating_forgot
                Rating.Unsure -> R.string.rating_unsure
                Rating.Remembered -> R.string.rating_remembered
            }
            StudyButton(
                label = stringResource(label),
                onClick = { onRate(rating) },
                modifier = Modifier.fillMaxWidth().then(
                    if (rating == Rating.Forgot) Modifier.focusRequester(firstRating) else Modifier,
                ),
                primary = rating == Rating.Remembered,
                enabled = !busy,
            )
        }
    }
}
