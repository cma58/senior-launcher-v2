package com.inclusion.seniorlauncher

import android.Manifest
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.SmsManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.inclusion.seniorlauncher.ui.home.HomeRoute
import com.inclusion.seniorlauncher.ui.home.TileIntent
import com.inclusion.seniorlauncher.ui.quicksettings.QuickSettingsEffect
import com.inclusion.seniorlauncher.ui.quicksettings.QuickSettingsRoute
import com.inclusion.seniorlauncher.ui.settings.SettingsRoute
import com.inclusion.seniorlauncher.ui.theme.SeniorLauncherTheme

class MainActivity : ComponentActivity() {

    private val requestRuntimePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* user decision logged */ }

    private val requestLauncherRole = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* result handled by system */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        promptDefaultLauncherIfNeeded()
        requestPermissions()

        setContent {
            SeniorLauncherTheme {
                LauncherNavHost(
                    onTileIntent = ::resolveTileIntent,
                    onSendSosSms = ::sendSosSms,
                    onOpenSystemPanel = ::openSystemPanel
                )
            }
        }
    }

    // -------- Default launcher prompt --------

    private fun promptDefaultLauncherIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val rm = getSystemService(Context.ROLE_SERVICE) as? RoleManager ?: return
        if (rm.isRoleAvailable(RoleManager.ROLE_HOME) && !rm.isRoleHeld(RoleManager.ROLE_HOME)) {
            requestLauncherRole.launch(rm.createRequestRoleIntent(RoleManager.ROLE_HOME))
        }
    }

    // -------- Runtime permissions --------

    private fun requestPermissions() {
        val needed = buildList {
            if (!has(Manifest.permission.SEND_SMS)) add(Manifest.permission.SEND_SMS)
            if (!has(Manifest.permission.ACCESS_FINE_LOCATION)) add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (!has(Manifest.permission.CALL_PHONE)) add(Manifest.permission.CALL_PHONE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !has(Manifest.permission.POST_NOTIFICATIONS)
            ) add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (needed.isNotEmpty()) requestRuntimePermissions.launch(needed.toTypedArray())
    }

    private fun has(perm: String): Boolean =
        ContextCompat.checkSelfPermission(this, perm) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

    // -------- Tile intent resolution --------

    /**
     * Maps a launcher tile intent to a real Android intent.
     * Falls back gracefully when target app is missing.
     */
    private fun resolveTileIntent(tile: TileIntent) {
        val intent: Intent? = when (tile) {
            TileIntent.OpenDialer     -> Intent(Intent.ACTION_DIAL)
            TileIntent.OpenContacts   -> Intent(Intent.ACTION_VIEW, Uri.parse("content://contacts/people/"))
            TileIntent.OpenRecent     -> Intent(Intent.ACTION_VIEW, Uri.parse("content://call_log/calls"))
            TileIntent.OpenAssistant  -> Intent(Intent.ACTION_VOICE_COMMAND)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            TileIntent.OpenMessages   -> Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING)
            TileIntent.OpenCamera     -> Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            TileIntent.OpenPhotos     -> Intent(Intent.ACTION_VIEW).apply {
                type = "image/*"
                setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            }
            TileIntent.OpenVideos     -> Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*")
            }
            TileIntent.OpenAlarms     -> Intent(AlarmClock.ACTION_SHOW_ALARMS)
            TileIntent.OpenAllApps    -> Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MARKET)
            TileIntent.OpenMedication -> null // TODO route to internal medication module
            TileIntent.OpenSettings,
            TileIntent.OpenQuickSettings -> null // handled in NavHost
            is TileIntent.OpenApp -> packageManager.getLaunchIntentForPackage(tile.packageName)
        }
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            intent?.let { startActivity(it) }
        } catch (_: ActivityNotFoundException) {
            // Silent fallback — show a Snackbar via your effect channel in production.
        }
    }

    // -------- System panels (Quick Settings tile actions) --------

    private fun openSystemPanel(effect: QuickSettingsEffect) {
        val action = when (effect) {
            QuickSettingsEffect.OpenWifiPanel       ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Settings.Panel.ACTION_WIFI
                else Settings.ACTION_WIFI_SETTINGS
            QuickSettingsEffect.OpenBluetoothPanel  -> Settings.ACTION_BLUETOOTH_SETTINGS
            QuickSettingsEffect.OpenDisplaySettings -> Settings.ACTION_DISPLAY_SETTINGS
            QuickSettingsEffect.OpenAirplaneSettings-> Settings.ACTION_AIRPLANE_MODE_SETTINGS
            QuickSettingsEffect.OpenSoundSettings   -> Settings.ACTION_SOUND_SETTINGS
            is QuickSettingsEffect.ShowError -> return
        }
        try {
            startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    // -------- SOS SMS --------

    private fun sendSosSms(phoneNumber: String, message: String) {
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION") SmsManager.getDefault()
            }
            smsManager?.sendTextMessage(phoneNumber, null, message, null, null)
        } catch (_: Exception) {
            // TODO surface error to UI; for now swallow to avoid crash during alarm flow.
        }
    }
}

@Composable
private fun LauncherNavHost(
    onTileIntent: (TileIntent) -> Unit,
    onSendSosSms: (String, String) -> Unit,
    onOpenSystemPanel: (QuickSettingsEffect) -> Unit
) {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeRoute(
                onTileIntent = onTileIntent,
                onSosActivated = onSendSosSms,
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenQuickSettings = { navController.navigate(Routes.QUICK_SETTINGS) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }
        composable(Routes.QUICK_SETTINGS) {
            QuickSettingsRoute(
                onBack = { navController.popBackStack() },
                onOpenSystemPanel = onOpenSystemPanel
            )
        }
    }
}

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val QUICK_SETTINGS = "quick_settings"
}
