package dev.kavrin.rsable.presentation.screens.ble_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kavrin.rsable.data.local.BleScanManager
import dev.kavrin.rsable.domain.model.BleScanResource
import dev.kavrin.rsable.util.safeLaunch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ClientBleListViewModel(
    private val bleScanManager: BleScanManager,
) : ViewModel(), ClientBleListContract {

    private val _state = MutableStateFlow(ClientBleListContract.State())
    override val state: StateFlow<ClientBleListContract.State> = _state
        .onStart {}
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _state.value
        )


    private val effectChannel = Channel<ClientBleListContract.Effect>()
    override val effect: Flow<ClientBleListContract.Effect> = effectChannel.receiveAsFlow()

    override fun onEvent(event: ClientBleListContract.Event) {
        when (event) {
            ClientBleListContract.Event.OnStartScan -> {
                viewModelScope.safeLaunch {
                    effectChannel.send(ClientBleListContract.Effect.StartScan)
                }

                observerBleDevices()
            }

            ClientBleListContract.Event.OnStopScan -> {
                viewModelScope.safeLaunch {
                    effectChannel.send(ClientBleListContract.Effect.StopScan)
                }
            }
        }
    }

    private fun observerBleDevices() {
        viewModelScope.safeLaunch {
            bleScanManager.result
                .collect { bleDeviceRes ->

                    when (bleDeviceRes) {
                        is BleScanResource.Error -> {
                            Log.d(TAG, "BleScanResource Error: ${bleDeviceRes.error}")
                        }

                        is BleScanResource.Loading -> {
                            Log.d(TAG, "BleScanResource Loading: ${bleDeviceRes}")
                            _state.update { currState ->
                                currState.copy(isLoading = true)
                            }

                        }

                        is BleScanResource.Success -> {
                            val bleDevices = bleDeviceRes.value.map { it.toBleDevice() }
                                .associateBy { it.macAddress }

                            _state.update { currState ->
                                currState.copy(bleDevices = _state.value.bleDevices.plus(bleDevices))
                            }
                            Log.d(TAG, "BleScanResource Success: ${_state.value.bleDevices}")
                        }
                    }
                }

        }
    }

    companion object {
        private const val TAG = "ClientBleListViewModel"
    }
}