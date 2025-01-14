package dev.kavrin.rsable.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dev.kavrin.rsable.R
import dev.kavrin.rsable.data.local.BleScanManager
import org.koin.android.ext.android.inject

class BleForegroundService : Service() {
    private val bleScanManager: BleScanManager by inject<BleScanManager>()
    private val channelId = "BLEScanChannel"

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "BLE Scanning", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SCAN -> bleScanManager.startScan()
            ACTION_STOP_SCAN -> bleScanManager.stopScan()
        }
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        bleScanManager.stopScan()
    }

    private fun createNotification(): Notification {

        return NotificationCompat.Builder(this, channelId).apply {
            setContentTitle("BLE Scanning")
            setContentText("Scanning for devices...")
            setSmallIcon(R.drawable.ic_bluetooth_scanning)
        }.build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val ACTION_START_SCAN = "START_SCAN"
        const val ACTION_STOP_SCAN = "STOP_SCAN"
        private const val TAG = "BleForegroundService"
    }
}