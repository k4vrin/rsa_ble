package dev.kavrin.rsable.data.local

import dev.kavrin.rsable.data.dto.DiscoveredBleDevice
import dev.kavrin.rsable.domain.model.BleScanResource
import kotlinx.coroutines.flow.Flow

interface BleScanManager {
    val result: Flow<BleScanResource<List<DiscoveredBleDevice>>>
    fun startScan()
    fun stopScan()
}