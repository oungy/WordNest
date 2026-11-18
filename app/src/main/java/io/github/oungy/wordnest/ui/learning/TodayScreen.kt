package io.github.oungy.wordnest.ui.learning

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.oungy.wordnest.R
import io.github.oungy.wordnest.data.local.study.StudyDashboard
import io.github.oungy.wordnest.ui.components.SelectionOption
import io.github.oungy.wordnest.ui.components.StudyButton
import io.github.oungy.wordnest.ui.theme.PanelShape
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal enum class TodayAction { Appearance, Books, Goal, Learn, Review }

private data class PackLabels(@get:StringRes val title: Int, @get:StringRes val description: Int)

private val packLabels = mapOf(
    "foundations" to PackLabels(R.string.book_foundations, R.string.book_foundations_description),
    "everyday" to PackLabels(R.string.book_everyday, R.string.book_everyday_description),
)

@Composable
internal fun TodayScreen(
    modifier: Modifier,
    state: StudyUiState,
    focus: Map<TodayAction, FocusRequester>,
    onAction: (TodayAction) -> Unit,
    onReload: () -> Unit,
) {
    val dashboard = state.dashboard
    Column(modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_wordnest),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(42.dp),
            )
            StudyButton(
                label = stringResource(R.string.appearance),
                onClick = { onAction(TodayAction.Appearance) },
                modifier = Modifier.focusRequester(focus.getValue(TodayAction.Appearance)),
                primary = false,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                stringResource(R.string.today),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { heading() },
            )
            if (dashboard != null) {
                val locale = LocalConfiguration.current.locales[0]
                val format = remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale) }
                Detail(dashboard.day.format(format))
            }
        }
        StudyStatus(state.error)
        if (dashboard == null) {
            if (state.error == null) {
                Detail(stringResource(R.string.study_loading))
            } else {
                StudyButton(stringResource(R.string.try_again), onReload, enabled = !state.busy)
            }
        } else {
            TodayCard(R.string.new_study_title) {
                NewPlanSummary(dashboard)
                if (dashboard.newTotal > dashboard.newCompleted) {
                    val action = if (dashboard.newCompleted == 0) R.string.start_study else R.string.continue_study
                    StudyButton(
                        label = stringResource(action),
                        onClick = { onAction(TodayAction.Learn) },
                        modifier = Modifier.fillMaxWidth().focusRequester(focus.getValue(TodayAction.Learn)),
                        enabled = !state.busy,
                    )
                }
            }
            TodayCard(R.string.review_title) {
                Detail(
                    if (dashboard.dueWords.isEmpty()) stringResource(R.string.no_reviews_due)
                    else pluralStringResource(R.plurals.reviews_due, dashboard.dueWords.size, dashboard.dueWords.size),
                )
                StudyButton(
                    label = stringResource(R.string.start_review),
                    onClick = { onAction(TodayAction.Review) },
                    modifier = Modifier.fillMaxWidth().focusRequester(focus.getValue(TodayAction.Review)),
                    enabled = !state.busy && dashboard.dueWords.isNotEmpty(),
                    primary = false,
                )
            }
            TodayCard(R.string.active_books) {
                if (dashboard.selectedPackIds.isEmpty()) {
                    Detail(stringResource(R.string.no_book_selected))
                } else {
                    dashboard.selectedPackIds.forEach { id ->
                        val progress = dashboard.packs.first { it.id == id }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(packTitle(id), style = MaterialTheme.typography.titleSmall)
                            Detail(stringResource(R.string.words_learned, progress.learned, progress.total))
                        }
                    }
                }
                StudyButton(
                    label = stringResource(R.string.choose_book),
                    onClick = { onAction(TodayAction.Books) },
                    modifier = Modifier.fillMaxWidth().focusRequester(focus.getValue(TodayAction.Books)),
                    enabled = !state.busy,
                    primary = dashboard.selectedPackIds.isEmpty(),
                )
                StudyButton(
                    label = stringResource(R.string.daily_goal_with_value, dashboard.dailyLimit),
                    onClick = { onAction(TodayAction.Goal) },
                    modifier = Modifier.fillMaxWidth().focusRequester(focus.getValue(TodayAction.Goal)),
                    enabled = !state.busy,
                    primary = false,
                )
            }
        }
    }
}

@Composable
private fun NewPlanSummary(dashboard: StudyDashboard) {
    val plan = dashboard.plan
    when {
        plan != null -> {
            Detail(stringResource(R.string.words_learned, dashboard.newCompleted, dashboard.newTotal))
            if (dashboard.newCompleted == dashboard.newTotal) {
                Detail(stringResource(R.string.new_study_complete))
            }
            if (dashboard.planPackIds != dashboard.selectedPackIds) {
                val books = dashboard.planPackIds.map { packTitle(it) }
                Detail(stringResource(R.string.today_plan_books, books.joinToString(stringResource(R.string.book_name_separator))))
            }
            if (plan.goal != dashboard.dailyLimit || dashboard.planPackIds != dashboard.selectedPackIds) {
                Detail(stringResource(R.string.next_plan_settings))
            }
        }
        dashboard.selectedPackIds.isEmpty() -> Detail(stringResource(R.string.choose_books_to_start))
        dashboard.newTotal == 0 -> Detail(stringResource(R.string.selected_books_complete))
        else -> Detail(pluralStringResource(R.plurals.words_available, dashboard.newTotal, dashboard.newTotal))
    }
}

@Composable
private fun TodayCard(@StringRes title: Int, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = PanelShape,
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                stringResource(title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
            content()
        }
    }
}

@Composable
private fun Detail(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
internal fun StudyStatus(error: StudyError?) {
    val resource = when (error) {
        StudyError.Load -> R.string.study_load_error
        StudyError.Save -> R.string.study_save_error
        StudyError.DayChanged -> R.string.study_day_changed
        null -> return
    }
    val colors = MaterialTheme.colorScheme
    Text(
        text = stringResource(resource),
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        color = if (error == StudyError.DayChanged) colors.onSurfaceVariant else colors.error,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
internal fun bookOptions(dashboard: StudyDashboard): List<SelectionOption> {
    val options = dashboard.packs.map { pack ->
        val description = packLabels[pack.id]?.description
        SelectionOption(
            pack.id,
            packTitle(pack.id),
            description?.let { stringResource(it) }.orEmpty(),
            progress = stringResource(R.string.words_learned, pack.learned, pack.total),
        )
    }
    return options + SelectionOption(
        id = "reading",
        title = stringResource(R.string.book_reading),
        description = stringResource(R.string.book_reading_unavailable),
        enabled = false,
    )
}

@Composable
private fun packTitle(id: String): String = packLabels[id]?.let { stringResource(it.title) } ?: id
