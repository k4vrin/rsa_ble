@file:OptIn(ExperimentalStdlibApi::class)

package dev.kavrin.rsable.presentation.screens.client.ble_detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kavrin.rsable.data.local.WriteType
import dev.kavrin.rsable.domain.model.BleDeviceType
import dev.kavrin.rsable.domain.model.GattEvent
import dev.kavrin.rsable.domain.model.Resource
import dev.kavrin.rsable.domain.repository.BleRepository
import dev.kavrin.rsable.util.safeLaunch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class BleDetailViewModel(
    private val bleRepository: BleRepository
) : ViewModel(), BleDetailContract {

    private val _state = MutableStateFlow(BleDetailContract.State())
    override val state: StateFlow<BleDetailContract.State> = _state.asStateFlow()

    private val _effect = Channel<BleDetailContract.Effect>()
    override val effect: Flow<BleDetailContract.Effect> = _effect.receiveAsFlow()


    override fun onEvent(event: BleDetailContract.Event) {
        when (event) {
            is BleDetailContract.Event.OnNavigateBack -> TODO()
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
                        .collect {
                            when (it) {
                                is Resource.Error -> {
                                    Log.d(TAG, "onEvent enableNotifyCharacteristic Error: ${it.cause}")
                                }
                                is Resource.Success -> {
                                    Log.d(TAG, "onEvent enableNotifyCharacteristic Success: ${it.data}")
                                    when (it.data) {
                                        is GattEvent.NotifyCharacteristic.Changed -> {
                                            _state.update { currState ->
                                                currState.copy(notifValue = it.data.value.toHexString())
                                            }

                                        }
                                        is GattEvent.NotifyCharacteristic.Failure -> {

                                        }
                                        is GattEvent.NotifyCharacteristic.Success -> {

                                        }
                                    }
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
                                    Log.d(TAG, "onEvent OnReadClick Success: ${it.data.toHexString()}")
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
        }
    }

    companion object {
        private const val TAG = "BleDetailViewModel"

        enum class HeartRateChar(val uuid: String, val operationType: CharacteristicProperty) {
            HEART_RATE_MEASUREMENT_UUID("00002a37-0000-1000-8000-00805f9b34fb", CharacteristicProperty.NOTIFIABLE),
            BODY_SENSOR_LOCATION_UUID("00002a38-0000-1000-8000-00805f9b34fb", CharacteristicProperty.READABLE),
            HEART_RATE_CONTROL_POINT_UUID("00002a39-0000-1000-8000-00805f9b34fb", CharacteristicProperty.WRITABLE)
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