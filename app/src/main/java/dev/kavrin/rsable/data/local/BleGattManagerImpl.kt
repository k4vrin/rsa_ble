@file:OptIn(ExperimentalStdlibApi::class)

package dev.kavrin.rsable.data.local

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import androidx.activity.result.launch
import dev.kavrin.rsable.data.dto.DiscoveredBleDevice
import dev.kavrin.rsable.domain.model.GattCharacteristic
import dev.kavrin.rsable.domain.model.GattEvent
import dev.kavrin.rsable.domain.model.GattService
import dev.kavrin.rsable.domain.model.Logger
import dev.kavrin.rsable.domain.model.Resource
import dev.kavrin.rsable.util.safeLaunch
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

@SuppressLint("MissingPermission")
class BleGattManagerImpl(
    private val context: Context,
) : BleGattManager {

    private val _gattEvents: MutableSharedFlow<Resource<GattEvent, GattEvent.Error>> =
        MutableSharedFlow(
            replay = 0,
            extraBufferCapacity = 70,
            onBufferOverflow = BufferOverflow.SUSPEND
        )
    override val gattEvents: SharedFlow<Resource<GattEvent, GattEvent.Error>> =
        _gattEvents.asSharedFlow()

    private val operationQueue = Channel<BleOperation>(
        capacity = 20,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    private val bluetoothGattMutex = Mutex()
    private var bluetoothGatt: BluetoothGatt? = null
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineName("BleGattManagerScope")
    )

    init {
        scope.safeLaunch {
            processOperations()
        }

        scope.safeLaunch {
            _gattEvents.filter {
                it is Resource.Error || (it is Resource.Success && it.data is GattEvent.ConnectionState)
            }.collect {
                when (it) {
                    is Resource.Error -> Unit
                    is Resource.Success -> {
                        if (it.data is GattEvent.ConnectionState.Connected) {
                            runCatching {
                                bluetoothGatt?.requestMtu(517)
                            }.getOrNull()
                        }
                    }
                }
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Resource.runCatching {
                val event = when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> GattEvent.ConnectionState.Connected
                    BluetoothProfile.STATE_DISCONNECTED -> GattEvent.ConnectionState.Disconnected
                    BluetoothProfile.STATE_DISCONNECTING -> GattEvent.ConnectionState.Disconnecting
                    else -> GattEvent.ConnectionState.Connecting
                }
                logConnection(
                    level = Logger.Level.INFO,
                    status = "Success",
                    message = "Connection state changed to $newState"
                )
                scope.safeLaunch {
                    _gattEvents.emit(success(event))
                }
            }.getOrElse { cause ->
                logConnection(
                    level = Logger.Level.ERROR,
                    status = "Failed",
                    message = "Connection failed",
                    cause = cause
                )
                scope.safeLaunch {
                    _gattEvents.emit(
                        Resource.error(
                            GattEvent.Error.ConnectionLost(message = "Connection failed: $cause")
                        )
                    )
                }
            }
        }


        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Resource.runCatching {
                val event = if (status == BluetoothGatt.GATT_SUCCESS) {
                    GattEvent.ServiceDiscovered(gatt.services.map { it.toGattService() })
                } else {
                    GattEvent.Error.ServiceDiscoveryFailed("Service discovery failed: $status")
                }
                Logger.log(
                    level = Logger.Level.INFO,
                    module = "BleGattManager",
                    operation = "GattService",
                    status = "Success",
                    message = "GattService $event"
                )
                scope.safeLaunch {
                    _gattEvents.emit(success(event))
                }
            }.getOrElse { cause ->
                Logger.log(
                    level = Logger.Level.ERROR,
                    module = "BleGattManager",
                    operation = "GattService",
                    status = "Error: $status",
                    message = "Service discovery failed",
                    cause = cause
                )
                scope.safeLaunch {
                    _gattEvents.emit(
                        Resource.error(
                            GattEvent.Error.ServiceDiscoveryFailed("Service discovery failed: $cause")
                        )
                    )
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int,
        ) {
            Resource.runCatching {
                val event = if (status == BluetoothGatt.GATT_SUCCESS) {
                    GattEvent.ReadCharacteristic.Success(
                        characteristic.toGattCharacteristic(),
                        value
                    )
                } else {
                    GattEvent.ReadCharacteristic.Failure(
                        characteristic.toGattCharacteristic(),
                        "Characteristic read failed: $status"
                    )
                }
                Logger.log(
                    level = Logger.Level.INFO,
                    module = "BleGattManager",
                    operation = "GattCharacteristic",
                    status = "Success",
                    message = "GattCharacteristic $event"
                )
                scope.safeLaunch {
                    _gattEvents.emit(success(event))
                }
            }.getOrElse {
                Logger.log(
                    level = Logger.Level.ERROR,
                    module = "BleGattManager",
                    operation = "GattService",
                    status = "Error: $status",
                    message = "Service discovery failed",
                    cause = it
                )
                scope.safeLaunch {
                    _gattEvents.emit(
                        Resource.error(
                            GattEvent.Error.GattError(
                                status = status,
                                operation = "onCharacteristicRead",
                                message = "Characteristic read failed: $it"
                            )
                        )
                    )
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int,
        ) {
            Resource.runCatching {
                val event = if (status == BluetoothGatt.GATT_SUCCESS) {
                    GattEvent.WriteCharacteristic.Success(characteristic.toGattCharacteristic())
                } else {
                    GattEvent.WriteCharacteristic.Failure(
                        characteristic.toGattCharacteristic(),
                        "Characteristic write failed: $status"
                    )
                }
                Logger.log(
                    level = Logger.Level.INFO,
                    module = "BleGattManager",
                    operation = "GattCharacteristic",
                    status = "Success",
                    message = "GattCharacteristic $event"
                )
                scope.safeLaunch {
                    _gattEvents.emit(success(event))
                }
            }.getOrElse {
                Logger.log(
                    level = Logger.Level.ERROR,
                    module = "BleGattManager",
                    operation = "GattService",
                    status = "Error: $status",
                    message = "Service discovery failed",
                    cause = it
                )
                scope.safeLaunch {
                    _gattEvents.emit(
                        Resource.error(
                            GattEvent.Error.GattError(
                                status = status,
                                operation = "onCharacteristicWrite",
                                message = "Characteristic Write failed: $it"
                            )
                        )
                    )
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
        ) {
            Resource.runCatching {
                val event = GattEvent.CharacteristicNotification(
                    characteristic.toGattCharacteristic(),
                    value
                )
                Logger.log(
                    level = Logger.Level.INFO,
                    module = "BleGattManager",
                    operation = "GattCharacteristic",
                    status = "Success",
                    message = "GattCharacteristic $event"
                )
                scope.safeLaunch {
                    _gattEvents.emit(success(event))
                }
            }.getOrElse {
                Logger.log(
                    level = Logger.Level.ERROR,
                    module = "BleGattManager",
                    operation = "GattService",
                    status = "Error: $gatt",
                    message = "Characteristic change failed",
                    cause = it
                )
                scope.safeLaunch {
                    Resource.error(
                        GattEvent.Error.GattError(
                            status = -999,
                            operation = "onCharacteristicChanged",
                            message = "Characteristic change failed: $it"
                        )
                    )
                }
            }
        }
    }

    /*************BleGattManager*************/

    override suspend fun connectToDevice(discoveredBleDevice: DiscoveredBleDevice): Resource<GattEvent.ConnectionState, GattEvent.Error> {
        try {
            bluetoothGatt = discoveredBleDevice.device.connectGatt(context, false, gattCallback)

            if (bluetoothGatt == null) {
                logConnection(
                    level = Logger.Level.ERROR,
                    status = "Failed",
                    message = "Failed to connect to device ${discoveredBleDevice.device.name}"
                )
                return Resource.error(
                    GattEvent.Error.ConnectionLost(
                        message = "Failed to connect to device ${discoveredBleDevice.device.name}"
                    )
                )
            }

            logConnection(
                level = Logger.Level.INFO,
                status = "Info",
                message = "Connected to device ${discoveredBleDevice.device.name}"
            )

            return handleConnectionGattEvents()
                .also {
                    when (it) {
                        is Resource.Error -> {
                            logConnection(
                                level = Logger.Level.ERROR,
                                status = "Failed",
                                message = "Exception during connection: ${it.cause?.message}",
                            )
                        }

                        is Resource.Success -> {
                            logConnection(
                                level = Logger.Level.INFO,
                                status = "Info",
                                message = "Connection result: ${it.data}"
                            )
                        }
                    }
                }

        } catch (e: Exception) {
            coroutineContext.ensureActive()
            logConnection(
                level = Logger.Level.ERROR,
                status = "Failed",
                message = "Exception during connection: ${e.message}",
                cause = e
            )
            return Resource.error(
                GattEvent.Error.ConnectionLost(message = "Exception during connection: ${e.message}")
            )
        }
    }

    private suspend fun handleConnectionGattEvents(): Resource<GattEvent.ConnectionState, GattEvent.Error> {
        return withTimeoutOrNull(5.seconds) { // Timeout after 5 seconds
            _gattEvents.firstOrNull { event ->
                event is Resource.Error || (event is Resource.Success && event.data is GattEvent.ConnectionState)
            }?.let { res ->
                when (res) {
                    is Resource.Error -> res
                    is Resource.Success -> Resource.success(res.data as GattEvent.ConnectionState)
                }
            }
        } ?: Resource.error(
            GattEvent.Error.TimeoutError(
                operation = "Connection",
                message = "Connection timeout"
            )
        ) // Handle timeout
    }

    private fun logConnection(
        level: Logger.Level,
        status: String,
        message: String,
        cause: Throwable? = null,
    ) {
        Logger.log(
            level = level,
            module = "BleGattManager",
            operation = "Connection",
            status = status,
            message = message
        )
    }


    override suspend fun disconnectFromDevice() {
        try {
            bluetoothGatt?.disconnect()
        } finally {
            bluetoothGatt?.close()
            bluetoothGatt = null
        }
        operationQueue.cancel()
        scope.cancel()
    }

    override suspend fun readCharacteristic(characteristicUuid: UUID): Resource<ByteArray, GattEvent.Error> {
        operationQueue.send(BleOperation.Read(characteristicUuid))
        logRead(
            characteristicUuid,
            Logger.Level.INFO,
            "RequestSent",
            "Characteristic read request $characteristicUuid send"
        )
        return handleReadGattEvent(characteristicUuid)
            .also {
                when (it) {
                    is Resource.Error -> {
                        logRead(
                            characteristicUuid,
                            Logger.Level.ERROR,
                            "ReadFailed",
                            "Characteristic read failed: ${it.cause?.message}",
                        )
                    }

                    is Resource.Success -> {
                        logRead(
                            characteristicUuid,
                            Logger.Level.INFO,
                            "ResponseReceived",
                            "Characteristic read response ${it.data.toHexString()}"
                        )
                    }
                }
            }
    }

    private suspend fun handleReadGattEvent(
        characteristicUuid: UUID,
    ): Resource<ByteArray, GattEvent.Error> {
        return withTimeoutOrNull(5.seconds) {
            _gattEvents.firstOrNull {
                it is Resource.Success && it.data is GattEvent.ReadCharacteristic && it.data.gattCharacteristic.uuid == characteristicUuid
            }?.let { res ->
                when (res) {
                    is Resource.Error -> res
                    is Resource.Success -> {
                        (res.data as GattEvent.ReadCharacteristic).let {
                            when (it) {
                                is GattEvent.ReadCharacteristic.Failure -> Resource.error(
                                    GattEvent.Error.GattError(
                                        status = -999,
                                        operation = "onCharacteristicRead",
                                        message = "Characteristic read failed: $it"
                                    )
                                )

                                is GattEvent.ReadCharacteristic.Success -> Resource.success(it.value)
                            }
                        }
                    }
                }
            }
        } ?: Resource.error(
            GattEvent.Error.TimeoutError(
                operation = "Read",
                uuid = characteristicUuid,
                message = "Read timeout"
            )
        )
    }

    private fun logRead(
        uuid: UUID?,
        level: Logger.Level,
        status: String,
        message: String,
        cause: Throwable? = null,
    ) {
        Logger.log(
            level = level,
            module = "BleGattManager",
            operation = "Read",
            uuid = uuid,
            status = status,
            message = message
        )
    }

    override suspend fun writeCharacteristic(
        characteristicUuid: UUID,
        value: ByteArray,
        writeType: WriteType,
    ): Resource<Unit, GattEvent.Error> {
        operationQueue.send(BleOperation.Write(characteristicUuid, value, writeType))
        logWrite(
            characteristicUuid,
            Logger.Level.INFO,
            "RequestSent",
            "Characteristic write request $characteristicUuid send"
        )

        return handleWriteGattEvent(characteristicUuid)
            .also {
                when (it) {
                    is Resource.Error -> {
                        logWrite(
                            characteristicUuid,
                            Logger.Level.ERROR,
                            "WriteFailed",
                            "Characteristic write failed: ${it.cause?.message}",
                        )
                    }

                    is Resource.Success -> {
                        logWrite(
                            characteristicUuid,
                            Logger.Level.INFO,
                            "WriteDone",
                            "Characteristic write done."
                        )
                    }
                }
            }

    }

    private suspend fun handleWriteGattEvent(
        characteristicUuid: UUID,
    ): Resource<Unit, GattEvent.Error> {
        return withTimeoutOrNull(5.seconds) {
            _gattEvents.firstOrNull {
                it is Resource.Success && it.data is GattEvent.WriteCharacteristic && it.data.gattCharacteristic.uuid == characteristicUuid
            }?.let { res ->
                when (res) {
                    is Resource.Error -> res
                    is Resource.Success -> {
                        (res.data as GattEvent.WriteCharacteristic)
                            .let { value ->
                                when (value) {
                                    is GattEvent.WriteCharacteristic.Failure -> Resource.error(
                                        GattEvent.Error.GattError(
                                            status = -999,
                                            operation = "onCharacteristicWrite",
                                            message = "Characteristic write failed: $value"
                                        )
                                    )

                                    is GattEvent.WriteCharacteristic.Success -> Resource.success(
                                        Unit
                                    )
                                }
                            }
                    }
                }
            }
        } ?: Resource.error(
            GattEvent.Error.TimeoutError(
                operation = "Write",
                uuid = characteristicUuid,
                message = "Write timeout"
            )
        )
    }

    private fun logWrite(
        uuid: UUID?,
        level: Logger.Level,
        status: String,
        message: String,
        cause: Throwable? = null,
    ) {
        Logger.log(
            level = level,
            module = "BleGattManager",
            operation = "Write",
            uuid = uuid,
            status = status,
            message = message
        )
    }


    private suspend fun processOperations() {

        try {
            for (operation in operationQueue) {
                if (!coroutineContext.isActive) {
                    break // Exit if the scope is canceled
                }
                bluetoothGattMutex.withLock {
                    when (operation) {
                        is BleOperation.Read -> executeRead(operation.uuid)
                        is BleOperation.Write -> executeWrite(
                            operation.uuid,
                            operation.value,
                            operation.writeType
                        )
                    }
                }
            }
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            _gattEvents.emit(
                Resource.error(
                    GattEvent.Error.GattError(
                        status = -999,
                        operation = "processOperations",
                        message = "Exception during operation: ${e.message}"
                    )
                )
            )
            Logger.log(
                level = Logger.Level.ERROR,
                module = "BleGattManager",
                operation = "Operation",
                status = "Error",
                message = "Exception during operation: ${e.message}",
            )
        }
    }

    private suspend fun executeRead(uuid: UUID) {
        bluetoothGatt?.let { gatt ->
            val characteristic = findCharacteristic(uuid)
            if (characteristic != null) {
                gatt.readCharacteristic(characteristic)
            } else {
                _gattEvents.emit(
                    Resource.success(
                        GattEvent.ReadCharacteristic.Failure(
                            gattCharacteristic = GattCharacteristic(uuid),
                            reason = "executeRead for $uuid failed"
                        )
                    )
                )
            }
        }
    }

    private suspend fun executeWrite(uuid: UUID, value: ByteArray, writeType: WriteType) {
        val writeTypeInt = when (writeType) {
            WriteType.WITH_RESPONSE -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            WriteType.WITHOUT_RESPONSE -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        }
        bluetoothGatt?.let { gatt ->
            val characteristic = findCharacteristic(uuid)
            if (characteristic != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeCharacteristic(characteristic, value, writeTypeInt)
                } else {
                    characteristic.value = value
                    characteristic.writeType = writeTypeInt
                    gatt.writeCharacteristic(characteristic)
                }
            } else {
                _gattEvents.emit(
                    Resource.success(
                        GattEvent.WriteCharacteristic.Failure(
                            gattCharacteristic = GattCharacteristic(uuid),
                            reason = "executeWrite for $uuid failed"
                        )
                    )
                )
            }
        }
    }

    private fun findCharacteristic(uuid: UUID): BluetoothGattCharacteristic? {
        return bluetoothGatt?.services?.flatMap { it.characteristics }?.find { it.uuid == uuid }
    }

    sealed class BleOperation {
        data class Read(val uuid: UUID) : BleOperation()
        data class Write(val uuid: UUID, val value: ByteArray, val writeType: WriteType) :
            BleOperation() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Write

                if (uuid != other.uuid) return false
                if (!value.contentEquals(other.value)) return false
                if (writeType != other.writeType) return false

                return true
            }

            override fun hashCode(): Int {
                var result = uuid.hashCode()
                result = 31 * result + value.contentHashCode()
                result = 31 * result + writeType.hashCode()
                return result
            }
        }
    }

    companion object {
        private const val TAG = "BleGattManagerImpl"

        fun BluetoothGattCharacteristic.toGattCharacteristic(): GattCharacteristic {
            return GattCharacteristic(
                uuid = this.uuid
            )
        }

        fun BluetoothGattService.toGattService(): GattService {
            return GattService(
                uuid = this.uuid,
                gattCharacteristics = this.characteristics.map { it.toGattCharacteristic() }
            )
        }
    }
}
