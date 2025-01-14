package dev.kavrin.rsable.data.dto

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Parcelable
import dev.kavrin.rsable.domain.model.BleDevice
import dev.kavrin.rsable.domain.model.BleDeviceType
import dev.kavrin.rsable.domain.model.MacAddress
import kotlinx.parcelize.Parcelize

@Suppress("unused")
@SuppressLint("MissingPermission")
@Parcelize
data class DiscoveredBleDevice(
    val device: BluetoothDevice,
    val scanResult: ScanResult? = null,
    val name: String? = null,
    val rssi: Int = 0,
    val previousRssi: Int = 0,
    val highestRssi: Int = 0,
) : Parcelable {

    fun isRssiLevelDifferent(): Boolean {
        return getRssiLevel(rssi) != getRssiLevel(previousRssi)
    }


    private fun getRssiLevel(rssi: Int): RssiLevel {
        return when (rssi) {
            in Int.MIN_VALUE..10 -> RssiLevel.WEAKEST
            in 11..28 -> RssiLevel.WEAK
            in 29..45 -> RssiLevel.MODERATE
            in 46..65 -> RssiLevel.STRONG
            else -> RssiLevel.STRONGEST
        }
    }

    fun update(scanResult: ScanResult, name: String? = null): DiscoveredBleDevice {
        return copy(
            device = scanResult.device,
            scanResult = scanResult,
            name = name ?: scanResult.scanRecord?.deviceName, // Simplified name assignment
            previousRssi = rssi,
            rssi = scanResult.rssi,
            highestRssi = maxOf(highestRssi, rssi) // Using maxOf for clarity
        )
    }

    fun matches(scanResult: ScanResult): Boolean {
        return device.address == scanResult.device.address
    }

    fun displayName(): String? {
        return when {
            name?.isNotEmpty() == true -> name
            device.name?.isNotEmpty() == true -> device.name
            else -> null
        }
    }

    fun macAddress(): MacAddress {
        return MacAddress(device.address.uppercase())
    }

    override fun hashCode(): Int {
        return device.address.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is DiscoveredBleDevice) {
            return device.address == other.device.address
        }
        return super.equals(other)
    }

    fun toBleDevice(): BleDevice {
        val bleDeviceType = device.uuids?.firstOrNull()?.toString()?.let { BleDeviceType.fromUuid(it) } ?: BleDeviceType.UNKNOWN
        return BleDevice(
            macAddress = macAddress(),
            name = name,
            rssi = rssi,
            previousRssi = previousRssi,
            highestRssi = highestRssi,
            bleDeviceType = bleDeviceType
        )
    }
}

fun ScanResult.toDiscoveredBluetoothDevice(name: String? = null): DiscoveredBleDevice {
    return DiscoveredBleDevice(
        device = device,
        scanResult = this,
        name = if (!name.isNullOrBlank()) name else if (scanRecord != null) scanRecord!!.deviceName else null,
        previousRssi = rssi,
        rssi = rssi,
        highestRssi = rssi
    )
}

enum class RssiLevel {
    WEAKEST, WEAK, MODERATE, STRONG, STRONGEST
}