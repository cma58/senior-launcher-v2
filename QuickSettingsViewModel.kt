package com.inclusion.seniorlauncher.ui.quicksettings

import android.app.Application
import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class QuickSettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(QuickSettingsState())
    val state: StateFlow<QuickSettingsState> = _state.asStateFlow()

    private val _effects = Channel<QuickSettingsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val cameraManager: CameraManager? =
        app.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            _state.value = _state.value.copy(isFlashlightOn = enabled)
        }
    }

    init {
        cameraManager?.registerTorchCallback(torchCallback, null)
    }

    override fun onCleared() {
        cameraManager?.unregisterTorchCallback(torchCallback)
        super.onCleared()
    }

    fun onIntent(intent: QuickSettingsIntent) {
        when (intent) {
            is QuickSettingsIntent.Toggle -> when (intent.which) {
                QuickToggle.FLASHLIGHT -> toggleFlashlight()
                QuickToggle.WIFI       -> _effects.trySend(QuickSettingsEffect.OpenWifiPanel)
                QuickToggle.BLUETOOTH  -> _effects.trySend(QuickSettingsEffect.OpenBluetoothPanel)
                QuickToggle.BRIGHTNESS -> _effects.trySend(QuickSettingsEffect.OpenDisplaySettings)
                QuickToggle.AIRPLANE   -> _effects.trySend(QuickSettingsEffect.OpenAirplaneSettings)
                QuickToggle.RINGER     -> _effects.trySend(QuickSettingsEffect.OpenSoundSettings)
            }
        }
    }

    private fun toggleFlashlight() {
        val cm = cameraManager ?: return
        try {
            // Pick the first camera with flash.
            val camId = cm.cameraIdList.firstOrNull { id ->
                cm.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return
            val newState = !_state.value.isFlashlightOn
            cm.setTorchMode(camId, newState)
            // Callback will update state.
        } catch (_: Exception) {
            _effects.trySend(QuickSettingsEffect.ShowError("Zaklamp niet beschikbaar"))
        }
    }
}

sealed interface QuickSettingsEffect {
    data object OpenWifiPanel       : QuickSettingsEffect
    data object OpenBluetoothPanel  : QuickSettingsEffect
    data object OpenDisplaySettings : QuickSettingsEffect
    data object OpenAirplaneSettings: QuickSettingsEffect
    data object OpenSoundSettings   : QuickSettingsEffect
    data class  ShowError(val message: String) : QuickSettingsEffect
}
