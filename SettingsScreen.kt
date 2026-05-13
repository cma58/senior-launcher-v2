package com.inclusion.seniorlauncher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inclusion.seniorlauncher.data.preferences.LauncherPreferences
import com.inclusion.seniorlauncher.ui.theme.SectionCommunication

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack
    )
}

@Composable
fun SettingsScreen(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
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
                .verticalScroll(rememberScrollState())
        ) {
            // ---- Header ----
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
                    text = "Hoofdscherm aanpassen",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            HorizontalDivider()

            // ---- Show Section Title toggle ----
            SettingRow(
                label = "Sectietitels tonen",
                description = "Toont labels zoals \"Bellen\" of \"Media\" boven elke groep."
            ) {
                Switch(
                    checked = state.showSectionTitles,
                    onCheckedChange = { onIntent(SettingsIntent.SetShowTitles(it)) }
                )
            }

            HorizontalDivider()

            // ---- Grid Size slider ----
            SliderSetting(
                label = "Aantal kolommen",
                value = state.gridColumns,
                min = LauncherPreferences.MIN_GRID_COLUMNS,
                max = LauncherPreferences.MAX_GRID_COLUMNS,
                onValueChange = { onIntent(SettingsIntent.SetGridColumns(it)) }
            )

            // ---- Icon Size slider ----
            SliderSetting(
                label = "Icoongrootte",
                value = state.iconSize,
                min = LauncherPreferences.MIN_ICON_SIZE,
                max = LauncherPreferences.MAX_ICON_SIZE,
                onValueChange = { onIntent(SettingsIntent.SetIconSize(it)) },
                valueSuffix = "dp"
            )

            // ---- Text Size slider ----
            SliderSetting(
                label = "Tekstgrootte",
                value = state.tileTextSize,
                min = LauncherPreferences.MIN_TILE_TEXT,
                max = LauncherPreferences.MAX_TILE_TEXT,
                onValueChange = { onIntent(SettingsIntent.SetTileTextSize(it)) },
                valueSuffix = "sp"
            )

            HorizontalDivider()

            // ---- High Contrast toggle ----
            SettingRow(
                label = "Hoog contrast",
                description = "Pure zwart/wit weergave voor zeer slechtziende gebruikers."
            ) {
                Switch(
                    checked = state.highContrast,
                    onCheckedChange = { onIntent(SettingsIntent.SetHighContrast(it)) }
                )
            }

            HorizontalDivider()

            // ---- Live preview ----
            Text(
                text = "VOORBEELD",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )

            LivePreview(
                showTitle = state.showSectionTitles,
                columns = state.gridColumns,
                iconSize = state.iconSize,
                textSize = state.tileTextSize,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// --------------------------------------------------------------
//  Setting rows
// --------------------------------------------------------------

@Composable
private fun SettingRow(
    label: String,
    description: String? = null,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (description != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.size(16.dp))
        trailing()
    }
}

@Composable
private fun SliderSetting(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit,
    valueSuffix: String = ""
) {
    val displayValue = if (valueSuffix.isEmpty()) "$value" else "$value$valueSuffix"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = displayValue,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = min.toFloat()..max.toFloat(),
            steps = (max - min) - 1,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        )
    }
}

// --------------------------------------------------------------
//  Live preview — shows current settings effect on a mini section
// --------------------------------------------------------------

@Composable
private fun LivePreview(
    showTitle: Boolean,
    columns: Int,
    iconSize: Int,
    textSize: Int,
    modifier: Modifier = Modifier
) {
    val sampleTiles = listOf(
        "Bellen" to Icons.Filled.Phone,
        "Alle apps" to Icons.Filled.Apps
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SectionCommunication)
            .padding(12.dp)
    ) {
        if (showTitle) {
            Text(
                text = "Bellen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF121212),
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sampleTiles.forEach { (label, icon) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color(0xFF121212),
                            modifier = Modifier.size(iconSize.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = label,
                            fontSize = textSize.sp,
                            color = Color(0xFF121212),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
                // Fill remaining cells based on columns setting
            }
            // Filler cells to visualise grid columns
            repeat((columns - sampleTiles.size).coerceAtLeast(0)) {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}
