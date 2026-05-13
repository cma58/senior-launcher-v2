package com.inclusion.seniorlauncher.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private const val SOS_HOLD_MS = 3_000L

@Composable
fun HomeRoute(
    onTileIntent: (TileIntent) -> Unit,
    onSosActivated: (String, String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenQuickSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.LaunchDialer       -> Unit /* handled via tile path */
                is HomeEffect.SendSosSms         -> onSosActivated(effect.phoneNumber, effect.message)
                is HomeEffect.ResolveTileIntent  -> {
                    when (val ti = effect.intent) {
                        TileIntent.OpenSettings      -> onOpenSettings()
                        TileIntent.OpenQuickSettings -> onOpenQuickSettings()
                        else -> onTileIntent(ti)
                    }
                }
                is HomeEffect.ShowError -> Unit
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
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            TopBar(
                onOpenSettings = { onIntent(HomeIntent.TapTile(TileIntent.OpenSettings)) },
                onOpenQuickSettings = { onIntent(HomeIntent.TapTile(TileIntent.OpenQuickSettings)) }
            )

            Spacer(Modifier.height(8.dp))

            ClockCard(clock = state.clock)

            Spacer(Modifier.height(16.dp))

            // Sectioned grid is scrollable when total height exceeds viewport.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.sections.forEach { section ->
                    SectionGrid(
                        section = section,
                        showTitle = state.showSectionTitles,
                        columns = state.gridColumns,
                        iconSizeDp = state.iconSizeDp,
                        textSizeSp = state.tileTextSizeSp,
                        onTileClick = { onIntent(HomeIntent.TapTile(it)) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

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
//  Top bar — small access points to Settings and Quick Settings
// ==============================================================

@Composable
private fun TopBar(
    onOpenSettings: () -> Unit,
    onOpenQuickSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onOpenQuickSettings,
            modifier = Modifier.size(56.dp).semantics {
                contentDescription = "Snelinstellingen openen"
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Tune,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(56.dp).semantics {
                contentDescription = "Instellingen openen"
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

// ==============================================================
//  Clock card
// ==============================================================

@Composable
private fun ClockCard(clock: ClockInfo) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Column {
            Text(
                text = clock.greeting,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = clock.timeText,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = clock.dateText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==============================================================
//  Section grid — Image 1 reference layout
// ==============================================================

@Composable
private fun SectionGrid(
    section: AppSection,
    showTitle: Boolean,
    columns: Int,
    iconSizeDp: Int,
    textSizeSp: Int,
    onTileClick: (TileIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(section.backgroundColor)
            .padding(12.dp)
    ) {
        if (showTitle) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF121212), // explicit — section bg is light
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
        }
        // Manual grid: chunk tiles per row, render as Row composables.
        // LazyVerticalGrid inside verticalScroll causes nested-scroll conflicts.
        section.tiles.chunked(columns).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { tile ->
                    HomeTileCell(
                        tile = tile,
                        iconSizeDp = iconSizeDp,
                        textSizeSp = textSizeSp,
                        onClick = { onTileClick(tile.intent) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining cells when last row is short
                repeat(columns - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HomeTileCell(
    tile: HomeTile,
    iconSizeDp: Int,
    textSizeSp: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .aspectRatio(0.95f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .debouncedClickable(
                role = Role.Button,
                onClickLabel = "Open ${tile.label}",
                onClick = onClick
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = tile.icon,
            contentDescription = null,
            tint = Color(0xFF121212),
            modifier = Modifier.size(iconSizeDp.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = tile.label,
            fontSize = textSizeSp.sp,
            color = Color(0xFF121212),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// ==============================================================
//  SOS button (unchanged behaviour, 3s long-press)
// ==============================================================

@Composable
private fun SosButton(
    progress: Float,
    isPressed: Boolean,
    onPressStart: () -> Unit,
    onPressTick: (Float) -> Unit,
    onPressCancel: () -> Unit,
    onActivate: () -> Unit
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

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp)
                .clip(RoundedCornerShape(20.dp))
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
                                    delay(16L)
                                }
                            }
                            tryAwaitRelease()
                            tickerJob.cancel()
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
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = SosRedOn,
                    modifier = Modifier.size(44.dp)
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
        if (isPressed) {
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = SosRed,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 900)
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
