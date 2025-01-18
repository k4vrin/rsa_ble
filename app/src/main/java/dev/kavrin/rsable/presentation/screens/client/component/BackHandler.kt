package dev.kavrin.rsable.presentation.screens.client.component

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner

/**
 * Registers a callback to handle back presses within a composable function.
 *
 * This function provides a declarative way to control the behavior of the system back button
 * within a composable UI hierarchy.
 *
 * @param enabled Controls whether the back handler is enabled or disabled. When `true`, back presses will be handled
 *   by the provided `onBack` callback. When `false`, back presses will be ignored. Defaults to `true`.
 * @param onBack The callback lambda that will be invoked when a back press is detected. This lambda should contain
 *   the logic to execute when a back press occurs, such as navigating to a previous screen or closing a dialog.
 *
 * **Usage:**
 *
 * ```kotlin
 * BackHandler(
 *     enabled = isDialogOpen,
 *     onBack = { closeDialog() }
 * )
 * ```
 *
 * In this example, the `BackHandler` will only handle back presses when `isDialogOpen` is `true`.
 * When a back press occurs, the `closeDialog()` function will be invoked.
 *
 * **Key Concepts:**
 *
 * *   **Composable:** `BackHandler` is designed to be used within a composable function.
 * *   **OnBackPressedDispatcher:** This function relies on the `OnBackPressedDispatcher`
 *     provided by the Android system to register and handle back press events.
 * *   **Lifecycle:** The callback is associated with a `LifecycleOwner` to ensure proper
 *     registration and removal based on the lifecycle of the composable.
 * *   **SideEffect:** The `SideEffect` is used to update the callback's enabled state whenever
 *     the `enabled` parameter changes.
 * *   **DisposableEffect:** The `DisposableEffect` is used to register the callback with the
 *     `OnBackPressedDispatcher` and automatically remove it when the composable leaves the
 *     composition.
 * *   **rememberUpdatedState:** The `rememberUpdatedState` is used to capture the most recent
 *      value of onBack, it updates the onBack lambda when a new one is provided.
 *
 * **Thread Safety:**
 *
 * The back handler is designed to be thread-safe. Multiple calls to `BackHandler` within a
 * single composable will not conflict with each other.
 *
 * **Error Handling:** */
@SuppressWarnings("MissingJvmstatic")
@Composable
public fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    // Safely update the current `onBack` lambda when a new one is provided
    val currentOnBack by rememberUpdatedState(onBack)
    // Remember in Composition a back callback that calls the `onBack` lambda
    val backCallback = remember {
        object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                currentOnBack()
            }
        }
    }
    // On every successful composition, update the callback with the `enabled` value
    SideEffect {
        backCallback.isEnabled = enabled
    }
    val backDispatcher = checkNotNull(LocalOnBackPressedDispatcherOwner.current) {
        "No OnBackPressedDispatcherOwner was provided via LocalOnBackPressedDispatcherOwner"
    }.onBackPressedDispatcher
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, backDispatcher) {
        // Add callback to the backDispatcher
        backDispatcher.addCallback(lifecycleOwner, backCallback)
        // When the effect leaves the Composition, remove the callback
        onDispose {
            backCallback.remove()
        }
    }
}
