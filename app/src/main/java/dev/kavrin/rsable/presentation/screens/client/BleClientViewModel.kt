package dev.kavrin.rsable.presentation.screens.client

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class BleClientViewModel : ViewModel(), BleClientContract {

    private val _state = MutableStateFlow(BleClientContract.State())
    override val state: StateFlow<BleClientContract.State> = _state.asStateFlow()

    private val _effect = Channel<BleClientContract.Effect>()
    override val effect: Flow<BleClientContract.Effect> = _effect.receiveAsFlow()

    override fun onEvent(event: BleClientContract.Event) {
        when (event) {
            is BleClientContract.Event.OnSelectedBleChange -> {
                _state.update { currState ->
                    currState.copy(
                        bleDevice = event.bleDevice?.copy(
                            services = event.gattServices
                        )
                    )
                }

            }
        }
    }
}