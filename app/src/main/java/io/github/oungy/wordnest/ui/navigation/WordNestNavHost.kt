package io.github.oungy.wordnest.ui.navigation

import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.oungy.wordnest.R
import io.github.oungy.wordnest.data.local.settings.AppearancePreferences
import io.github.oungy.wordnest.data.local.settings.ThemeMode
import io.github.oungy.wordnest.data.local.study.StudyMode
import io.github.oungy.wordnest.ui.components.DailyLimitSheet
import io.github.oungy.wordnest.ui.components.SelectionSheet
import io.github.oungy.wordnest.ui.learning.ExerciseScreen
import io.github.oungy.wordnest.ui.learning.StudyViewModel
import io.github.oungy.wordnest.ui.learning.TodayAction
import io.github.oungy.wordnest.ui.learning.TodayScreen
import io.github.oungy.wordnest.ui.learning.bookOptions
import io.github.oungy.wordnest.ui.settings.AppearanceScreen
import java.util.function.Consumer

@Composable
internal fun WordNestNavHost(
    appearance: AppearancePreferences,
    saveFailed: Boolean,
    study: StudyViewModel,
    onThemeChange: (ThemeMode) -> Unit,
    onTransparencyChange: (Boolean) -> Unit,
) {
    val state by study.state.collectAsStateWithLifecycle()
    val dashboard = state.dashboard
    val session = state.session
    var inLibrary by rememberSaveable { mutableStateOf(false) }
    val libraryTab = remember { FocusRequester() }
    var focusLibraryTab by remember { mutableStateOf(false) }
    fun leaveLibrary() {
        inLibrary = false
        focusLibraryTab = true
    }

    var panel by rememberSaveable { mutableStateOf<TodayAction?>(null) }
    var returnFocus by remember { mutableStateOf<TodayAction?>(null) }
    val focus = remember { TodayAction.entries.associateWith { FocusRequester() } }
    val homeScroll = rememberScrollState()
    val settingsScroll = rememberScrollState()
    val useGlass = windowBlurAvailable() && !appearance.reduceTransparency
    var librarySheetOpen by remember { mutableStateOf(false) }
    val sheetOpen = librarySheetOpen || (dashboard != null && (panel == TodayAction.Books || panel == TodayAction.Goal))
    val windowFocused = LocalWindowInfo.current.isWindowFocused

    fun dismissPanel() {
        returnFocus = panel
        panel = null
    }

    fun endSession() {
        inLibrary = false
        returnFocus = if (session?.mode == StudyMode.Review) TodayAction.Review else TodayAction.Learn
        study.leaveSession()
    }

    LaunchedEffect(focusLibraryTab, inLibrary, windowFocused) {
        if (focusLibraryTab && !inLibrary && windowFocused) {
            libraryTab.requestFocus()
            focusLibraryTab = false
        }
    }
    BackHandler(enabled = panel == TodayAction.Appearance, onBack = ::dismissPanel)
    BackHandler(enabled = session != null, onBack = ::endSession)
    LaunchedEffect(returnFocus, state.busy, windowFocused, inLibrary) {
        val target = returnFocus
        if (inLibrary) {
            returnFocus = null
        } else if (target != null && !state.busy && windowFocused && session == null && panel == null) {
            val available = when (target) {
                TodayAction.Learn -> dashboard != null && dashboard.newTotal > dashboard.newCompleted
                TodayAction.Review -> dashboard?.dueWords?.isNotEmpty() == true
                else -> true
            }
            focus.getValue(if (available) target else TodayAction.Books).requestFocus()
            returnFocus = null
        }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().blur(if (sheetOpen && useGlass) 16.dp else 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                val width = Modifier.widthIn(max = 568.dp).fillMaxWidth()
                val padding = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
                when {
                    session != null && dashboard != null -> {
                        val reviewing = session.mode == StudyMode.Review
                        val word = if (reviewing) dashboard.dueWords.firstOrNull() else dashboard.nextNewWord
                        key(session.mode, session.day, word?.id) {
                            ExerciseScreen(
                                modifier = width.verticalScroll(rememberScrollState()).then(padding),
                                session = session,
                                word = word,
                                completed = if (reviewing) session.reviewed else dashboard.newCompleted,
                                total = if (reviewing) session.reviewTotal else dashboard.newTotal,
                                busy = state.busy,
                                error = state.error,
                                onBack = ::endSession,
                                onRate = study::rate,
                            )
                        }
                    }
                    panel == TodayAction.Appearance -> AppearanceScreen(
                        modifier = width.verticalScroll(settingsScroll).then(padding),
                        appearance = appearance,
                        saveFailed = saveFailed,
                        onBack = ::dismissPanel,
                        onThemeChange = onThemeChange,
                        onTransparencyChange = onTransparencyChange,
                    )
                    inLibrary -> LibraryDestination(width, study, useGlass, ::leaveLibrary) { librarySheetOpen = it }
                    else -> TodayScreen(
                        modifier = width.verticalScroll(homeScroll).then(padding),
                        state = state,
                        focus = focus,
                        onReload = study::refresh,
                        onAction = { action ->
                            when (action) {
                                TodayAction.Learn -> study.start(StudyMode.New)
                                TodayAction.Review -> study.start(StudyMode.Review)
                                else -> panel = action
                            }
                        },
                    )
                }
            }
            if (session == null && panel != TodayAction.Appearance) {
                StudyNavigation(
                    inLibrary = inLibrary,
                    libraryFocus = libraryTab,
                    onToday = { inLibrary = false },
                    onLibrary = { inLibrary = true },
                )
            }
        }
        if (dashboard != null) {
            when (panel) {
                TodayAction.Books -> SelectionSheet(
                    title = stringResource(R.string.choose_book),
                    options = bookOptions(dashboard),
                    selection = dashboard.selectedPackIds,
                    canConfirm = !state.busy,
                    useGlass = useGlass,
                    onDismiss = ::dismissPanel,
                    onConfirm = { selection ->
                        if (!study.state.value.busy) {
                            study.selectPacks(selection)
                            dismissPanel()
                        }
                    },
                )
                TodayAction.Goal -> DailyLimitSheet(
                    limit = dashboard.dailyLimit,
                    canConfirm = !state.busy,
                    useGlass = useGlass,
                    onDismiss = ::dismissPanel,
                    onConfirm = { limit ->
                        if (!study.state.value.busy) {
                            study.setDailyLimit(limit)
                            dismissPanel()
                        }
                    },
                )
                else -> Unit
            }
        }
    }
}

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
