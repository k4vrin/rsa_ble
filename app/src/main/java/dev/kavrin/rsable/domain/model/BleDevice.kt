package dev.kavrin.rsable.domain.model

data class BleDevice(
    val macAddress: String,
    val name: String?,
    val rssi: Int,
    val previousRssi: Int,
    val highestRssi: Int,
    val bleDeviceType: BleDeviceType
)