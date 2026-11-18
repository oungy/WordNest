package io.github.oungy.wordnest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.oungy.wordnest.data.local.settings.AppearancePreferences
import io.github.oungy.wordnest.data.local.settings.PreferencesStore
import io.github.oungy.wordnest.data.local.settings.ThemeMode
import io.github.oungy.wordnest.ui.WordNestApp
import io.github.oungy.wordnest.ui.theme.WordNestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferences = PreferencesStore(applicationContext)
        setContent {
            val appearance by preferences.appearance.collectAsState(initial = AppearancePreferences())
            val darkTheme = when (appearance.theme) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }
            SideEffect {
                val bars = SystemBarStyle.auto(
                    lightScrim = android.graphics.Color.TRANSPARENT,
                    darkScrim = android.graphics.Color.TRANSPARENT,
                    detectDarkMode = { darkTheme },
                )
                enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
            }
            WordNestTheme(darkTheme = darkTheme) {
                WordNestApp(appearance = appearance, preferences = preferences)
            }
        }
    }
}
