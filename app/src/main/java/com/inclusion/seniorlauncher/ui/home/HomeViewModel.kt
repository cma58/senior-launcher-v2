package com.inclusion.seniorlauncher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
 * Threading model:
 *  - All state mutations happen on viewModelScope (Main by default).
 *  - State is exposed as immutable StateFlow.
 *  - One-shot effects flow through a Channel to prevent replay on config change.
 */
class HomeViewModel : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        startClockLoop()
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.ClockTick -> refreshClock()
            is HomeIntent.CallContact -> handleCall(intent.contactId)
            HomeIntent.OpenAllApps  -> _effects.trySend(HomeEffect.NavigateToAllApps)
            HomeIntent.OpenSettings -> _effects.trySend(HomeEffect.NavigateToSettings)

            HomeIntent.SosPressStart -> _state.update {
                copy(sosCountdownProgress = 0f, isSosTriggering = true)
            }
            is HomeIntent.SosPressTick -> _state.update {
                copy(sosCountdownProgress = intent.progress)
            }
            HomeIntent.SosPressCancel -> _state.update {
                copy(sosCountdownProgress = 0f, isSosTriggering = false)
            }
            HomeIntent.SosActivate -> activateSos()
        }
    }

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
        _state.update {
            copy(
                clock = ClockInfo(
                    timeText = timeFmt.format(Date()),
                    dateText = dateFmt.format(Date()).replaceFirstChar { it.uppercase() },
                    greeting = greeting
                )
            )
        }
    }

    // ---- Contact call ----

    private fun handleCall(contactId: String) {
        val contact = _state.value.contacts.firstOrNull { it.id == contactId } ?: return
        _effects.trySend(HomeEffect.LaunchDialer(contact.phoneNumber))
    }

    // ---- SOS ----

    private fun activateSos() = viewModelScope.launch {
        _state.update { copy(isSosTriggering = false, sosCountdownProgress = 0f) }
        // In productie: haal configured SOS-contact uit Repository.
        val emergencyContact = _state.value.contacts.firstOrNull()?.phoneNumber ?: return@launch
        val message = "NOODOPROEP: Ik heb hulp nodig. Locatie volgt in aparte melding."
        _effects.send(HomeEffect.SendSosSms(emergencyContact, message))
    }

    // ---- Helpers ----

    private inline fun MutableStateFlow<HomeState>.update(block: HomeState.() -> HomeState) {
        value = value.block()
    }
}
