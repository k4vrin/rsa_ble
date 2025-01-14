package dev.kavrin.rsable.data.local

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.util.Log
import dev.kavrin.rsable.data.dto.DiscoveredBleDevice
import dev.kavrin.rsable.data.dto.toDiscoveredBluetoothDevice
import dev.kavrin.rsable.data.mappers.mapScanError
import dev.kavrin.rsable.domain.model.BleScanError
import dev.kavrin.rsable.domain.model.BleScanResource
import dev.kavrin.rsable.domain.model.MacAddress
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.debounce

class BleScanManagerImpl(
    bleAdapter: BluetoothAdapter,
) : ScanCallback(), BleScanManager {

    private val scanner = bleAdapter.bluetoothLeScanner
    private val devices = mutableMapOf<MacAddress, DiscoveredBleDevice>()

    private val _result = MutableStateFlow<BleScanResource<List<DiscoveredBleDevice>>>(BleScanResource.createLoading())
    @OptIn(FlowPreview::class)
    override val result: Flow<BleScanResource<List<DiscoveredBleDevice>>> = _result.asSharedFlow().debounce(250)


    @SuppressLint("MissingPermission")
    override fun startScan() {

        Log.d(TAG, "startScan: Start Scan")

        val settings = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ScanSettings.Builder()
                // A balance between scan speed and power consumption.
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                // This line disables the use of legacy scanning. Legacy scanning is an older approach
                // to BLE scanning that is less efficient and has been superseded by newer methods.
                // By setting this to false, we ensure that the scan uses the modern, optimized approach.
                .setLegacy(false)
                // The report delay determines how often scan results are reported to the app.
                // A value of 0 indicates that results should be reported as soon as they are discovered.
                .setReportDelay(0)
                .build()
        } else {
            ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setReportDelay(0)
                .build()
        }

        // Permission is mandatory for entering the app and is granted upfront.
        scanner.startScan(
            /* filters = */ emptyList(),
            /* settings = */ settings,
            /* callback = */ this
        )

        _result.tryEmit(BleScanResource.createLoading())
    }

    @SuppressLint("MissingPermission")
    override fun stopScan() {
        runCatching {
            scanner.stopScan(this)
        }.getOrElse {
            Log.d(TAG, "stopScan Failed to stop scanning. error: ${it.localizedMessage}")
            _result.tryEmit(BleScanResource.createError(BleScanError.StopScanFailed))
        }
    }

    /**************ScanCallBack**************/

    private fun addDevice(scanResult: ScanResult) {
        val macAddress = MacAddress(scanResult.device.address.uppercase())
        val existingDevice = devices[macAddress]

        existingDevice?.let {
            if (!existingDevice.hasSameData(scanResult)) {
                devices[macAddress] = scanResult.toDiscoveredBluetoothDevice()
                _result.tryEmit(BleScanResource.createSuccess(devices.values.toList()))
            }
            return
        }

        devices[macAddress] = scanResult.toDiscoveredBluetoothDevice()
    }

    // In DiscoveredBleDevice (or a similar class):
    @SuppressLint("MissingPermission")
    private fun DiscoveredBleDevice.hasSameData(scanResult: ScanResult): Boolean {
        // Compare relevant data like RSSI, name, etc.
        return this.rssi == scanResult.rssi && this.name == scanResult.device.name
    }

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        result?.let {
            addDevice(scanResult = result)
            _result.tryEmit(BleScanResource.createSuccess(devices.values.toList()))
        }
    }

    override fun onScanFailed(errorCode: Int) {
        Log.d(TAG, "onScanFailed errorCode: $errorCode")
        _result.tryEmit(BleScanResource.createError(mapScanError(errorCode)))
    }

    companion object {
        private const val TAG = "BleScanManagerImpl"
    }

}
