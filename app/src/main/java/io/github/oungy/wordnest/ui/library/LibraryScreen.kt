package io.github.oungy.wordnest.ui.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.oungy.wordnest.R
import io.github.oungy.wordnest.data.local.study.LibraryRepository
import io.github.oungy.wordnest.data.local.study.LibrarySnapshot
import io.github.oungy.wordnest.ui.components.ChoiceRow
import io.github.oungy.wordnest.ui.components.SelectionOption
import io.github.oungy.wordnest.ui.components.SelectionSheet
import io.github.oungy.wordnest.ui.components.StudyButton
import io.github.oungy.wordnest.ui.components.WordNestSheet
import io.github.oungy.wordnest.ui.theme.ControlShape

private enum class Panel(val trigger: String) {
    Create("create"), Rename("rename"), Delete("delete"), EditWords("words"), Memberships("memberships")
}

@Composable
internal fun LibraryScreen(
    modifier: Modifier,
    snapshot: LibrarySnapshot,
    useGlass: Boolean,
    onBack: () -> Unit,
    onSheetVisible: (Boolean) -> Unit,
    submitEdit: (suspend LibraryRepository.() -> Unit) -> String,
    awaitEdit: suspend (String) -> Boolean,
) {
    var group by rememberSaveable { mutableStateOf<LibraryGroup?>(null) }
    var notebookId by rememberSaveable { mutableStateOf<String?>(null) }
    var wordId by rememberSaveable { mutableStateOf<String?>(null) }
    var panel by rememberSaveable { mutableStateOf<Panel?>(null) }
    DisposableEffect(panel != null) {
        onSheetVisible(panel != null)
        onDispose { onSheetVisible(false) }
    }
    var query by rememberSaveable(group, notebookId) { mutableStateOf("") }
    var needsPracticeOnly by rememberSaveable(group) { mutableStateOf(false) }
    var restoreFocus by remember { mutableStateOf<String?>(null) }
    var pendingEdit by rememberSaveable { mutableStateOf<String?>(null) }
    val writing = pendingEdit != null
    var saveFailed by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val windowFocused = LocalWindowInfo.current.isWindowFocused
    val focusTarget: @Composable (String) -> Modifier = { id ->
        val requester = remember(id) { FocusRequester() }
        LaunchedEffect(restoreFocus, windowFocused, panel, writing, id) {
            if (restoreFocus == id && windowFocused && panel == null && !writing) {
                requester.requestFocus()
                restoreFocus = null
            }
        }
        Modifier.focusRequester(requester)
    }

    val book = snapshot.notebooks.firstOrNull { it.id == notebookId }
    val entry = snapshot.words.firstOrNull { it.word.id == wordId }
    val activeGroup = group
    val bookWordIds = remember(snapshot.memberships, notebookId) {
        snapshot.memberships.filter { it.notebookId == notebookId }.map { it.wordId }.toSet()
    }
    val wordNotebookIds = remember(snapshot.memberships, wordId) {
        snapshot.memberships.filter { it.wordId == wordId }.map { it.notebookId }.toSet()
    }
    val search = query.trim()
    val words = remember(snapshot.words, activeGroup, needsPracticeOnly, bookWordIds, search) {
        snapshot.words.filter { item ->
            val member = activeGroup?.contains(item) ?: (item.word.id in bookWordIds)
            member && (!needsPracticeOnly || activeGroup != LibraryGroup.Mistaken || item.needsPractice) &&
                (item.word.spelling.contains(search, true) || item.word.definition.contains(search, true) ||
                    item.word.chineseDefinition.contains(search, true))
        }
    }
    fun dismiss() {
        restoreFocus = panel?.trigger
        panel = null
        saveFailed = false
    }
    fun openPanel(next: Panel) {
        saveFailed = false
        panel = next
    }
    fun back() {
        if (writing) return
        saveFailed = false
        when {
            panel != null -> dismiss()
            wordId != null -> {
                restoreFocus = if (words.any { it.word.id == wordId }) wordId else "back"
                wordId = null
            }
            group != null || notebookId != null -> {
                restoreFocus = notebookId ?: group?.name
                notebookId = null
                group = null
            }
            else -> onBack()
        }
    }
    fun write(action: suspend LibraryRepository.() -> Unit) {
        if (pendingEdit != null) return
        saveFailed = false
        pendingEdit = submitEdit(action)
    }
    LaunchedEffect(pendingEdit) {
        val id = pendingEdit ?: return@LaunchedEffect
        val saved = awaitEdit(id)
        val removed = panel == Panel.Delete && (saved || book == null)
        if (saved || removed) {
            val createdFromWord = panel == Panel.Create && entry != null
            dismiss()
            if (removed) {
                notebookId = null
                restoreFocus = Panel.Create.trigger
            } else if (createdFromWord) {
                restoreFocus = Panel.Memberships.trigger
            }
        } else {
            saveFailed = true
        }
        pendingEdit = null
    }
    BackHandler(enabled = panel == null, onBack = ::back)
    val homeScroll = rememberScrollState()
    val listScroll = key(group, notebookId) { rememberScrollState() }
    val detailScroll = key(wordId) { rememberScrollState() }
    val scroll = when {
        entry != null -> detailScroll
        group != null || book != null -> listScroll
        else -> homeScroll
    }
    val saveError = if (saveFailed) stringResource(R.string.study_save_error) else null
    Column(
        modifier.verticalScroll(scroll).padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StudyButton(stringResource(R.string.go_back), ::back, focusTarget("back"), enabled = !writing, primary = false)
        val title = entry?.word?.spelling ?: book?.name ?: activeGroup?.let { stringResource(it.title) }
            ?: stringResource(R.string.library_title)
        Text(title, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() })
        if (saveError != null && panel == null) Text(saveError, color = MaterialTheme.colorScheme.error)

        when {
            entry != null -> {
                DictionaryCard(entry.word)
                StudyButton(
                    stringResource(if (entry.saved) R.string.library_unsave else R.string.library_save_word),
                    { write { setSaved(entry.word.id, !entry.saved) } },
                    Modifier.fillMaxWidth(), enabled = !writing, primary = !entry.saved,
                )
                Heading(stringResource(R.string.library_word_notebooks))
                val names = snapshot.notebooks.filter { it.id in wordNotebookIds }.map { it.name }
                if (names.isEmpty()) Muted(stringResource(R.string.library_no_memberships))
                else names.forEach { Muted(it) }
                StudyButton(
                    stringResource(R.string.library_edit_memberships), { openPanel(Panel.Memberships) },
                    focusTarget(Panel.Memberships.trigger), enabled = !writing && snapshot.notebooks.isNotEmpty(),
                    primary = false,
                )
                if (snapshot.notebooks.isEmpty()) {
                    StudyButton(stringResource(R.string.library_create), { openPanel(Panel.Create) },
                        focusTarget(Panel.Create.trigger), enabled = !writing)
                }
            }
            book != null || activeGroup != null -> {
                if (book != null) {
                    StudyButton(stringResource(R.string.library_edit_words), { openPanel(Panel.EditWords) },
                        focusTarget(Panel.EditWords.trigger))
                }
                if (activeGroup == LibraryGroup.Mistaken) {
                    ChoiceRow(stringResource(R.string.library_needs_practice), selected = needsPracticeOnly,
                        onSelect = { needsPracticeOnly = !needsPracticeOnly }, multiple = true)
                }
                if (activeGroup == LibraryGroup.Mastered) Muted(stringResource(R.string.library_mastery_rule))
                if (activeGroup == LibraryGroup.All) {
                    OutlinedTextField(
                        value = query, onValueChange = { query = it }, singleLine = true,
                        label = { Text(stringResource(R.string.library_search_words)) },
                        modifier = Modifier.fillMaxWidth(), shape = ControlShape,
                    )
                }
                if (words.isEmpty()) {
                    val empty = when {
                        query.isNotBlank() -> R.string.library_no_matches
                        activeGroup == LibraryGroup.Mistaken && needsPracticeOnly -> R.string.library_empty_practice
                        else -> activeGroup?.empty ?: R.string.library_empty_book
                    }
                    Muted(stringResource(empty))
                }
                words.forEach { item ->
                    key(item.word.id) {
                        LibraryLink(item.word.spelling, item.word.definitionForLocale(), {
                            keyboard?.hide()
                            wordId = item.word.id
                        }, focusTarget(item.word.id))
                    }
                }
                if (book != null) {
                    Heading(stringResource(R.string.library_manage))
                    StudyButton(stringResource(R.string.library_rename), { openPanel(Panel.Rename) },
                        focusTarget(Panel.Rename.trigger), primary = false)
                    StudyButton(stringResource(R.string.library_delete), { openPanel(Panel.Delete) },
                        focusTarget(Panel.Delete.trigger), primary = false)
                }
            }
            else -> LibraryHome(snapshot, { group = it }, { notebookId = it }, { openPanel(Panel.Create) }, focusTarget)
        }
    }

    val onDismiss = { if (!writing) dismiss() }
    when (panel) {
        Panel.Create, Panel.Rename -> {
            val editing = book.takeIf { panel == Panel.Rename }
            NotebookNameSheet(
                snapshot = snapshot, editing = editing, useGlass = useGlass, busy = writing,
                saveError = saveError, onDismiss = onDismiss,
                onSave = { name ->
                    write { saveNotebook(editing?.id, name) }
                },
            )
        }
        Panel.Delete -> if (book != null) {
            WordNestSheet(stringResource(R.string.library_delete), useGlass, onDismiss) {
                Text(stringResource(R.string.library_delete_message, book.name))
                if (saveError != null) Text(saveError, color = MaterialTheme.colorScheme.error)
                StudyButton(stringResource(R.string.library_delete_confirm), {
                    write { deleteNotebook(book.id) }
                }, Modifier.fillMaxWidth(), enabled = !writing)
            }
        }
        Panel.EditWords -> if (book != null) {
            SelectionSheet(
                title = stringResource(R.string.library_edit_words),
                options = snapshot.words.map {
                    SelectionOption(it.word.id, it.word.spelling, it.word.definitionForLocale())
                },
                selection = bookWordIds.toList(), canConfirm = !writing, useGlass = useGlass,
                onDismiss = onDismiss, onConfirm = { ids -> write { setWords(book.id, ids) } },
                searchLabel = stringResource(R.string.library_search_words),
                emptyLabel = stringResource(R.string.library_no_matches), error = saveError,
            )
        }
        Panel.Memberships -> if (entry != null) {
            SelectionSheet(
                title = stringResource(R.string.library_edit_memberships),
                options = snapshot.notebooks.map { SelectionOption(it.id, it.name, "") },
                selection = wordNotebookIds.toList(), canConfirm = !writing, useGlass = useGlass,
                onDismiss = onDismiss, onConfirm = { ids -> write { setNotebooks(entry.word.id, ids) } },
                searchLabel = stringResource(R.string.library_search_notebooks),
                emptyLabel = stringResource(R.string.library_no_matching_notebooks), error = saveError,
            )
        }
        null -> Unit
    }
}
