package dev.kavrin.rsable.presentation.util

import android.Manifest.permission
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun isBluetoothEnabled(): State<Boolean> {
    val context = LocalContext.current
    val bluetoothState = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val state =
                    intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                bluetoothState.value = (state == BluetoothAdapter.STATE_ON)
            }
        }
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(receiver, filter)

        val blManager = context.getSystemService(BluetoothManager::class.java)
        bluetoothState.value = blManager.adapter?.isEnabled == true

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
    return bluetoothState
}


fun Context.isPeripheralModeSupported(): Boolean {
    val blManager = this.getSystemService(BluetoothManager::class.java)
    return blManager.adapter.isMultipleAdvertisementSupported
}

@RequiresPermission(permission.ACCESS_FINE_LOCATION)
fun observeLocationState(context: Context): StateFlow<Boolean> {
    val locationState = MutableStateFlow(context.isLocationEnabled())

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationState.value = context.isLocationEnabled()
        }

        override fun onProviderEnabled(provider: String) {
            locationState.value = context.isLocationEnabled()
        }

        override fun onProviderDisabled(provider: String) {
            locationState.value = context.isLocationEnabled()
        }
    }

    locationManager.registerGnssStatusCallback(
        object : GnssStatus.Callback() {
            override fun onStarted() {
                locationState.value = context.isLocationEnabled()
            }

            override fun onStopped() {
                locationState.value = context.isLocationEnabled()
            }
        },
        null
    )

    return locationState
}

fun Context.isLocationEnabled(): Boolean {
    val locationManager =
        this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val isNetworkEnabled =
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) locationManager.isLocationEnabled else (isGPSEnabled || isNetworkEnabled)
}

fun Context.isBleSupported(): Boolean {
    return this.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
}
