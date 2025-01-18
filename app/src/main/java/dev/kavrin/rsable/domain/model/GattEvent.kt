package dev.kavrin.rsable.domain.model

import java.util.UUID

sealed interface GattEvent {
    // Connection events
    sealed class ConnectionState : GattEvent {
        data object Connecting : ConnectionState()
        data object Reconnecting : ConnectionState()
        data object Disconnecting : ConnectionState()
        data object Connected : ConnectionState()
        data object Disconnected : ConnectionState()
        data class Failed(val reason: String, val cause: Throwable? = null) : ConnectionState()
    }
    data class MtuChanged(val mtu: Int) : GattEvent
    data class ServiceDiscovered(val services: List<GattService>) : GattEvent

    // Characteristic events


    sealed interface NotifyCharacteristic : GattEvent {
        val gattCharacteristic: GattCharacteristic

        data class Success(
            override val gattCharacteristic: GattCharacteristic,
        ) : NotifyCharacteristic

        data class Changed(
            override val gattCharacteristic: GattCharacteristic,
            val value: ByteArray
        ) : NotifyCharacteristic

        data class Failure(
            override val gattCharacteristic: GattCharacteristic,
            val reason: String,
        ) : NotifyCharacteristic
    }

    sealed interface ReadCharacteristic : GattEvent {
        val gattCharacteristic: GattCharacteristic

        data class Success(
            override val gattCharacteristic: GattCharacteristic,
            val value: ByteArray,
        ) : ReadCharacteristic

        data class Failure(
            override val gattCharacteristic: GattCharacteristic,
            val reason: String,
        ) : ReadCharacteristic
    }

    sealed interface WriteCharacteristic : GattEvent {
        val gattCharacteristic: GattCharacteristic

        data class Success(override val gattCharacteristic: GattCharacteristic) :
            WriteCharacteristic

        data class Failure(
            override val gattCharacteristic: GattCharacteristic,
            val reason: String,
        ) : WriteCharacteristic
    }

    // Known BLE GATT Errors
    sealed interface Error : GattEvent {
        val message: String?

        data class GattError(
            val status: Int,
            val operation: String,
            val uuid: UUID? = null,
            override val message: String? = null,
        ) : Error

        data class TimeoutError(
            val operation: String, val uuid: UUID? = null,
            override val message: String?,
        ) : Error

        data class ConnectionLost(override val message: String? = null) : Error
        data class ServiceDiscoveryFailed(override val message: String?) : Error
        data class DescriptorNotFound(val uuid: UUID, override val message: String?) : Error
        data class InsufficientPermissions(val operation: String, override val message: String?) : Error
        data class UnknownError(override val message: String?, val cause: Throwable? = null) : Error
    }
}

data class GattService(val uuid: String, val gattCharacteristics: List<GattCharacteristic>)
data class GattCharacteristic(val uuid: String)

enum class ConnectionFailureReason {
    TIMEOUT,
    UNKNOWN_DEVICE,
    // ... other reasons ...
}