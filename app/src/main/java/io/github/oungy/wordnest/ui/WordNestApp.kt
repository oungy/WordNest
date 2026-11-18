package io.github.oungy.wordnest.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.oungy.wordnest.data.local.settings.AppearancePreferences
import io.github.oungy.wordnest.data.local.settings.PreferencesStore
import io.github.oungy.wordnest.ui.learning.StudyViewModel
import io.github.oungy.wordnest.ui.navigation.WordNestNavHost
import java.io.IOException
import kotlinx.coroutines.launch

@Composable
internal fun WordNestApp(
    appearance: AppearancePreferences,
    preferences: PreferencesStore,
    study: StudyViewModel,
) {
    val scope = rememberCoroutineScope()
    val saveFailed = remember { mutableStateOf(false) }
    fun save(update: suspend () -> Unit) {
        scope.launch {
            saveFailed.value = try {
                update()
                false
            } catch (_: IOException) {
                true
            }
        }
    }

    WordNestNavHost(
        appearance = appearance,
        saveFailed = saveFailed.value,
        study = study,
        onThemeChange = { theme -> save { preferences.setTheme(theme) } },
        onTransparencyChange = { reduce -> save { preferences.setReduceTransparency(reduce) } },
    )
}
