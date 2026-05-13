package com.inclusion.seniorlauncher.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Photo
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.inclusion.seniorlauncher.ui.theme.SectionCommunication
import com.inclusion.seniorlauncher.ui.theme.SectionMedia
import com.inclusion.seniorlauncher.ui.theme.SectionMessaging
import com.inclusion.seniorlauncher.ui.theme.SectionUtility
import com.inclusion.seniorlauncher.ui.theme.TileAccents

/**
 * MVI contract for the Home screen.
 */

// ---- State ----

data class HomeState(
    val clock: ClockInfo = ClockInfo(),
    val sections: List<AppSection> = AppSection.defaults(),
    val contacts: List<PhotoContact> = PhotoContact.sampleContacts(),
    val sosCountdownProgress: Float = 0f,
    val isSosTriggering: Boolean = false,
    // From DataStore — drive layout density
    val showSectionTitles: Boolean = true,
    val gridColumns: Int = 3,
    val iconSizeDp: Int = 96,
    val tileTextSizeSp: Int = 22
)

data class ClockInfo(
    val timeText: String = "--:--",
    val dateText: String = "",
    val greeting: String = "Welkom"
)

/**
 * A grouped, colour-coded section in the home grid (Image 1 reference).
 * Sections give cognitive scaffolding: "what kind of thing am I tapping?"
 */
data class AppSection(
    val id: String,
    val title: String,
    val backgroundColor: Color,
    val tiles: List<HomeTile>
) {
    companion object {
        fun defaults(): List<AppSection> = listOf(
            AppSection(
                id = "communication",
                title = "Bellen",
                backgroundColor = SectionCommunication,
                tiles = listOf(
                    HomeTile("recent",   "Recent",   Icons.Filled.History,  TileIntent.OpenRecent),
                    HomeTile("dialer",   "Bellen",   Icons.Filled.Phone,    TileIntent.OpenDialer),
                    HomeTile("contacts", "Contacten", Icons.Filled.Contacts, TileIntent.OpenContacts)
                )
            ),
            AppSection(
                id = "messaging",
                title = "Berichten",
                backgroundColor = SectionMessaging,
                tiles = listOf(
                    HomeTile("whatsapp",  "WhatsApp",  Icons.Filled.Chat,  TileIntent.OpenApp("com.whatsapp")),
                    HomeTile("assistant", "Assistent", Icons.Filled.Mic,   TileIntent.OpenAssistant),
                    HomeTile("messages",  "Berichten", Icons.Filled.Forum, TileIntent.OpenMessages)
                )
            ),
            AppSection(
                id = "media",
                title = "Media",
                backgroundColor = SectionMedia,
                tiles = listOf(
                    HomeTile("photos", "Foto's", Icons.Filled.Photo,     TileIntent.OpenPhotos),
                    HomeTile("camera", "Camera", Icons.Filled.CameraAlt, TileIntent.OpenCamera),
                    HomeTile("videos", "Video",  Icons.Filled.Movie,     TileIntent.OpenVideos)
                )
            ),
            AppSection(
                id = "utility",
                title = "Hulpmiddelen",
                backgroundColor = SectionUtility,
                tiles = listOf(
                    HomeTile("pills",  "Medicijnen", Icons.Filled.Medication, TileIntent.OpenMedication),
                    HomeTile("apps",   "Alle apps",  Icons.Filled.Apps,       TileIntent.OpenAllApps),
                    HomeTile("alarms", "Wekker",     Icons.Filled.Alarm,      TileIntent.OpenAlarms)
                )
            )
        )
    }
}

data class HomeTile(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val intent: TileIntent
)

/** Each tile carries an intent — host activity resolves it to an Android Intent. */
sealed interface TileIntent {
    data object OpenRecent     : TileIntent
    data object OpenDialer     : TileIntent
    data object OpenContacts   : TileIntent
    data object OpenAssistant  : TileIntent
    data object OpenMessages   : TileIntent
    data object OpenPhotos     : TileIntent
    data object OpenCamera     : TileIntent
    data object OpenVideos     : TileIntent
    data object OpenMedication : TileIntent
    data object OpenAllApps    : TileIntent
    data object OpenAlarms     : TileIntent
    data object OpenSettings   : TileIntent
    data object OpenQuickSettings : TileIntent
    data class  OpenApp(val packageName: String) : TileIntent
}

/** Legacy photo-contact model kept for direct-dial favourites. */
data class PhotoContact(
    val id: String,
    val displayName: String,
    val phoneNumber: String,
    val photoUri: String? = null,
    val accentColor: Color
) {
    companion object {
        fun sampleContacts(): List<PhotoContact> = listOf(
            PhotoContact("1", "Marie",    "+32471000001", accentColor = TileAccents[0]),
            PhotoContact("2", "Pieter",   "+32471000002", accentColor = TileAccents[1]),
            PhotoContact("3", "Dokter",   "+32471000003", accentColor = TileAccents[2]),
            PhotoContact("4", "Apotheek", "+32471000004", accentColor = TileAccents[3])
        )
    }
}

// ---- Intent (View → ViewModel) ----

sealed interface HomeIntent {
    data object ClockTick : HomeIntent
    data class TapTile(val intent: TileIntent) : HomeIntent
    data class CallContact(val contactId: String) : HomeIntent

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
    data class ResolveTileIntent(val intent: TileIntent) : HomeEffect
    data class ShowError(val messageRes: Int) : HomeEffect
}
