package dev.kavrin.rsable.presentation.screens.client.ble_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kavrin.rsable.domain.model.BleScanResource
import dev.kavrin.rsable.domain.model.GattEvent
import dev.kavrin.rsable.domain.model.Resource
import dev.kavrin.rsable.domain.repository.BleRepository
import dev.kavrin.rsable.util.safeLaunch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

class BleListViewModel(
    private val bleRepository: BleRepository,
) : ViewModel(), BleListContract {

    private val _state = MutableStateFlow(BleListContract.State())
    override val state: StateFlow<BleListContract.State> = _state.asStateFlow()

    private val effectChannel = Channel<BleListContract.Effect>()
    override val effect: Flow<BleListContract.Effect> = effectChannel.receiveAsFlow()

    override fun onEvent(event: BleListContract.Event) {
        when (event) {
            BleListContract.Event.OnStartScan -> {
                viewModelScope.safeLaunch {
                    effectChannel.send(BleListContract.Effect.StartScan)
                }
                _state.update { currState ->
                    currState.copy(isScanning = true)
                }

                observerBleDevices()
            }

            BleListContract.Event.OnStopScan -> {
                Log.d(TAG, "onEvent: stop scan")
                _state.update { currState ->
                    currState.copy(isScanning = false)
                }
                viewModelScope.safeLaunch {
                    effectChannel.send(BleListContract.Effect.StopScan)
                }
            }

            is BleListContract.Event.OnDeviceClicked -> {


                viewModelScope.safeLaunch {
                    _state.update { currState ->
                        currState.copy(isLoading = true)
                    }
                    bleRepository.connectToDevice(event.bleDevice)

                    Log.d(TAG, "start connect to device..")
                    withTimeout(10.seconds) {
                        bleRepository.gattEvents
                            .firstOrNull {
                                it is Resource.Error || (it is Resource.Success && (it.data is GattEvent.ServiceDiscovered))
                            }?.let { res ->
                                Log.d(TAG, "connect res: $res")
                                when (res) {
                                    is Resource.Error -> {
                                        _state.update { currState ->
                                            currState.copy(
                                                isLoading = false,
                                                errors = currState.errors + res.cause?.message
                                            )
                                        }
                                    }

                                    is Resource.Success -> {
                                        (res.data as GattEvent.ServiceDiscovered).let {
                                            effectChannel.send(
                                                BleListContract.Effect.NavigateToDetail(
                                                    bleDevice = event.bleDevice,
                                                    gattServices = it.services
                                                )
                                            )

                                            _state.update { currState ->
                                                currState.copy(isLoading = false)
                                            }
                                        }
                                    }
                                }
                            }
                    }
                }.invokeOnCompletion {
                    if (it != null) {
                        _state.update { currState ->
                            currState.copy(
                                isLoading = false,
                                errors = currState.errors + it.message
                            )
                        }
                    }
                    _state.update { currState ->
                        currState.copy(isLoading = false)
                    }
                }
            }

            BleListContract.Event.OnClearErrors -> {
                Log.d(TAG, "onEvent clear errors")
                _state.update { currState ->
                    currState.copy(
                        errors = emptyList()
                    )
                }

            }

            is BleListContract.Event.OnBluetoothStatusChange -> {
                if (!event.isEnable) {
                    _state.update { currState ->
                        currState.copy(
                            isScanning = false,
                            errors = currState.errors + "Bluetooth is disabled."
                        )
                    }
                }

            }
            is BleListContract.Event.OnLocationStatusChange -> {
                if (!event.isEnable) {
                    _state.update { currState ->
                        currState.copy(
                            isScanning = false,
                            errors = currState.errors + "Location is disabled."
                        )
                    }
                }
            }

            BleListContract.Event.OnNavigateBack -> {
                viewModelScope.safeLaunch {
                    effectChannel.send(BleListContract.Effect.NavigateBack)
                }
            }
        }
    }

    private fun observerBleDevices() {
        viewModelScope.safeLaunch {
            bleRepository.result
                .collect { bleDeviceRes ->

                    when (bleDeviceRes) {
                        is BleScanResource.Error -> {
                            Log.d(TAG, "BleScanResource Error: ${bleDeviceRes.error}")
                            _state.update { currState ->
                                currState.copy(
                                    errors = currState.errors + bleDeviceRes.error.toString()
                                )
                            }
                        }

                        is BleScanResource.Loading -> {
                            Log.d(TAG, "BleScanResource Loading: ${bleDeviceRes}")
                        }

                        is BleScanResource.Success -> {
                            val bleDevices = bleDeviceRes.value
                                .associateBy { it.macAddress }

                            _state.update { currState ->
                                currState.copy(
                                    bleDevices = _state.value.bleDevices.plus(bleDevices)
                                )
                            }
//                            Log.d(TAG, "BleScanResource Success: ${_state.value.bleDevices}")
                        }
                    }
                }

        }
    }

    companion object {
        private const val TAG = "ClientBleListViewModel"
    }
}