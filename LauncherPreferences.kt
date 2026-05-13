package com.inclusion.seniorlauncher.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Top-level DataStore extension. Single instance per process.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "launcher_preferences"
)

/**
 * User-facing customisation knobs from the Home Customisation screen.
 *
 * All defaults are senior-friendly:
 *  • Section titles shown for cognitive scaffolding
 *  • 3 columns matches reference layout (Image 1)
 *  • 96dp icons and 22sp tile text are comfortable on mid-range 6" phones
 */
class LauncherPreferences(private val context: Context) {

    companion object {
        private val KEY_SHOW_SECTION_TITLES = booleanPreferencesKey("show_section_titles")
        private val KEY_GRID_COLUMNS        = intPreferencesKey("grid_columns")
        private val KEY_ICON_SIZE           = intPreferencesKey("icon_size_dp")
        private val KEY_TILE_TEXT_SIZE      = intPreferencesKey("tile_text_size_sp")
        private val KEY_HIGH_CONTRAST       = booleanPreferencesKey("high_contrast_mode")

        // Defaults
        const val DEFAULT_SHOW_TITLES   = true
        const val DEFAULT_GRID_COLUMNS  = 3
        const val DEFAULT_ICON_SIZE     = 96
        const val DEFAULT_TILE_TEXT     = 22

        // Slider bounds — enforced both in UI and here
        const val MIN_GRID_COLUMNS = 2
        const val MAX_GRID_COLUMNS = 4
        const val MIN_ICON_SIZE    = 64
        const val MAX_ICON_SIZE    = 128
        const val MIN_TILE_TEXT    = 18  // hard floor per spec
        const val MAX_TILE_TEXT    = 32
    }

    // ---- Reads ----

    val showSectionTitles: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_SHOW_SECTION_TITLES] ?: DEFAULT_SHOW_TITLES }

    val gridColumns: Flow<Int> = context.dataStore.data
        .map { (it[KEY_GRID_COLUMNS] ?: DEFAULT_GRID_COLUMNS)
            .coerceIn(MIN_GRID_COLUMNS, MAX_GRID_COLUMNS) }

    val iconSizeDp: Flow<Int> = context.dataStore.data
        .map { (it[KEY_ICON_SIZE] ?: DEFAULT_ICON_SIZE)
            .coerceIn(MIN_ICON_SIZE, MAX_ICON_SIZE) }

    val tileTextSizeSp: Flow<Int> = context.dataStore.data
        .map { (it[KEY_TILE_TEXT_SIZE] ?: DEFAULT_TILE_TEXT)
            .coerceIn(MIN_TILE_TEXT, MAX_TILE_TEXT) }

    val highContrastMode: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_HIGH_CONTRAST] ?: false }

    // ---- Writes ----

    suspend fun setShowSectionTitles(value: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_SECTION_TITLES] = value }
    }

    suspend fun setGridColumns(value: Int) {
        context.dataStore.edit {
            it[KEY_GRID_COLUMNS] = value.coerceIn(MIN_GRID_COLUMNS, MAX_GRID_COLUMNS)
        }
    }

    suspend fun setIconSizeDp(value: Int) {
        context.dataStore.edit {
            it[KEY_ICON_SIZE] = value.coerceIn(MIN_ICON_SIZE, MAX_ICON_SIZE)
        }
    }

    suspend fun setTileTextSizeSp(value: Int) {
        context.dataStore.edit {
            it[KEY_TILE_TEXT_SIZE] = value.coerceIn(MIN_TILE_TEXT, MAX_TILE_TEXT)
        }
    }

    suspend fun setHighContrastMode(value: Boolean) {
        context.dataStore.edit { it[KEY_HIGH_CONTRAST] = value }
    }
}
