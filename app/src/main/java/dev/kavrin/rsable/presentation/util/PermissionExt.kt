package dev.kavrin.rsable.presentation.util

import android.Manifest
import android.os.Build

object PermissionsUtil {
    val is30OrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val is33OrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU


    val blePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,

        )
    }

    val backgroundLocationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        listOf(
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    } else {
        emptyList()
    }

    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.POST_NOTIFICATIONS
    } else {
        null
    }

}