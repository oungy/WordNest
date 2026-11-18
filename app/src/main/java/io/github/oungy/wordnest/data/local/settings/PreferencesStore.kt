package io.github.oungy.wordnest.data.local.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.appearanceStore by preferencesDataStore(name = "appearance")

enum class ThemeMode(val storedValue: String) {
    System("system"),
    Light("light"),
    Dark("dark"),
}

data class AppearancePreferences(
    val theme: ThemeMode = ThemeMode.System,
    val reduceTransparency: Boolean = false,
)

class PreferencesStore(context: Context) {
    private val store = context.applicationContext.appearanceStore

    val appearance: Flow<AppearancePreferences> = store.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            AppearancePreferences(
                theme = ThemeMode.entries.firstOrNull { it.storedValue == preferences[ThemeKey] }
                    ?: ThemeMode.System,
                reduceTransparency = preferences[TransparencyKey] ?: false,
            )
        }

    suspend fun setTheme(theme: ThemeMode) {
        store.edit { it[ThemeKey] = theme.storedValue }
    }

    suspend fun setReduceTransparency(reduce: Boolean) {
        store.edit { it[TransparencyKey] = reduce }
    }

    private companion object {
        val ThemeKey = stringPreferencesKey("theme")
        val TransparencyKey = booleanPreferencesKey("reduce_transparency")
    }
}
