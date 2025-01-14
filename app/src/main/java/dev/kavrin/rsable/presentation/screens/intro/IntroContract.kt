package dev.kavrin.rsable.presentation.screens.intro

import dev.kavrin.rsable.presentation.util.UnidirectionalViewModel

interface IntroContract :
    UnidirectionalViewModel<IntroContract.State, IntroContract.Effect, IntroContract.Event> {
    data class State(
        val isLoading: Boolean = true,
        val isPermissionGranted: Boolean = true,
        val isPeripheralModeSupported: Boolean = true,
        val isBLESupported: Boolean = true
    )

    sealed interface Effect {
        data object NavigateToPeripheral : Effect
        data object NavigateToCentral : Effect
        data object AskForPermissions : Effect
    }

    sealed interface Event {
        data object OnNavigateToPeripheral : Event
        data object OnNavigateToCentral : Event
        data object OnRequestToGrantPermissions : Event
        data class OnChangePermissionState(val isGranted: Boolean) : Event
        data class OnBleSupported(val isSupported: Boolean) : Event
        data class OnPeripheralModeSupported(val isSupported: Boolean) : Event
    }

}