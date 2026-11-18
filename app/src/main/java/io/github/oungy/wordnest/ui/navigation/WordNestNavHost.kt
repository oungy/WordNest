package io.github.oungy.wordnest.ui.navigation

import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.oungy.wordnest.R
import io.github.oungy.wordnest.data.local.settings.AppearancePreferences
import io.github.oungy.wordnest.data.local.settings.ThemeMode
import io.github.oungy.wordnest.ui.components.SelectionOption
import io.github.oungy.wordnest.ui.components.SelectionSheet
import io.github.oungy.wordnest.ui.settings.AppearanceScreen
import io.github.oungy.wordnest.ui.theme.ControlShape
import io.github.oungy.wordnest.ui.theme.PanelShape
import java.util.function.Consumer

@Composable
internal fun WordNestNavHost(
    appearance: AppearancePreferences,
    saveFailed: Boolean,
    onThemeChange: (ThemeMode) -> Unit,
    onTransparencyChange: (Boolean) -> Unit,
) {
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var bookPickerOpen by rememberSaveable { mutableStateOf(false) }
    var selectedBook by rememberSaveable { mutableStateOf<String?>(null) }
    var returnToBookButton by remember { mutableStateOf(false) }
    var returnToSettingsButton by remember { mutableStateOf(false) }
    val bookButton = remember { FocusRequester() }
    val settingsButton = remember { FocusRequester() }
    val homeScroll = rememberScrollState()
    val settingsScroll = rememberScrollState()
    val options = sampleBooks()
    val blurAvailable = windowBlurAvailable()
    val useGlass = blurAvailable && !appearance.reduceTransparency

    fun dismissSettings() {
        settingsOpen = false
        returnToSettingsButton = true
    }

    fun dismissPicker() {
        bookPickerOpen = false
        returnToBookButton = true
    }

    BackHandler(enabled = settingsOpen, onBack = ::dismissSettings)
    LaunchedEffect(returnToBookButton, returnToSettingsButton) {
        if (returnToBookButton) {
            bookButton.requestFocus()
            returnToBookButton = false
        }
        if (returnToSettingsButton) {
            settingsButton.requestFocus()
            returnToSettingsButton = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier.safeDrawingPadding()
                .blur(if (bookPickerOpen && useGlass) 16.dp else 0.dp)
                .verticalScroll(if (settingsOpen) settingsScroll else homeScroll)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            val contentWidth = Modifier.widthIn(max = 520.dp).fillMaxWidth()
            if (settingsOpen) {
                AppearanceScreen(
                    modifier = contentWidth,
                    appearance = appearance,
                    saveFailed = saveFailed,
                    onBack = ::dismissSettings,
                    onThemeChange = onThemeChange,
                    onTransparencyChange = onTransparencyChange,
                )
            } else {
                HomeScreen(
                    modifier = contentWidth,
                    bookButton = bookButton,
                    settingsButton = settingsButton,
                    selectedBookTitle = options.firstOrNull { it.id == selectedBook }?.title,
                    onPickBook = { bookPickerOpen = true },
                    onOpenSettings = { settingsOpen = true },
                )
            }
        }
        if (bookPickerOpen) {
            SelectionSheet(
                title = stringResource(R.string.choose_book),
                options = options,
                selection = selectedBook,
                useGlass = useGlass,
                onDismiss = ::dismissPicker,
                onConfirm = {
                    selectedBook = it
                    dismissPicker()
                },
            )
        }
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier,
    bookButton: FocusRequester,
    settingsButton: FocusRequester,
    selectedBookTitle: String?,
    onPickBook: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val bookButtonInteractions = remember { MutableInteractionSource() }
    val bookButtonFocused by bookButtonInteractions.collectIsFocusedAsState()
    Column(modifier, verticalArrangement = Arrangement.spacedBy(28.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_wordnest),
                contentDescription = null,
                modifier = Modifier.size(42.dp),
            )
            OutlinedButton(
                onClick = onOpenSettings,
                modifier = Modifier.focusRequester(settingsButton),
                shape = ControlShape,
            ) {
                Text(stringResource(R.string.appearance))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(
                text = stringResource(R.string.brand_title),
                style = MaterialTheme.typography.displaySmall,
                color = colors.primary,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.welcome_message),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
            )
        }
        Surface(
            shape = PanelShape,
            color = colors.surfaceContainer,
            border = BorderStroke(1.dp, colors.outlineVariant),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.sample_books),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = selectedBookTitle ?: stringResource(R.string.no_book_selected),
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Button(
                    onClick = onPickBook,
                    modifier = Modifier.fillMaxWidth().focusRequester(bookButton),
                    shape = ControlShape,
                    border = if (bookButtonFocused) BorderStroke(2.dp, colors.onPrimary) else null,
                    interactionSource = bookButtonInteractions,
                ) {
                    Text(stringResource(R.string.choose_book))
                }
            }
        }
    }
}

@Composable
private fun sampleBooks() = listOf(
    SelectionOption(
        id = "foundations",
        title = stringResource(R.string.book_foundations),
        description = stringResource(R.string.book_foundations_description),
    ),
    SelectionOption(
        id = "everyday",
        title = stringResource(R.string.book_everyday),
        description = stringResource(R.string.book_everyday_description),
    ),
    SelectionOption(
        id = "reading",
        title = stringResource(R.string.book_reading),
        description = stringResource(R.string.book_reading_unavailable),
        enabled = false,
    ),
)

@Composable
private fun windowBlurAvailable(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
    val context = LocalContext.current
    val windowManager = remember(context) { context.getSystemService(WindowManager::class.java) }
    var enabled by remember(windowManager) { mutableStateOf(false) }
    DisposableEffect(windowManager) {
        val listener = Consumer<Boolean> { enabled = it }
        windowManager?.addCrossWindowBlurEnabledListener(listener)
        onDispose { windowManager?.removeCrossWindowBlurEnabledListener(listener) }
    }
    return enabled
}
