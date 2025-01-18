package dev.kavrin.rsable.data.repository

import dev.kavrin.rsable.data.local.BleGattManager
import dev.kavrin.rsable.data.local.BleScanManager
import dev.kavrin.rsable.data.local.WriteType
import dev.kavrin.rsable.domain.model.BleDevice
import dev.kavrin.rsable.domain.model.BleScanResource
import dev.kavrin.rsable.domain.model.GattEvent
import dev.kavrin.rsable.domain.model.Resource
import dev.kavrin.rsable.domain.repository.BleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID


class BleRepositoryImpl(
    private val bleGattManager: BleGattManager,
    private val bleScanManager: BleScanManager,
) : BleRepository {

    override val result: SharedFlow<BleScanResource<List<BleDevice>>> = bleScanManager.result

    override fun startScan() {
        bleScanManager.startScan()
    }

    override fun stopScan() {
        bleScanManager.stopScan()
    }

    override val gattEvents: SharedFlow<Resource<GattEvent, GattEvent.Error>> = bleGattManager.gattEvents

    override suspend fun connectToDevice(bleDevice: BleDevice): Resource<GattEvent.ConnectionState, GattEvent.Error> {
        val device = bleScanManager.devices[bleDevice.macAddress]
        device?.let {
            return bleGattManager.connectToDevice(device)
        }
        return Resource.error(GattEvent.Error.ConnectionLost("Device not found"))
    }

    override suspend fun disconnectFromDevice() {
        bleGattManager.disconnectFromDevice()
    }

    override suspend fun readCharacteristic(characteristicUuid: UUID): Resource<ByteArray, GattEvent.Error> {
        return bleGattManager.readCharacteristic(characteristicUuid)
    }

    override suspend fun writeCharacteristic(
        characteristicUuid: UUID,
        value: ByteArray,
        writeType: WriteType,
    ): Resource<Unit, GattEvent.Error> {
        return bleGattManager.writeCharacteristic(
            characteristicUuid = characteristicUuid,
            value = value,
            writeType = writeType
        )
    }

    override suspend fun enableNotifyCharacteristic(characteristicUuid: UUID): Resource<GattEvent.NotifyCharacteristic, GattEvent.Error> {
        return bleGattManager.enableNotifyCharacteristic(characteristicUuid)
    }
}