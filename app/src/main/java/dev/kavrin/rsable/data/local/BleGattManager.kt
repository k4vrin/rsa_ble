package dev.kavrin.rsable.data.local

import dev.kavrin.rsable.data.dto.DiscoveredBleDevice
import dev.kavrin.rsable.domain.model.GattEvent
import dev.kavrin.rsable.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import java.util.UUID

interface BleGattManager {
    val gattEvents: SharedFlow<Resource<GattEvent, GattEvent.Error>>

    suspend fun connectToDevice(discoveredBleDevice: DiscoveredBleDevice): Resource<GattEvent.ConnectionState, GattEvent.Error>
    suspend fun disconnectFromDevice()
    suspend fun readCharacteristic(characteristicUuid: UUID): Resource<ByteArray, GattEvent.Error>
    suspend fun writeCharacteristic(characteristicUuid: UUID, value: ByteArray, writeType: WriteType): Resource<Unit, GattEvent.Error>
    suspend fun enableNotifyCharacteristic(characteristicUuid: UUID): Flow<Resource<GattEvent.NotifyCharacteristic, GattEvent.Error>>

}
