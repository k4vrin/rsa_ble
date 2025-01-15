package dev.kavrin.rsable.data.dto

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Parcelable
import android.util.Log
import dev.kavrin.rsable.data.util.ScanRecordParser
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

    @OptIn(ExperimentalStdlibApi::class)
    fun toBleDevice(): BleDevice {
        val scanRecord = scanResult?.scanRecord
        val uuidString = scanRecord?.serviceUuids
            ?.firstOrNull {
                val uuidString = it.toString()
                BleDeviceType.fromUuid(uuidString) != BleDeviceType.UNKNOWN
            }?.toString()
        Log.d(TAG, "toBleDevice. Advertisement data: ${getManuAdvData(scanRecord?.bytes)}")
        val bleDeviceType = uuidString?.let { BleDeviceType.fromUuid(it) } ?: BleDeviceType.UNKNOWN

        val deviceName = scanRecord?.deviceName?.takeIf { it.isNotBlank() }
            ?: device.name?.takeIf { it.isNotBlank() }
            ?: macAddress().value

        return BleDevice(
            macAddress = macAddress(),
            name = deviceName,
            rssi = rssi,
            previousRssi = previousRssi,
            highestRssi = highestRssi,
            bleDeviceType = bleDeviceType
        )
    }

    companion object {
        private const val TAG = "DiscoveredBleDevice"

        private fun getManuAdvData(bytes: ByteArray?): String? {
            val builder = StringBuilder()
            val strManu = ScanRecordParser.getAdvertisements(bytes).find {
                it?.contains("Manufacturer Specific Data", ignoreCase = true) == true
            }

            if (strManu.isNullOrBlank() || !strManu.contains(
                    "Wesko Name",
                    ignoreCase = true
                )
            ) return null
            val firstTwoBytes = strManu.substringBefore("<br/>")
                .replace(oldValue = "Manufacturer Specific Data&&Company Code: 0x", "")
            builder.append(firstTwoBytes.substring(2))
            builder.append(firstTwoBytes.substring(0, 2))
            val dataBytesStr = strManu.substring(
                startIndex = strManu.indexOf("<br/>") + 5,
                endIndex = strManu.lastIndexOf("<br/>")
            )
                .replace("Data: 0x", "")
            builder.append(dataBytesStr)

            return builder.toString()
        }
    }
}

fun ScanResult.toDiscoveredBluetoothDevice(name: String? = null): DiscoveredBleDevice {
    return DiscoveredBleDevice(
        device = device,
        scanResult = this,
        name = if (scanRecord?.deviceName.isNullOrBlank()) scanRecord?.deviceName else if (scanRecord != null) name else null,
        previousRssi = rssi,
        rssi = rssi,
        highestRssi = rssi
    )
}

enum class RssiLevel {
    WEAKEST, WEAK, MODERATE, STRONG, STRONGEST
}