package com.example.minitangram.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.minitangram.ui.theme.DisplayMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("mini_tangram_preferences")

data class UserProgress(
    val displayMode: DisplayMode = DisplayMode.SYSTEM,
    val soundEnabled: Boolean = true,
    val unlockedLevel: Int = 1,
    val bestTimes: Map<Int, Long> = emptyMap()
)

class PreferencesRepository(private val context: Context) {
    private val modeKey = stringPreferencesKey("display_mode")
    private val soundEnabledKey = booleanPreferencesKey("sound_enabled")
    private val unlockedKey = intPreferencesKey("unlocked_level")

    val progress: Flow<UserProgress> = context.dataStore.data.map { preferences ->
        UserProgress(
            displayMode = preferences[modeKey]?.let {
                runCatching { DisplayMode.valueOf(it) }.getOrDefault(DisplayMode.SYSTEM)
            } ?: DisplayMode.SYSTEM,
            soundEnabled = preferences[soundEnabledKey] ?: true,
            unlockedLevel = preferences[unlockedKey] ?: 1,
            bestTimes = (1..12).mapNotNull { level ->
                preferences[longPreferencesKey("best_time_$level")]?.let { level to it }
            }.toMap()
        )
    }

    suspend fun setDisplayMode(mode: DisplayMode) {
        context.dataStore.edit { it[modeKey] = mode.name }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[soundEnabledKey] = enabled }
    }

    suspend fun completeLevel(level: Int, elapsedSeconds: Long) {
        context.dataStore.edit { preferences ->
            val bestKey = longPreferencesKey("best_time_$level")
            val previous = preferences[bestKey]
            if (previous == null || elapsedSeconds < previous) preferences[bestKey] = elapsedSeconds
            preferences[unlockedKey] = maxOf(preferences[unlockedKey] ?: 1, (level + 1).coerceAtMost(12))
        }
    }
}
