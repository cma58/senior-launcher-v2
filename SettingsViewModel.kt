package com.inclusion.seniorlauncher.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inclusion.seniorlauncher.data.preferences.LauncherPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = LauncherPreferences(app.applicationContext)

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefs.showSectionTitles,
                prefs.gridColumns,
                prefs.iconSizeDp,
                prefs.tileTextSizeSp,
                prefs.highContrastMode
            ) { showTitles, cols, icon, text, hc ->
                SettingsState(
                    showSectionTitles = showTitles,
                    gridColumns = cols,
                    iconSize = icon,
                    tileTextSize = text,
                    highContrast = hc
                )
            }.collect { _state.value = it }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        viewModelScope.launch {
            when (intent) {
                is SettingsIntent.SetShowTitles    -> prefs.setShowSectionTitles(intent.value)
                is SettingsIntent.SetGridColumns   -> prefs.setGridColumns(intent.value)
                is SettingsIntent.SetIconSize      -> prefs.setIconSizeDp(intent.value)
                is SettingsIntent.SetTileTextSize  -> prefs.setTileTextSizeSp(intent.value)
                is SettingsIntent.SetHighContrast  -> prefs.setHighContrastMode(intent.value)
            }
        }
    }
}
