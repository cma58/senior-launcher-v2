package com.inclusion.seniorlauncher.ui.home

import androidx.compose.ui.graphics.Color
import com.inclusion.seniorlauncher.ui.theme.TileAccents

/**
 * MVI contract for the Home screen.
 *
 *  State  = immutable snapshot of what the UI should render.
 *  Intent = user actions flowing up.
 *  Effect = one-shot side-effects (navigation, toast, dialer).
 */

// ---- State ----

data class HomeState(
    val clock: ClockInfo = ClockInfo(),
    val contacts: List<PhotoContact> = PhotoContact.sampleContacts(),
    val sosCountdownProgress: Float = 0f, // 0f..1f while user is pressing SOS
    val isSosTriggering: Boolean = false,
    val highContrastMode: Boolean = false
)

data class ClockInfo(
    val timeText: String = "--:--",
    val dateText: String = "",
    val greeting: String = "Welkom"
)

data class PhotoContact(
    val id: String,
    val displayName: String,
    val phoneNumber: String,
    val photoUri: String? = null,
    val accentColor: Color
) {
    companion object {
        /** Placeholder data – in productie laden we uit ContactsContract. */
        fun sampleContacts(): List<PhotoContact> = listOf(
            PhotoContact("1", "Marie",     "+32471000001", accentColor = TileAccents[0]),
            PhotoContact("2", "Pieter",    "+32471000002", accentColor = TileAccents[1]),
            PhotoContact("3", "Dokter",    "+32471000003", accentColor = TileAccents[2]),
            PhotoContact("4", "Apotheek",  "+32471000004", accentColor = TileAccents[3])
        )
    }
}

// ---- Intent (from View to ViewModel) ----

sealed interface HomeIntent {
    data object ClockTick : HomeIntent
    data class CallContact(val contactId: String) : HomeIntent
    data object OpenAllApps : HomeIntent
    data object OpenSettings : HomeIntent

    // SOS flow
    data object SosPressStart : HomeIntent
    data class SosPressTick(val progress: Float) : HomeIntent
    data object SosPressCancel : HomeIntent
    data object SosActivate : HomeIntent
}

// ---- Effect (one-shot) ----

sealed interface HomeEffect {
    data class LaunchDialer(val phoneNumber: String) : HomeEffect
    data class SendSosSms(val phoneNumber: String, val message: String) : HomeEffect
    data object NavigateToSettings : HomeEffect
    data object NavigateToAllApps : HomeEffect
    data class ShowError(val messageRes: Int) : HomeEffect
}
