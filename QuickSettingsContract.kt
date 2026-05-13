package com.inclusion.seniorlauncher.ui.quicksettings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

data class QuickSettingsState(
    val isFlashlightOn: Boolean = false,
    val isRingerSilent: Boolean = false
)

/**
 * Senior-friendly large toggle tiles (Image 3 reference).
 * Most system toggles need user confirmation on modern Android, so we
 * route through Settings panels instead of toggling silently. Flashlight
 * is the exception — direct CameraManager torch control, no permission.
 */
enum class QuickToggle(
    val labelOn: String,
    val labelOff: String,
    val iconOn: ImageVector,
    val iconOff: ImageVector
) {
    WIFI(
        labelOn = "Wifi",
        labelOff = "Wifi",
        iconOn = Icons.Filled.Wifi,
        iconOff = Icons.Filled.Wifi
    ),
    BLUETOOTH(
        labelOn = "Bluetooth",
        labelOff = "Bluetooth",
        iconOn = Icons.Filled.Bluetooth,
        iconOff = Icons.Filled.Bluetooth
    ),
    BRIGHTNESS(
        labelOn = "Helderheid",
        labelOff = "Helderheid",
        iconOn = Icons.Filled.BrightnessHigh,
        iconOff = Icons.Filled.BrightnessHigh
    ),
    FLASHLIGHT(
        labelOn = "Zaklamp aan",
        labelOff = "Zaklamp",
        iconOn = Icons.Filled.FlashlightOn,
        iconOff = Icons.Filled.FlashlightOff
    ),
    AIRPLANE(
        labelOn = "Vliegmodus",
        labelOff = "Vliegmodus",
        iconOn = Icons.Filled.AirplanemodeActive,
        iconOff = Icons.Filled.AirplanemodeActive
    ),
    RINGER(
        labelOn = "Geluid uit",
        labelOff = "Geluid aan",
        iconOn = Icons.Filled.NotificationsOff,
        iconOff = Icons.Filled.NotificationsActive
    )
}

sealed interface QuickSettingsIntent {
    data class Toggle(val which: QuickToggle) : QuickSettingsIntent
}
