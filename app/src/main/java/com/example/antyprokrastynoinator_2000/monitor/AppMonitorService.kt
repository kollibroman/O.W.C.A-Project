package com.example.antyprokrastynoinator_2000.monitor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.antyprokrastynoinator_2000.MainActivity
import com.example.antyprokrastynoinator_2000.data.TaskRepository

class AppMonitorService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var detector: ForegroundAppDetector
    private lateinit var overlay: BlockingOverlay
    private lateinit var allowedPackages: Set<String>

    private val checkRunnable = object : Runnable {
        override fun run() {
            if (checkForegroundApp()) {
                handler.postDelayed(this, CHECK_INTERVAL_MS)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        detector = ForegroundAppDetector(this)
        allowedPackages = ForegroundAppDetector.allowedPackages(this)
        overlay = BlockingOverlay(this, onReturnToTask = ::returnToTask)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
        handler.removeCallbacks(checkRunnable)
        handler.post(checkRunnable)
        // Zadania są tylko w pamięci, więc po zabiciu procesu nie ma czego pilnować.
        return START_NOT_STICKY
    }

    // Zwraca false, gdy serwis się zatrzymuje.
    private fun checkForegroundApp(): Boolean {
        if (!TaskRepository.hasUpcomingOrActiveTask()) {
            stopSelf()
            return false
        }

        val activeTask = TaskRepository.activeTask()
        val screenOn = getSystemService(PowerManager::class.java).isInteractive
        if (activeTask == null || !screenOn || !MonitorPermissions.isMonitoringReady(this)) {
            overlay.hide()
            return true
        }

        val foreground = detector.currentForegroundPackage()
        if (foreground != null && foreground !in allowedPackages) {
            overlay.show(activeTask)
        } else {
            overlay.hide()
        }
        return true
    }

    private fun returnToTask() {
        overlay.hide()
        startActivity(
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Monitorowanie zadań",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Pilnuje, żebyś nie korzystał z innych aplikacji w trakcie zadania" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentTitle("Prokrastynoinator2000 czuwa")
        .setContentText("W trakcie zadania inne aplikacje będą blokowane")
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    override fun onDestroy() {
        handler.removeCallbacks(checkRunnable)
        overlay.hide()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "app_monitor"
        private const val NOTIFICATION_ID = 1001
        private const val CHECK_INTERVAL_MS = 2_000L

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, AppMonitorService::class.java)
            )
        }
    }
}
