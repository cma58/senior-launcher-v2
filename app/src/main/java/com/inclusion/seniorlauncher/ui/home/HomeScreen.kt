package com.inclusion.seniorlauncher.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inclusion.seniorlauncher.ui.common.debouncedClickable
import com.inclusion.seniorlauncher.ui.theme.SeniorLauncherTheme
import com.inclusion.seniorlauncher.ui.theme.SosRed
import com.inclusion.seniorlauncher.ui.theme.SosRedOn
import com.inclusion.seniorlauncher.ui.theme.SosRedPressed
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Long-press duration for SOS activation. 3 seconds is the spec minimum —
 * long enough to prevent accidental trigger, short enough to be usable in
 * real emergency.
 */
private const val SOS_HOLD_MS = 3_000L

/**
 * Home screen route.
 *
 * Layout from top to bottom:
 *   1. Clock + greeting (displayLarge, pure informational)
 *   2. 2×2 photo contact grid (weight=1, fills available space)
 *   3. SOS button (heightIn min=120dp, long-press only)
 *
 * All taps route through [debouncedClickable]. The SOS uses raw pointer input
 * because it needs continuous press tracking, not a click event.
 */
@Composable
fun HomeRoute(
    onCallContact: (String) -> Unit,
    onSosActivated: (String, String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Pipe one-shot effects to the host.
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.LaunchDialer -> onCallContact(effect.phoneNumber)
                is HomeEffect.SendSosSms   -> onSosActivated(effect.phoneNumber, effect.message)
                else -> Unit /* settings / all-apps handled in NavHost */
            }
        }
    }

    HomeScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun HomeScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // Honor system bar insets without letting the scaffold consume them —
        // we want to draw behind but pad content.
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            ClockHeader(
                clock = state.clock,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            ContactsGrid(
                contacts = state.contacts,
                onContactClick = { onIntent(HomeIntent.CallContact(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(Modifier.height(24.dp))

            SosButton(
                progress = state.sosCountdownProgress,
                isPressed = state.isSosTriggering,
                onPressStart  = { onIntent(HomeIntent.SosPressStart) },
                onPressTick   = { onIntent(HomeIntent.SosPressTick(it)) },
                onPressCancel = { onIntent(HomeIntent.SosPressCancel) },
                onActivate    = { onIntent(HomeIntent.SosActivate) }
            )
        }
    }
}

// ==============================================================
//  Clock Header
// ==============================================================

@Composable
private fun ClockHeader(
    clock: ClockInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.semantics {
            // Screen readers announce time changes automatically.
            liveRegion = LiveRegionMode.Polite
        },
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = clock.greeting,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = clock.timeText,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = clock.dateText,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==============================================================
//  Contacts Grid  (2×2 default)
// ==============================================================

@Composable
private fun ContactsGrid(
    contacts: List<PhotoContact>,
    onContactClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // switch to 3 voor 3×2 layout
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(contacts, key = { it.id }) { contact ->
            ContactTile(
                contact = contact,
                onClick = { onContactClick(contact.id) }
            )
        }
    }
}

@Composable
private fun ContactTile(
    contact: PhotoContact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(3.dp, contact.accentColor, RoundedCornerShape(24.dp))
            .debouncedClickable(
                role = Role.Button,
                onClickLabel = "Bel ${contact.displayName}",
                onClick = onClick
            )
            .padding(16.dp) // required 16dp padding — prevents accidental taps
            .semantics {
                contentDescription = "Foto-snelkoppeling. Bel ${contact.displayName}."
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Photo placeholder. Replace with Coil AsyncImage for real photos.
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(contact.accentColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null, // decorative — name below carries meaning
                tint = Color.White,
                modifier = Modifier.size(72.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = contact.displayName,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==============================================================
//  SOS Button  (long-press 3s + visual countdown)
// ==============================================================

@Composable
private fun SosButton(
    progress: Float,
    isPressed: Boolean,
    onPressStart: () -> Unit,
    onPressTick: (Float) -> Unit,
    onPressCancel: () -> Unit,
    onActivate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val targetBg by animateColorAsState(
        targetValue = if (isPressed) SosRedPressed else SosRed,
        label = "sosBg"
    )
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "sosProgress"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp) // well above 56dp minimum — emergency button
                .clip(RoundedCornerShape(24.dp))
                .background(targetBg)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onPressStart()
                            val startMs = System.currentTimeMillis()
                            val tickerJob = scope.launch {
                                while (isActive) {
                                    val elapsed = System.currentTimeMillis() - startMs
                                    val p = (elapsed.toFloat() / SOS_HOLD_MS).coerceIn(0f, 1f)
                                    onPressTick(p)
                                    if (p >= 1f) {
                                        onActivate()
                                        return@launch
                                    }
                                    delay(16L) // ~60fps
                                }
                            }
                            tryAwaitRelease()
                            tickerJob.cancel()
                            // If activation already fired, state will be reset by VM.
                            val elapsed = System.currentTimeMillis() - startMs
                            if (elapsed < SOS_HOLD_MS) onPressCancel()
                        }
                    )
                }
                .semantics {
                    role = Role.Button
                    contentDescription = "Noodknop. Houd drie seconden ingedrukt om hulp op te roepen."
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = SosRedOn,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.size(16.dp))
                Text(
                    text = if (isPressed) "Houd vast…" else "Noodhulp",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SosRedOn,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Countdown progress bar. Only visible while pressing.
        if (isPressed) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = SosRed,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }
    }
}

// ==============================================================
//  Preview
// ==============================================================

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun HomeScreenPreview() {
    SeniorLauncherTheme {
        HomeScreen(
            state = HomeState(
                clock = ClockInfo("14:23", "Dinsdag 23 april", "Goedemiddag")
            ),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800, uiMode = 0x21)
@Composable
private fun HomeScreenDarkPreview() {
    SeniorLauncherTheme(darkTheme = true) {
        HomeScreen(
            state = HomeState(
                clock = ClockInfo("14:23", "Dinsdag 23 april", "Goedemiddag"),
                sosCountdownProgress = 0.6f,
                isSosTriggering = true
            ),
            onIntent = {}
        )
    }
}
