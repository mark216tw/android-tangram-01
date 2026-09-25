package com.example.minitangram.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.minitangram.ui.theme.DisplayMode
import com.example.minitangram.game.Difficulty
import com.example.minitangram.game.Level
import com.example.minitangram.game.saveInPlace
import com.example.minitangram.game.PieceKind
import com.example.minitangram.game.Pose
import com.example.minitangram.game.Vec2
import com.example.minitangram.game.PieceColorTheme
import com.example.minitangram.game.levels
import com.example.minitangram.game.nextBuiltInLevel
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("mini_tangram_preferences_v2")

data class UserProgress(
    val displayMode: DisplayMode = DisplayMode.SYSTEM,
    val soundEnabled: Boolean = true,
    val hideBuiltInLevels: Boolean = false,
    val unlockedLevel: Int = 1,
    val bestTimes: Map<Int, Long> = emptyMap(),
    val difficulty: Difficulty = Difficulty.BEGINNER,
    val customLevels: List<Level> = emptyList(),
    val pieceColorTheme: PieceColorTheme = PieceColorTheme.CLASSIC
)

class PreferencesRepository(private val context: Context) {
    private val modeKey = stringPreferencesKey("display_mode")
    private val soundEnabledKey = booleanPreferencesKey("sound_enabled")
    private val hideBuiltInLevelsKey = booleanPreferencesKey("hide_built_in_levels")
    private val unlockedKey = intPreferencesKey("built_in_unlocked_v2")
    private val difficultyKey = stringPreferencesKey("difficulty")
    private val customLevelsKey = stringPreferencesKey("custom_levels")
    private val pieceColorThemeKey = stringPreferencesKey("piece_color_theme")

    val progress: Flow<UserProgress> = context.dataStore.data.map { preferences ->
        val customLevels = decodeLevels(preferences[customLevelsKey])
        UserProgress(
            displayMode = preferences[modeKey]?.let {
                runCatching { DisplayMode.valueOf(it) }.getOrDefault(DisplayMode.SYSTEM)
            } ?: DisplayMode.SYSTEM,
            soundEnabled = preferences[soundEnabledKey] ?: true,
            hideBuiltInLevels = preferences[hideBuiltInLevelsKey] ?: false,
            unlockedLevel = preferences[unlockedKey] ?: levels.first().id,
            difficulty = preferences[difficultyKey]?.let { runCatching { Difficulty.valueOf(it) }.getOrDefault(Difficulty.BEGINNER) } ?: Difficulty.BEGINNER,
            customLevels = customLevels,
            pieceColorTheme = preferences[pieceColorThemeKey]?.let { runCatching { PieceColorTheme.valueOf(it) }.getOrDefault(PieceColorTheme.CLASSIC) } ?: PieceColorTheme.CLASSIC,
            bestTimes = (levels.map { it.id } + customLevels.map { it.id }).mapNotNull { level ->
                preferences[bestTimeKey(level)]?.let { level to it }
            }.toMap()
        )
    }

    suspend fun setDisplayMode(mode: DisplayMode) {
        context.dataStore.edit { it[modeKey] = mode.name }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[soundEnabledKey] = enabled }
    }

    suspend fun setHideBuiltInLevels(hidden: Boolean) {
        context.dataStore.edit { it[hideBuiltInLevelsKey] = hidden }
    }

    suspend fun setDifficulty(difficulty: Difficulty) {
        context.dataStore.edit { it[difficultyKey] = difficulty.name }
    }

    suspend fun setPieceColorTheme(theme: PieceColorTheme) {
        context.dataStore.edit { it[pieceColorThemeKey] = theme.name }
    }

    suspend fun saveCustomLevel(level: Level) {
        context.dataStore.edit { preferences ->
            val existing = decodeLevels(preferences[customLevelsKey])
            val id = if (level.id < 0) level.id else (existing.minOfOrNull { it.id } ?: 0) - 1
            val saved = level.copy(id = id)
            val levels = existing.saveInPlace(saved)
            preferences[customLevelsKey] = encodeLevels(levels)
        }
    }

    suspend fun deleteCustomLevel(id: Int) {
        context.dataStore.edit { preferences ->
            preferences[customLevelsKey] = encodeLevels(decodeLevels(preferences[customLevelsKey]).filterNot { it.id == id })
            preferences.remove(bestTimeKey(id))
        }
    }

    fun exportCustomLevels(levels: List<Level>): String = encodeLevels(levels)

    suspend fun importCustomLevels(json: String, replace: Boolean = false): Result<Unit> = runCatching {
        val imported = decodeLevels(json)
        require(imported.isNotEmpty()) { "檔案沒有可匯入的關卡" }
        context.dataStore.edit { preferences ->
            val current = if (replace) emptyList() else decodeLevels(preferences[customLevelsKey])
            val used = current.map { it.id }.toMutableSet()
            var nextId = (used.minOrNull() ?: 0) - 1
            val normalized = imported.map { level ->
                var id = level.id
                if (id >= 0 || id in used) {
                    while (nextId in used) nextId--
                    id = nextId--
                }
                used += id
                level.copy(id = id)
            }
            preferences[customLevelsKey] = encodeLevels(current + normalized)
        }
    }

    suspend fun completeLevel(level: Int, elapsedSeconds: Long) {
        context.dataStore.edit { preferences ->
            val bestKey = bestTimeKey(level)
            val previous = preferences[bestKey]
            if (previous == null || elapsedSeconds < previous) preferences[bestKey] = elapsedSeconds
            nextBuiltInLevel(level)?.let { next ->
                val unlocked = preferences[unlockedKey] ?: levels.first().id
                val unlockedIndex = levels.indexOfFirst { it.id == unlocked }.coerceAtLeast(0)
                val nextIndex = levels.indexOfFirst { it.id == next.id }
                if (nextIndex > unlockedIndex) preferences[unlockedKey] = next.id
            }
        }
    }
}

private fun bestTimeKey(levelId: Int) = longPreferencesKey(
    if (levelId > 0) "built_in_v2_best_time_$levelId" else "best_time_$levelId"
)

private fun encodeLevels(levels: List<Level>): String {
    val root = JSONObject().put("version", 1).put("levels", JSONArray())
    val array = root.getJSONArray("levels")
    levels.forEach { level ->
        val pieces = JSONArray()
        PieceKind.entries.forEach { kind ->
            val pose = level.targets.getValue(kind)
            pieces.put(JSONObject().put("kind", kind.name).put("x", pose.center.x).put("y", pose.center.y).put("rotation", pose.rotation).put("flipped", pose.flipped))
        }
        array.put(JSONObject().put("id", level.id).put("name", level.name).put("pieces", pieces).apply {
            level.canvasYScale?.let { put("canvasYScale", it) }
        })
    }
    return root.toString()
}

private fun decodeLevels(raw: String?): List<Level> {
    if (raw.isNullOrBlank()) return emptyList()
    return runCatching {
        val array = JSONObject(raw).getJSONArray("levels")
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val pieces = item.getJSONArray("pieces")
            val targets = (0 until pieces.length()).associate {
                val piece = pieces.getJSONObject(it)
                PieceKind.valueOf(piece.getString("kind")) to Pose(
                    Vec2(piece.getDouble("x").toFloat(), piece.getDouble("y").toFloat()),
                    piece.getInt("rotation"), piece.optBoolean("flipped", false)
                )
            }
            require(targets.keys == PieceKind.entries.toSet())
            require(item.getString("name").isNotBlank() && targets.values.all { it.center.x in 0f..1f && it.center.y in 0f..1f })
            val scale = if (item.has("canvasYScale")) item.getDouble("canvasYScale").toFloat() else null
            require(scale == null || (scale.isFinite() && scale > 0f))
            Level(item.getInt("id"), item.getString("name"), targets, scale)
        }
    }.getOrElse { emptyList() }
}
