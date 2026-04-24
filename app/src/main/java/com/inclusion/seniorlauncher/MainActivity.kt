package com.inclusion.seniorlauncher

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.inclusion.seniorlauncher.ui.home.HomeRoute
import com.inclusion.seniorlauncher.ui.theme.SeniorLauncherTheme

/**
 * Single-activity host for the launcher.
 *
 *  • Edge-to-edge: enabled via [enableEdgeToEdge] — safe drawing insets are
 *    honored inside each screen's Scaffold.
 *  • Predictive back: opt-in via manifest flag + Compose's built-in
 *    [androidx.activity.compose.PredictiveBackHandler] inside screens that need
 *    custom behavior (NavHost handles stack pops natively).
 *  • Default launcher: on Android 10+ we request [RoleManager.ROLE_HOME].
 *    On older versions we rely on the HOME intent-filter only.
 */
class MainActivity : ComponentActivity() {

    private val requestSmsPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* User decision logged — no blocking UI. */ }

    private val requestLauncherRole = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* Result handled by system. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        promptDefaultLauncherIfNeeded()
        requestRuntimePermissions()

        setContent {
            SeniorLauncherTheme {
                SeniorLauncherNavHost(
                    onCallDial = ::launchDialer,
                    onSendSosSms = ::sendSosSms
                )
            }
        }
    }

    // -------- Default launcher prompt --------

    private fun promptDefaultLauncherIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = getSystemService(Context.ROLE_SERVICE) as? RoleManager ?: return
            if (rm.isRoleAvailable(RoleManager.ROLE_HOME) &&
                !rm.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                requestLauncherRole.launch(rm.createRequestRoleIntent(RoleManager.ROLE_HOME))
            }
        }
    }

    // -------- Runtime permissions --------

    private fun requestRuntimePermissions() {
        val needed = buildList {
            if (!hasPermission(Manifest.permission.SEND_SMS)) add(Manifest.permission.SEND_SMS)
            if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (!hasPermission(Manifest.permission.CALL_PHONE)) add(Manifest.permission.CALL_PHONE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !hasPermission(Manifest.permission.POST_NOTIFICATIONS)
            ) add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (needed.isNotEmpty()) requestSmsPermission.launch(needed.toTypedArray())
    }

    private fun hasPermission(perm: String): Boolean =
        ContextCompat.checkSelfPermission(this, perm) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

    // -------- Outbound intents --------

    private fun launchDialer(phoneNumber: String) {
        // ACTION_DIAL opens the dialer with number pre-filled — no CALL_PHONE required.
        // Use ACTION_CALL for direct call (requires permission + UX consideration).
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun sendSosSms(phoneNumber: String, message: String) {
        // NB: in production, wrap in try/catch and surface failure via HomeEffect.ShowError.
        // Also append GPS coordinates from FusedLocationProviderClient before sending.
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION") SmsManager.getDefault()
        }
        smsManager?.sendTextMessage(phoneNumber, null, message, null, null)
    }
}

/**
 * Navigation graph. Kept minimal here; expand with Settings, AllApps, Medication routes.
 *
 * Using string-based routes for simplicity. To migrate to type-safe routes
 * (Compose Nav 2.8+), add the kotlinx.serialization plugin and use
 * @Serializable data objects.
 */
@Composable
private fun SeniorLauncherNavHost(
    onCallDial: (String) -> Unit,
    onSendSosSms: (String, String) -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeRoute(
                onCallContact = onCallDial,
                onSosActivated = onSendSosSms
            )
        }
        // composable(Routes.SETTINGS) { SettingsRoute() }
        // composable(Routes.ALL_APPS) { AllAppsRoute() }
    }
}

/**
 * Navigation route constants.
 */
object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val ALL_APPS = "all_apps"
}
