package dev.kavrin.rsable.presentation.intro

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kavrin.rsable.util.safeLaunch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class IntroViewModel : ViewModel(), IntroContract {

    private val _state = MutableStateFlow(IntroContract.State())
    override val state: StateFlow<IntroContract.State> = _state
        .onStart {}
        .stateIn(
            scope = viewModelScope,
            // 5 seconds longer than the last subscriber disconnects
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = _state.value
        )


    private val effectChannel = Channel<IntroContract.Effect>()
    override val effect: Flow<IntroContract.Effect> = effectChannel.receiveAsFlow()

    override fun onEvent(event: IntroContract.Event) {
        when (event) {
            IntroContract.Event.OnNavigateToCentral -> {/* TODO:  */}
            IntroContract.Event.OnNavigateToPeripheral -> {/* TODO:  */}
            is IntroContract.Event.OnBleSupported -> {/* TODO:  */}
            is IntroContract.Event.OnChangePermissionState -> {
                Log.d(TAG, "onEvent OnChangePermissionState isGranted: ${event.isGranted}")

                _state.update { currState ->
                    currState.copy(isPermissionGranted = event.isGranted)
                }

            }
            is IntroContract.Event.OnPeripheralModeSupported -> {/* TODO:  */}
            IntroContract.Event.OnRequestToGrantPermissions -> {
                viewModelScope.safeLaunch {
                    effectChannel.send(IntroContract.Effect.AskForPermissions)
                }
            }
        }
    }

    companion object {
        private const val TAG = "IntroViewModel"
    }

}