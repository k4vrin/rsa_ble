package dev.kavrin.rsable.data.repository

import dev.kavrin.rsable.data.local.BleGattManager
import dev.kavrin.rsable.data.local.BleScanManager
import dev.kavrin.rsable.data.local.WriteType
import dev.kavrin.rsable.domain.model.BleDevice
import dev.kavrin.rsable.domain.model.BleScanResource
import dev.kavrin.rsable.domain.model.GattEvent
import dev.kavrin.rsable.domain.repository.BleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.map
import java.util.UUID


class BleRepositoryImpl(
    private val bleGattManager: BleGattManager,
    private val bleScanManager: BleScanManager,
) : BleRepository {

    override val result: Flow<BleScanResource<List<BleDevice>>>
        get() = bleScanManager.result.map { bleScanResource ->
            bleScanResource.flatMap {
                BleScanResource.Success(it.map { discoveredBleDevice -> discoveredBleDevice.toBleDevice() })
            }
        }

    override fun startScan() {
        TODO("Not yet implemented")
    }

    override fun stopScan() {
        TODO("Not yet implemented")
    }

    override val gattDataFlow: MutableSharedFlow<GattEvent>
        get() = TODO("Not yet implemented")

    override fun connectToDevice(discoveredBleDevice: BleDevice) {
        TODO("Not yet implemented")
    }

    override fun disconnectFromDevice() {
        TODO("Not yet implemented")
    }

    override fun readCharacteristic(characteristicUuid: UUID) {
        TODO("Not yet implemented")
    }

    override fun writeCharacteristic(
        characteristicUuid: UUID,
        value: ByteArray,
        writeType: WriteType,
    ) {
        TODO("Not yet implemented")
    }
}