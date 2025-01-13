package dev.kavrin.rsable.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for a unidirectional view model.
 *
 * This interface defines a view model that follows a unidirectional data flow pattern.
 * It exposes a [StateFlow] for the UI state, a [Flow] for one-time effects, and a
 * function to handle user or system [events].
 *
 * @param STATE The type representing the immutable UI state.
 * @param EFFECT The type representing one-time side effects to be performed by the UI.
 * @param EVENT The type representing user or system events that can trigger state changes.
 *
 * The unidirectional flow can be described as follows:
 *
 * 1. **State:** The UI renders itself based on the current [state].
 * 2. **Event:** The UI dispatches an [event] in response to user interactions or other inputs.
 * 3. **onEvent:** The [onEvent] function in the view model receives the [event].
 * 4. **Update State:** The view model updates its internal state based on the received [event].
 * 5. **Effect:** If needed, the view model can emit an [effect] to trigger side effects (e.g., navigation, showing a toast).
 * 6. **New State:** The view model exposes the updated [state] through the [state] property.
 * 7. **Repeat:** The UI observes the new [state] and re-renders itself, completing the cycle.
 *
 * **Key Concepts:**
 *
 * - **State (STATE):** Represents the entire UI state in a single, immutable data structure.
 *   Changes to the state should result in UI re-renders.
 * - **Effect (EFFECT):** Represents one-time side effects. These are typically actions that
 *   are not directly represented in the UI state, such as navigation, showing a snackbar,
 *   or displaying an alert dialog.
 * - **Event (EVENT):** Represents user actions or system events that can potentially change
 *   the UI state or trigger an effect.
 *
 * **Benefits of Unidirectional Data Flow:**
 *
 * - **Predictability:** The UI state is determined solely by the view model's state,
 *   making it easier to reason about and debug.
 * - **Test */
interface UnidirectionalViewModel<STATE, EFFECT, EVENT> {
    val state: StateFlow<STATE>
    val effect: Flow<EFFECT>
    fun onEvent(event: EVENT)
}

data class StateEffectDispatch<STATE, EFFECT, EVENT>(
    val state: STATE,
    val effect: Flow<EFFECT>,
    val dispatch: (EVENT) -> Unit
)

/**
 * A Composable function that simplifies the usage of a [UnidirectionalViewModel] within a Compose UI.
 *
 * It collects the current [STATE] from the ViewModel's state flow, exposes a function to dispatch [EVENT]s,
 * and exposes the [EFFECT] flow.
 *
 * This function streamlines the process of observing and interacting with a UnidirectionalViewModel,
 * removing the need for boilerplate code to collect state and handle events.
 *
 * @param viewModel The [UnidirectionalViewModel] to connect to the UI.
 * @param STATE The type representing the state held by the ViewModel.
 * @param EFFECT The type representing the side effects produced by the ViewModel.
 * @param EVENT The type representing the events that can be dispatched to the ViewModel.
 * @return A [StateEffectDispatch] object containing the current state, the effect flow, and the dispatch function.
 *
 * @see UnidirectionalViewModel
 * @see StateEffectDispatch
 * @see collectAsStateWithLifecycle
 *
 * Example Usage:
 * ```kotlin
 * @Composable
 * fun MyScreen(myViewModel: MyViewModel) {
 *     val (state, effect, dispatch) = use(myViewModel)
 *
 *     // Access and render the state
 *     Text("Counter: ${state.count}")
 *
 *     // Trigger an event using dispatch
 *     Button(onClick = { dispatch(MyEvent.Increment) }) {
 *         Text("Increment")
 *     }
 *
 *     // Handle effects using LaunchedEffect or similar
 *     LaunchedEffect(effect) {
 *        effect.collect { myEffect ->
 *          when(myEffect) {
 *             MyEffect.ShowToast -> Toast.makeText(context, "Toast!", Toast.LENGTH_SHORT).show()
 *           }
 *         }
 *      }
 * }
 *
 * */
@Composable
inline fun <reified STATE, EFFECT, EVENT> use(
    viewModel: UnidirectionalViewModel<STATE, EFFECT, EVENT>
): StateEffectDispatch<STATE, EFFECT, EVENT> {

    val state: STATE by viewModel.state.collectAsStateWithLifecycle()

    val dispatch: (EVENT) -> Unit = { event ->
        viewModel.onEvent(event)
    }

    return StateEffectDispatch(
        state = state,
        effect = viewModel.effect,
        dispatch = dispatch
    )
}