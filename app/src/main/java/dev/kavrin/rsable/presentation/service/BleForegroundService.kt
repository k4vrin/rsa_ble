package dev.kavrin.rsable.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dev.kavrin.rsable.R
import dev.kavrin.rsable.data.local.BleScanManager
import dev.kavrin.rsable.domain.model.BleDeviceType
import dev.kavrin.rsable.domain.model.BleScanResource
import dev.kavrin.rsable.util.safeLaunch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.koin.android.ext.android.inject

class BleForegroundService : Service() {
    private val bleScanManager: BleScanManager by inject<BleScanManager>()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val channelId = "BLEScanChannel"
    private var observerJob: Job? = null

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "BLE Scanning", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: $intent")
        when (intent?.action) {
            ACTION_START_SCAN -> {
                bleScanManager.startScan()
                startForeground(
                    NOTIFICATION_ID_SCAN,
                    createNotification("BLE Scanning", "Scanning for devices...")
                )
                observeScanResults()
            }

            ACTION_STOP_SCAN -> {

                bleScanManager.stopScan()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        bleScanManager.stopScan()
        observerJob = null
    }

    private fun createNotification(
        title: String,
        message: String,
        icon: Int = R.drawable.ic_bluetooth_scanning,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        vibratePattern: LongArray? = null,
    ): Notification {
        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(icon)
            .setPriority(priority)


        vibratePattern?.let { builder.setVibrate(it) }

        return builder.build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun observeScanResults() {
        observerJob = scope.safeLaunch {
            bleScanManager.result
                .collect { scanResource ->
                    Log.d(TAG, "observeScanResults: $scanResource")
                    if (scanResource is BleScanResource.Success) {
                        for (bleDevice in scanResource.value) {
                            when (bleDevice.type) {
                                BleDeviceType.HEART_RATE_MONITOR -> {
                                    notifyUser("Target device found", "A heart rate monitor found.")
                                }
                                else -> Unit
                            }
                        }
                    }
                }
        }
    }

    private fun notifyUser(title: String, message: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notification = createNotification(
            title,
            message,
            android.R.drawable.ic_dialog_info,
            NotificationCompat.PRIORITY_HIGH,
            longArrayOf(0, 500, 200, 500)
        )

        notificationManager.notify(NOTIFICATION_ID_NOTIFY_TARGET, notification)
    }

    companion object {
        const val NOTIFICATION_ID_SCAN = 1
        const val NOTIFICATION_ID_NOTIFY_TARGET = 2
        const val ACTION_START_SCAN = "dev.kavrin.rsable.START_SCAN"
        const val ACTION_STOP_SCAN = "dev.kavrin.rsable.STOP_SCAN"
        private const val TAG = "BleForegroundService"
    }
}