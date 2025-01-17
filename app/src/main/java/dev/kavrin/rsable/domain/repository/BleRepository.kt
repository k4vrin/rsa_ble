package dev.kavrin.rsable.domain.repository

import dev.kavrin.rsable.data.local.WriteType
import dev.kavrin.rsable.domain.model.BleDevice
import dev.kavrin.rsable.domain.model.BleScanResource
import dev.kavrin.rsable.domain.model.GattEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.UUID

interface BleRepository {
    val result: Flow<BleScanResource<List<BleDevice>>>
    fun startScan()
    fun stopScan()

    val gattDataFlow: MutableSharedFlow<GattEvent>

    fun connectToDevice(discoveredBleDevice: BleDevice)
    fun disconnectFromDevice()
    fun readCharacteristic(characteristicUuid: UUID)
    fun writeCharacteristic(characteristicUuid: UUID, value: ByteArray, writeType: WriteType)
}