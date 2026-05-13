package com.inclusion.seniorlauncher.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inclusion.seniorlauncher.data.preferences.LauncherPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * MVI ViewModel for HomeScreen.
 *
 * Subscribes to LauncherPreferences so layout responds instantly to changes
 * made on the Home Customisation screen (Image 2 reference).
 */
class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = LauncherPreferences(app.applicationContext)

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        observePreferences()
        startClockLoop()
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.ClockTick -> refreshClock()
            is HomeIntent.TapTile -> _effects.trySend(HomeEffect.ResolveTileIntent(intent.intent))
            is HomeIntent.CallContact -> handleCall(intent.contactId)

            HomeIntent.SosPressStart -> _state.mutate {
                copy(sosCountdownProgress = 0f, isSosTriggering = true)
            }
            is HomeIntent.SosPressTick -> _state.mutate {
                copy(sosCountdownProgress = intent.progress)
            }
            HomeIntent.SosPressCancel -> _state.mutate {
                copy(sosCountdownProgress = 0f, isSosTriggering = false)
            }
            HomeIntent.SosActivate -> activateSos()
        }
    }

    // ---- Preferences ----

    private fun observePreferences() = viewModelScope.launch {
        combine(
            prefs.showSectionTitles,
            prefs.gridColumns,
            prefs.iconSizeDp,
            prefs.tileTextSizeSp
        ) { showTitles, cols, iconSize, textSize ->
            LayoutPrefs(showTitles, cols, iconSize, textSize)
        }.collect { p ->
            _state.mutate {
                copy(
                    showSectionTitles = p.showTitles,
                    gridColumns = p.cols,
                    iconSizeDp = p.iconSize,
                    tileTextSizeSp = p.textSize
                )
            }
        }
    }

    private data class LayoutPrefs(
        val showTitles: Boolean,
        val cols: Int,
        val iconSize: Int,
        val textSize: Int
    )

    // ---- Clock ----

    private fun startClockLoop() = viewModelScope.launch {
        while (isActive) {
            refreshClock()
            delay(1_000L)
        }
    }

    private fun refreshClock() {
        val now = Calendar.getInstance()
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFmt = SimpleDateFormat("EEEE d MMMM", Locale.getDefault())
        val greeting = when (now.get(Calendar.HOUR_OF_DAY)) {
            in 5..11  -> "Goedemorgen"
            in 12..17 -> "Goedemiddag"
            in 18..22 -> "Goedenavond"
            else      -> "Welterusten"
        }
        _state.mutate {
            copy(
                clock = ClockInfo(
                    timeText = timeFmt.format(Date()),
                    dateText = dateFmt.format(Date()).replaceFirstChar { it.uppercase() },
                    greeting = greeting
                )
            )
        }
    }

    // ---- Contact / SOS ----

    private fun handleCall(contactId: String) {
        val contact = _state.value.contacts.firstOrNull { it.id == contactId } ?: return
        _effects.trySend(HomeEffect.LaunchDialer(contact.phoneNumber))
    }

    private fun activateSos() = viewModelScope.launch {
        _state.mutate { copy(isSosTriggering = false, sosCountdownProgress = 0f) }
        val emergencyContact = _state.value.contacts.firstOrNull()?.phoneNumber ?: return@launch
        val message = "NOODOPROEP: Ik heb hulp nodig. Locatie volgt in aparte melding."
        _effects.send(HomeEffect.SendSosSms(emergencyContact, message))
    }

    // ---- Helpers ----

    private inline fun MutableStateFlow<HomeState>.mutate(block: HomeState.() -> HomeState) {
        value = value.block()
    }
}
