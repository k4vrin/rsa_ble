package dev.kavrin.rsable.data.dto

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Parcelable
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

    fun hasRssiLevelChanged(): Boolean {
        return getRssiLevel(rssi) != getRssiLevel(previousRssi)
    }

    /**
     * Calculates the RSSI (Received Signal Strength Indicator) level based on the provided RSSI value.
     *
     * This function maps the RSSI value to a level ranging from 0 to 4, where:
     * - 0 represents the weakest signal (RSSI in the range of Int.MIN_VALUE to 10).
     * - 1 represents a weak signal (RSSI in the range of 11 to 28).
     * - 2 represents a moderate signal (RSSI in the range of 29 to 45).
     * - 3 represents a strong signal (RSSI in the range of 46 to 65).
     * - 4 represents the strongest signal or an out-of-range/invalid RSSI(RSSI in the range greater than 65).
     *
     * Note: The provided RSSI values are assumed to be already processed and not raw dBm values.
     *       The specific thresholds (10, 28, 45, 65) are arbitrary and can be adjusted based on the
     *       specific use case or hardware requirements.
     *
     * @param rssi The RSSI value (integer) to determine the level for.
     * @return An integer representing the RSSI level (0 to 4).
     */
    private fun getRssiLevel(rssi: Int): Int {
        return when (rssi) {
            in Int.MIN_VALUE..10 -> 0
            in 11..28 -> 1
            in 29..45 -> 2
            in 46..65 -> 3
            else -> 4
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
