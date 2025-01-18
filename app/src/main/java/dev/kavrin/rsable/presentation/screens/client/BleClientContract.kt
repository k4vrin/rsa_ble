package dev.kavrin.rsable.presentation.screens.client

import dev.kavrin.rsable.domain.model.BleDevice
import dev.kavrin.rsable.domain.model.GattService
import dev.kavrin.rsable.presentation.util.UnidirectionalViewModel

interface BleClientContract :
    UnidirectionalViewModel<BleClientContract.State, BleClientContract.Effect, BleClientContract.Event> {
    data class State(
        val bleDevice: BleDevice? = null,
    )

    sealed interface Effect {}
    sealed interface Event {
        data class OnSelectedBleChange(
            val bleDevice: BleDevice?,
            val gattServices: List<GattService>,
        ) : Event
    }
}