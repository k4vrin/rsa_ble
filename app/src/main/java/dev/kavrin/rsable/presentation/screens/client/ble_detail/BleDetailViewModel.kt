@file:OptIn(ExperimentalStdlibApi::class)

package dev.kavrin.rsable.presentation.screens.client.ble_detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kavrin.rsable.data.local.WriteType
import dev.kavrin.rsable.domain.model.BleDeviceType
import dev.kavrin.rsable.domain.model.GattEvent
import dev.kavrin.rsable.domain.model.Resource
import dev.kavrin.rsable.domain.repository.BleRepository
import dev.kavrin.rsable.util.safeLaunch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class BleDetailViewModel(
    private val bleRepository: BleRepository,
) : ViewModel(), BleDetailContract {

    private val _state = MutableStateFlow(BleDetailContract.State())
    override val state: StateFlow<BleDetailContract.State> = _state.asStateFlow()

    private val _effect = Channel<BleDetailContract.Effect>()
    override val effect: Flow<BleDetailContract.Effect> = _effect.receiveAsFlow()

    init {
        viewModelScope.safeLaunch {
            bleRepository.gattEvents
                .collect { event ->
                    when (event) {
                        is Resource.Error -> {
                            Log.d(TAG, "onEvent Error: ${event.cause}")
                            _state.update { currState ->
                                currState.copy(
                                    errors = currState.errors + event.cause?.message
                                )
                            }
                            delay(2.seconds)
                            _effect.send(
                                BleDetailContract.Effect.NavigateBack
                            )
                        }
                        is Resource.Success -> {
                            Log.d(TAG, "onEvent Success: ${event.data}")
                            when (event.data) {
                                GattEvent.ConnectionState.Connected -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            isReconnecting = false,
                                            errors = currState.errors + "Device Connected."
                                        )
                                    }
                                }
                                GattEvent.ConnectionState.Connecting -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "Device Connecting."
                                        )
                                    }
                                }
                                GattEvent.ConnectionState.Reconnecting -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            isReconnecting = true,
//                                            notifValues = emptyList()
                                        )
                                    }

                                }
                                GattEvent.ConnectionState.Disconnected -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            isLoading = false,
                                            errors = currState.errors + "Device Disconnected"
                                        )
                                    }
                                    _effect.send(BleDetailContract.Effect.NavigateBack)
                                }
                                GattEvent.ConnectionState.Disconnecting -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "Device Disconnecting."
                                        )
                                    }
                                }
                                is GattEvent.ConnectionState.Failed -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "Device connection failed: ${event.data.reason}"
                                        )
                                    }
                                }
                                is GattEvent.Error.ConnectionLost -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "Device connection lost: ${event.data.message}",
                                            isReconnecting = false
                                        )
                                    }
                                    delay(2.seconds)
                                    _effect.send(
                                        BleDetailContract.Effect.NavigateBack
                                    )
                                }
                                is GattEvent.Error.DescriptorNotFound -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "Descriptor not found: ${event.data.message}"
                                        )
                                    }
                                }
                                is GattEvent.Error.GattError -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "GATT error: ${event.data.message}"
                                        )
                                    }
                                }
                                is GattEvent.Error.InsufficientPermissions -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "Insufficient permissions: ${event.data.message}"
                                        )
                                    }
                                }
                                is GattEvent.Error.ServiceDiscoveryFailed -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "Service discovery failed: ${event.data.message}"
                                        )
                                    }
                                }
                                is GattEvent.Error.TimeoutError -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "Timeout error: ${event.data.message}"
                                        )
                                    }
                                }
                                is GattEvent.Error.UnknownError -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "Unknown error: ${event.data.message}"
                                        )
                                    }
                                }
                                is GattEvent.MtuChanged -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "MTU changed: ${event.data.mtu}"
                                        )
                                    }
                                }
                                is GattEvent.NotifyCharacteristic.Changed -> {
                                    Log.d(TAG, "NotifyCharacteristic.Changed: ${event.data.value.toHexString()}")
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "Notify characteristic changed: ${event.data.value.toHexString()}",
                                            notifValues = currState.notifValues + event.data.value.map { hexValue ->
                                                hexValue.toInt().toFloat()
                                            }

                                        )
                                    }
                                }
                                is GattEvent.NotifyCharacteristic.Failure -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "Notify characteristic failure: ${event.data.reason}"
                                        )
                                    }
                                }
                                is GattEvent.NotifyCharacteristic.Success -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "Notify characteristic success"
                                        )
                                    }
                                }
                                is GattEvent.ReadCharacteristic.Failure -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "Read characteristic failure: ${event.data.reason}"
                                        )
                                    }
                                }
                                is GattEvent.ReadCharacteristic.Success -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "Read characteristic success: ${event.data.value.toHexString()}"
                                        )
                                    }
                                }
                                is GattEvent.ServiceDiscovered -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "Service discovered"
                                        )
                                    }
                                }
                                is GattEvent.WriteCharacteristic.Failure -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "Write characteristic failure: ${event.data.reason}"
                                        )
                                    }
                                }
                                is GattEvent.WriteCharacteristic.Success -> {
                                    _state.update { currState ->
                                        currState.copy(
                                            errors = currState.errors + "Write characteristic success"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    override fun onEvent(event: BleDetailContract.Event) {
        when (event) {
            is BleDetailContract.Event.OnNavigateBack -> {
                viewModelScope.safeLaunch {
                    bleRepository.disconnectFromDevice()
                    _effect.send(
                        BleDetailContract.Effect.NavigateBack
                    )
                }
            }

            is BleDetailContract.Event.OnSelectedBleChange -> {
                Log.d(TAG, "OnSelectedBleChange: ${event.bleDevice}")
                _state.update { currState ->
                    currState.copy(
                        bleDevice = event.bleDevice?.copy(
                            services = event.bleDevice.services.filter { it.uuid.lowercase() == BleDeviceType.HEART_RATE_MONITOR.uuid.lowercase() }
                        )
                    )
                }

            }

            is BleDetailContract.Event.OnNotifyClick -> {
                viewModelScope.safeLaunch {
                    bleRepository.enableNotifyCharacteristic(UUID.fromString(event.uuid))
                        .let {
                            when (it) {
                                is Resource.Error -> {
                                    Log.d(
                                        TAG,
                                        "onEvent enableNotifyCharacteristic Error: ${it.cause}"
                                    )
                                }

                                is Resource.Success -> {
                                    Log.d(
                                        TAG,
                                        "onEvent enableNotifyCharacteristic Success: ${it.data}"
                                    )
                                }
                            }
                        }
                }
            }

            is BleDetailContract.Event.OnReadClick -> {
                viewModelScope.safeLaunch {
                    bleRepository.readCharacteristic(UUID.fromString(event.uuid))
                        .let {
                            when (it) {
                                is Resource.Error -> {
                                    Log.d(TAG, "onEvent OnReadClick Error: ${it.cause}")
                                }

                                is Resource.Success -> {
                                    Log.d(
                                        TAG,
                                        "onEvent OnReadClick Success: ${it.data.toHexString()}"
                                    )
                                    _state.update { currState ->
                                        currState.copy(
                                            readValue = it.data.toHexString()
                                        )
                                    }

                                }
                            }
                        }
                }
            }

            is BleDetailContract.Event.OnWriteClick -> {
                viewModelScope.safeLaunch {
                    bleRepository.writeCharacteristic(
                        characteristicUuid = UUID.fromString(event.uuid),
                        value = state.value.writeValue.toByteArray(Charsets.UTF_8),
                        writeType = WriteType.WITH_RESPONSE
                    ).let {
                        when (it) {
                            is Resource.Error -> {
                                Log.d(TAG, "onEvent OnWriteClick Error: ${it.cause}")
                            }

                            is Resource.Success -> {
                                Log.d(TAG, "onEvent OnWriteClick Success")

                            }
                        }
                    }
                }
            }

            is BleDetailContract.Event.OnWriteValueChange -> {
                _state.update { currState ->
                    currState.copy(
                        writeValue = event.value
                    )
                }

            }

            BleDetailContract.Event.OnClearErrors -> {
                _state.update { currState ->
                    currState.copy(
                        errors = emptyList()
                    )
                }

            }
        }
    }

    companion object {
        private const val TAG = "BleDetailViewModel"

        enum class HeartRateChar(val uuid: String, val operationType: CharacteristicProperty) {
            HEART_RATE_MEASUREMENT_UUID(
                "00002a37-0000-1000-8000-00805f9b34fb",
                CharacteristicProperty.NOTIFIABLE
            ),
            BODY_SENSOR_LOCATION_UUID(
                "00002a38-0000-1000-8000-00805f9b34fb",
                CharacteristicProperty.READABLE
            ),
            HEART_RATE_CONTROL_POINT_UUID(
                "00002a39-0000-1000-8000-00805f9b34fb",
                CharacteristicProperty.WRITABLE
            )
        }

        enum class CharacteristicProperty {
            READABLE,
            WRITABLE,
            NOTIFIABLE,
            UNKNOWN
        }


        fun parseUuidToShortForm(fullUuid: String?): String {
            if (fullUuid.isNullOrBlank()) return "N/A"
            val regex = Regex("^0000([0-9a-fA-F]{4})-0000-1000-8000-00805f9b34fb$")
            val matchResult = regex.matchEntire(fullUuid)
            return if (matchResult != null) {
                "0x${matchResult.groupValues[1].uppercase()}"
            } else {
                "Invalid UUID"
            }
        }
    }
}