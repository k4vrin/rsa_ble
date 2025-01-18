package dev.kavrin.rsable.data.local

import dev.kavrin.rsable.data.dto.DiscoveredBleDevice
import dev.kavrin.rsable.domain.model.BleDevice
import dev.kavrin.rsable.domain.model.BleScanResource
import dev.kavrin.rsable.domain.model.MacAddress
import kotlinx.coroutines.flow.SharedFlow

interface BleScanManager {
    val devices: Map<MacAddress, DiscoveredBleDevice>
    val result: SharedFlow<BleScanResource<List<BleDevice>>>
    fun startScan()
    fun stopScan()
}