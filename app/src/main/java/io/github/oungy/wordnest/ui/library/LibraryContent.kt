package io.github.oungy.wordnest.ui.library

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.oungy.wordnest.R
import io.github.oungy.wordnest.data.local.study.LibrarySnapshot
import io.github.oungy.wordnest.data.local.study.LibraryWord
import io.github.oungy.wordnest.data.local.study.StudyWord
import io.github.oungy.wordnest.ui.components.StudyButton
import io.github.oungy.wordnest.ui.theme.ControlShape
import io.github.oungy.wordnest.ui.theme.PanelShape

internal enum class LibraryGroup(@get:StringRes val title: Int, @get:StringRes val empty: Int) {
    Saved(R.string.library_saved, R.string.library_empty_saved),
    Mistaken(R.string.library_mistaken, R.string.library_empty_mistaken),
    Learned(R.string.library_learned, R.string.library_empty_learned),
    Mastered(R.string.library_mastered, R.string.library_empty_mastered),
    All(R.string.library_all, R.string.library_empty_all);

    fun contains(entry: LibraryWord): Boolean = when (this) {
        Saved -> entry.saved
        Mistaken -> entry.mistaken
        Learned -> entry.learned
        Mastered -> entry.mastered
        All -> true
    }
}

@Composable
internal fun LibraryHome(
    snapshot: LibrarySnapshot,
    onGroup: (LibraryGroup) -> Unit,
    onNotebook: (String) -> Unit,
    onCreate: () -> Unit,
    focusTarget: @Composable (String) -> Modifier,
) {
    Heading(stringResource(R.string.library_records))
    LibraryGroup.entries.filter { it != LibraryGroup.All }.forEach { group ->
        LibraryLink(
            stringResource(group.title), wordCount(snapshot.words.count(group::contains)),
            { onGroup(group) }, focusTarget(group.name),
        )
    }
    Heading(stringResource(R.string.library_notebooks))
    if (snapshot.notebooks.isEmpty()) Muted(stringResource(R.string.library_empty_notebooks))
    StudyButton(stringResource(R.string.library_create), onCreate, focusTarget("create"))
    val counts = remember(snapshot.memberships) { snapshot.memberships.groupingBy { it.notebookId }.eachCount() }
    snapshot.notebooks.forEach { book ->
        LibraryLink(book.name, wordCount(counts[book.id] ?: 0), { onNotebook(book.id) }, focusTarget(book.id))
    }
    Heading(stringResource(R.string.library_local_dictionary))
    LibraryLink(
        stringResource(R.string.library_all), wordCount(snapshot.words.size),
        { onGroup(LibraryGroup.All) }, focusTarget(LibraryGroup.All.name),
    )
}

@Composable
internal fun LibraryLink(title: String, detail: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        shape = ControlShape,
        color = colors.surfaceContainer,
        border = if (focused) BorderStroke(2.dp, colors.primary) else BorderStroke(1.dp, colors.outlineVariant),
        interactionSource = interaction,
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Muted(detail)
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = colors.primary,
                modifier = Modifier.clearAndSetSemantics { })
        }
    }
}

@Composable
internal fun DictionaryCard(word: StudyWord) {
    val chinese = LocalConfiguration.current.locales[0]?.language == "zh"
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(), shape = PanelShape, color = colors.surfaceContainerHigh,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Muted(word.partOfSpeech)
            Text(word.definitionForLocale(), style = MaterialTheme.typography.titleMedium)
            Text(word.example, style = MaterialTheme.typography.bodyLarge)
            if (chinese) Muted(word.chineseExample)
        }
    }
}

@Composable
internal fun StudyWord.definitionForLocale(): String =
    if (LocalConfiguration.current.locales[0]?.language == "zh") chineseDefinition else definition

@Composable
internal fun Heading(label: String) {
    Text(label, modifier = Modifier.padding(top = 8.dp).semantics { heading() },
        style = MaterialTheme.typography.titleLarge)
}

@Composable
internal fun Muted(label: String) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
internal fun wordCount(count: Int): String = pluralStringResource(R.plurals.library_word_count, count, count)
