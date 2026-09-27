package com.ssajudn.tarsika.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ssajudn.tarsika.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.tarsikaPreferences by preferencesDataStore(name = "hushkeep_preferences")

class ThemePreferenceStore(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme_mode")

    val themeMode: Flow<ThemeMode> =
        context.tarsikaPreferences.data.map { preferences ->
            preferences[themeKey]?.let { value ->
                ThemeMode.entries.firstOrNull { it.name == value }
            } ?: ThemeMode.SYSTEM
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.tarsikaPreferences.edit { preferences ->
            preferences[themeKey] = mode.name
        }
    }
}
