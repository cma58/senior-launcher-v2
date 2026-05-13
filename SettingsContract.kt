package com.inclusion.seniorlauncher.ui.settings

import com.inclusion.seniorlauncher.data.preferences.LauncherPreferences

data class SettingsState(
    val showSectionTitles: Boolean = LauncherPreferences.DEFAULT_SHOW_TITLES,
    val gridColumns: Int = LauncherPreferences.DEFAULT_GRID_COLUMNS,
    val iconSize: Int = LauncherPreferences.DEFAULT_ICON_SIZE,
    val tileTextSize: Int = LauncherPreferences.DEFAULT_TILE_TEXT,
    val highContrast: Boolean = false
)

sealed interface SettingsIntent {
    data class SetShowTitles(val value: Boolean) : SettingsIntent
    data class SetGridColumns(val value: Int) : SettingsIntent
    data class SetIconSize(val value: Int) : SettingsIntent
    data class SetTileTextSize(val value: Int) : SettingsIntent
    data class SetHighContrast(val value: Boolean) : SettingsIntent
}
