package com.inclusion.seniorlauncher.medication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives medication reminders scheduled via [android.app.AlarmManager.setAlarmClock].
 *
 * TODO (v2 roadmap step 3):
 *  • Read medication metadata from extras or Repository
 *  • Show full-screen notification with medication name + photo
 *  • Trigger confirmation flow ("Ik heb mijn pil genomen")
 *  • Reschedule next occurrence
 *
 * For now this is a no-op stub so the manifest-declared receiver resolves.
 */
class MedicationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Stub: full implementation forthcoming.
    }
}
