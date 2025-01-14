package dev.kavrin.rsable.presentation.screens.ble_list

import dev.kavrin.rsable.domain.model.BleDevice
import dev.kavrin.rsable.domain.model.MacAddress
import dev.kavrin.rsable.presentation.util.UnidirectionalViewModel

interface ClientBleListContract :
    UnidirectionalViewModel<ClientBleListContract.State, ClientBleListContract.Effect, ClientBleListContract.Event> {

    data class State(
        val isLoading: Boolean = true,
        val bleDevices: Map<MacAddress, BleDevice> = emptyMap()
    )

    sealed interface Effect {
        data object StartScan : Effect
        data object StopScan : Effect
    }

    sealed interface Event {
        data object OnStartScan : Event
        data object OnStopScan : Event
    }
}