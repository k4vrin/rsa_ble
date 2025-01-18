package dev.kavrin.rsable.domain.model

data class BleDevice(
    val macAddress: MacAddress,
    val name: String?,
    val rssi: Int,
    val previousRssi: Int,
    val highestRssi: Int,
    val type: BleDeviceType,
    val isConnectable: Boolean,
    val services: List<GattService> = emptyList()
)