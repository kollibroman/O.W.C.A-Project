package com.example.antyprokrastynoinator_2000.monitor

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telecom.TelecomManager

class ForegroundAppDetector(context: Context) {

    private val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)
    private var lastQueryTime = System.currentTimeMillis() - INITIAL_LOOKBACK_MS
    private var currentPackage: String? = null

    fun currentForegroundPackage(): String? {
        val now = System.currentTimeMillis()
        // Lekkie nakładanie się okien zapytań, żeby nie zgubić zdarzeń na granicy.
        val events = usageStatsManager.queryEvents(lastQueryTime - OVERLAP_MS, now)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            @Suppress("DEPRECATION")
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                currentPackage = event.packageName
            }
        }
        lastQueryTime = now
        return currentPackage
    }

    companion object {
        private const val INITIAL_LOOKBACK_MS = 10 * 60 * 1000L
        private const val OVERLAP_MS = 2_000L

        fun allowedPackages(context: Context): Set<String> {
            val pm = context.packageManager
            val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            @Suppress("DEPRECATION")
            val launchers = pm.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
                .map { it.activityInfo.packageName }
            val dialer = context.getSystemService(TelecomManager::class.java)?.defaultDialerPackage

            return buildSet {
                add(context.packageName)
                add("com.android.systemui")
                add("com.android.settings")
                addAll(launchers)
                dialer?.let { add(it) }
            }
        }
    }
}
