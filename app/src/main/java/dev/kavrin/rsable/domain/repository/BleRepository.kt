package dev.kavrin.rsable.domain.repository

import dev.kavrin.rsable.data.local.WriteType
import dev.kavrin.rsable.domain.model.BleDevice
import dev.kavrin.rsable.domain.model.BleScanResource
import dev.kavrin.rsable.domain.model.GattEvent
import dev.kavrin.rsable.domain.model.Resource
import kotlinx.coroutines.flow.SharedFlow
import java.util.UUID

interface BleRepository {
    val result: SharedFlow<BleScanResource<List<BleDevice>>>
    fun startScan()
    fun stopScan()

    val gattEvents: SharedFlow<Resource<GattEvent, GattEvent.Error>>
    suspend fun connectToDevice(bleDevice: BleDevice): Resource<GattEvent.ConnectionState, GattEvent.Error>
    suspend fun disconnectFromDevice()
    suspend fun readCharacteristic(characteristicUuid: UUID): Resource<ByteArray, GattEvent.Error>
    suspend fun writeCharacteristic(characteristicUuid: UUID, value: ByteArray, writeType: WriteType): Resource<Unit, GattEvent.Error>
    suspend fun enableNotifyCharacteristic(characteristicUuid: UUID): Resource<GattEvent.NotifyCharacteristic, GattEvent.Error>
}