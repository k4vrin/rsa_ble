package dev.kavrin.rsable.presentation.screens.client.ble_list

import dev.kavrin.rsable.domain.model.BleDevice
import dev.kavrin.rsable.domain.model.GattService
import dev.kavrin.rsable.domain.model.MacAddress
import dev.kavrin.rsable.presentation.util.UnidirectionalViewModel

interface BleListContract :
    UnidirectionalViewModel<BleListContract.State, BleListContract.Effect, BleListContract.Event> {

    data class State(
        val isLoading: Boolean = true,
        val isScanning: Boolean = true,
        val bleDevices: Map<MacAddress, BleDevice> = emptyMap()
    )

    sealed interface Effect {
        data object StartScan : Effect
        data object StopScan : Effect
        data class NavigateToDetail(val bleDevice: BleDevice, val gattServices: List<GattService>) :
            Effect
    }

    sealed interface Event {
        data object OnStartScan : Event
        data object OnStopScan : Event
        data class OnDeviceClicked(val bleDevice: BleDevice) : Event
    }
}