package dev.kavrin.rsable.presentation.screens.client.ble_detail

import dev.kavrin.rsable.domain.model.BleDevice
import dev.kavrin.rsable.domain.model.GattService
import dev.kavrin.rsable.presentation.util.UnidirectionalViewModel

interface BleDetailContract :
    UnidirectionalViewModel<BleDetailContract.State, BleDetailContract.Effect, BleDetailContract.Event> {
    data class State(
        val isLoading: Boolean = false,
        val bleDevice: BleDevice? = null,
        val writeValue: String = "",
        val notifValues: List<Float> = emptyList(),
        val readValue: String = "",
    )

    sealed interface Effect {
        data object NavigateBack : Effect
    }

    sealed interface Event {
        data object OnNavigateBack : Event
        data class OnSelectedBleChange(val bleDevice: BleDevice?) : Event
        data class OnWriteValueChange(val value: String) : Event
        data class OnNotifyClick(val uuid: String) : Event
        data class OnReadClick(val uuid: String) : Event
        data class OnWriteClick(val uuid: String, val value: String): Event
    }
}