package com.inclusion.seniorlauncher.medication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Re-registers all scheduled medication alarms after device boot.
 *
 * Scheduled alarms are lost across reboots, so we need to iterate over
 * persisted medication schedules and re-call `AlarmManager.setAlarmClock()`
 * for each upcoming dose.
 *
 * Listens to:
 *  • ACTION_BOOT_COMPLETED — standard boot
 *  • ACTION_LOCKED_BOOT_COMPLETED — Direct Boot mode (before user unlock)
 *
 * TODO (v2 roadmap step 3):
 *  • Inject MedicationRepository
 *  • Query all active medication schedules
 *  • Reschedule via MedicationAlarmScheduler
 *
 * For now this is a no-op stub so the manifest-declared receiver resolves.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) return
        // Stub: full reschedule logic forthcoming.
    }
}
