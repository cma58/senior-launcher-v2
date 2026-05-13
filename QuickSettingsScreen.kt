package com.inclusion.seniorlauncher.ui.quicksettings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inclusion.seniorlauncher.ui.common.debouncedClickable

@Composable
fun QuickSettingsRoute(
    onBack: () -> Unit,
    onOpenSystemPanel: (QuickSettingsEffect) -> Unit,
    viewModel: QuickSettingsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            // ShowError handled inline if you wire a Snackbar; everything else
            // is an Android system intent to launch from the activity.
            if (effect !is QuickSettingsEffect.ShowError) {
                onOpenSystemPanel(effect)
            }
        }
    }

    QuickSettingsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack
    )
}

@Composable
fun QuickSettingsScreen(
    state: QuickSettingsState,
    onIntent: (QuickSettingsIntent) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Terug",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "Snelinstellingen",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(QuickToggle.values().toList()) { toggle ->
                    val isOn = when (toggle) {
                        QuickToggle.FLASHLIGHT -> state.isFlashlightOn
                        QuickToggle.RINGER     -> state.isRingerSilent
                        else -> false // others = launch panel, no persistent state
                    }
                    ToggleTile(
                        toggle = toggle,
                        isOn = isOn,
                        onClick = { onIntent(QuickSettingsIntent.Toggle(toggle)) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ToggleTile(
    toggle: QuickToggle,
    isOn: Boolean,
    onClick: () -> Unit
) {
    val label = if (isOn) toggle.labelOn else toggle.labelOff
    val icon = if (isOn) toggle.iconOn else toggle.iconOff
    val bgColor = if (isOn) MaterialTheme.colorScheme.primary else Color.White
    val fgColor = if (isOn) MaterialTheme.colorScheme.onPrimary else Color(0xFF121212)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(
                width = 2.dp,
                color = if (isOn) MaterialTheme.colorScheme.primary else Color(0xFFCCCCCC),
                shape = RoundedCornerShape(20.dp)
            )
            .debouncedClickable(
                role = Role.Switch,
                onClickLabel = label,
                onClick = onClick
            )
            .padding(16.dp)
            .semantics {
                contentDescription = "$label, ${if (isOn) "aan" else "uit"}"
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fgColor,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.size(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = fgColor,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
