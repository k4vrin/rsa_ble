package dev.kavrin.rsable.presentation.screens.intro

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dev.kavrin.rsable.R
import dev.kavrin.rsable.presentation.screens.intro.components.RSAButton
import dev.kavrin.rsable.presentation.theme.DarkGreen
import dev.kavrin.rsable.presentation.theme.RsLight
import dev.kavrin.rsable.presentation.theme.RsYellow
import dev.kavrin.rsable.presentation.theme.padding
import dev.kavrin.rsable.presentation.util.HorizontalSpacer
import dev.kavrin.rsable.presentation.util.PermissionsUtil
import dev.kavrin.rsable.presentation.util.VerticalSpacer
import dev.kavrin.rsable.presentation.util.collectInLaunchedEffect
import dev.kavrin.rsable.presentation.util.isBleSupported
import dev.kavrin.rsable.presentation.util.isPeripheralModeSupported
import dev.kavrin.rsable.presentation.util.use
import org.koin.androidx.compose.koinViewModel

private const val TAG = "IntroScreen"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun IntroScreenRoot(
    modifier: Modifier = Modifier,
    viewModel: IntroViewModel = koinViewModel(),
    onNavigateToPeripheral: () -> Unit,
    onNavigateToCentral: () -> Unit,
) {
    val (state, effect, dispatch) = use(viewModel)
    val blePermissionState =
        rememberMultiplePermissionsState(permissions = PermissionsUtil.blePermissions)
    val bgPermissionState = rememberMultiplePermissionsState(permissions = PermissionsUtil.backgroundLocationPermission)

    LaunchedEffect(blePermissionState.allPermissionsGranted) {
        dispatch(IntroContract.Event.OnChangePermissionState(blePermissionState.allPermissionsGranted))
    }
    effect.collectInLaunchedEffect { eff ->
        when (eff) {
            IntroContract.Effect.NavigateToCentral -> {
                onNavigateToCentral()
            }

            IntroContract.Effect.NavigateToPeripheral -> {
                onNavigateToPeripheral()
            }

            IntroContract.Effect.AskForPermissions -> {
                Log.d(TAG, "IntroScreenRoot: AskForPermissions")
                blePermissionState.launchMultiplePermissionRequest()
                bgPermissionState.launchMultiplePermissionRequest()
            }
        }
    }

    IntroScreen(
        modifier = modifier,
        state = state,
        dispatch = dispatch
    )
}

@Composable
fun IntroScreen(
    modifier: Modifier,
    state: IntroContract.State,
    dispatch: (IntroContract.Event) -> Unit,
) {

    val context = LocalContext.current

    LaunchedEffect(Unit) {

        dispatch(
            IntroContract.Event.OnPeripheralModeSupported(
                context.isPeripheralModeSupported()
            )
        )

        dispatch(
            IntroContract.Event.OnBleSupported(
                context.isBleSupported()
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = DarkGreen
            )
            .padding(MaterialTheme.padding.medium)
    ) {

        Column(
            modifier = Modifier
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                modifier = Modifier,
                text = "Welcome",
                style = MaterialTheme.typography.titleLarge,
                color = RsLight
            )
            VerticalSpacer(MaterialTheme.padding.medium)
            Image(
                modifier = Modifier,
                painter = painterResource(id = R.drawable.main_title),
                contentDescription = null
            )
            VerticalSpacer(MaterialTheme.padding.medium)
//            RSAButton(
//                title = "Peripheral(Server)",
//                enabled = state.isBLESupported && state.isPermissionGranted,
//                onClick = { dispatch(IntroContract.Event.OnNavigateToPeripheral) }
//            )
//
//            VerticalSpacer(MaterialTheme.padding.medium)

            RSAButton(
                title = "Ble Central",
                enabled = state.isBLESupported && state.isPeripheralModeSupported && state.isPermissionGranted,
                onClick = { dispatch(IntroContract.Event.OnNavigateToCentral) }
            )

        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            androidx.compose.animation.AnimatedVisibility(
                visible = !state.isBLESupported
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        tint = RsYellow,
                        contentDescription = stringResource(R.string.warning_icon)
                    )

                    HorizontalSpacer(MaterialTheme.padding.medium)
                    Text(
                        modifier = Modifier,
                        text = "Unfortunately your device does not support BLE.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RsYellow
                    )
                }

            }

            VerticalSpacer(MaterialTheme.padding.medium)

            androidx.compose.animation.AnimatedVisibility(
                visible = !state.isPeripheralModeSupported
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        tint = RsYellow,
                        contentDescription = stringResource(R.string.warning_icon)
                    )
                    HorizontalSpacer(MaterialTheme.padding.medium)
                    Text(
                        modifier = Modifier,
                        text = "Unfortunately your device does not support peripheral mode.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RsYellow
                    )
                }

            }

            androidx.compose.animation.AnimatedVisibility(
                visible = state.isBLESupported && !state.isPermissionGranted
            ) {

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            tint = RsYellow,
                            contentDescription = stringResource(R.string.warning_icon)
                        )
                        HorizontalSpacer(MaterialTheme.padding.medium)
                        Text(
                            modifier = Modifier,
                            text = "The necessary permissions are not granted.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RsYellow
                        )
                    }
                    VerticalSpacer(MaterialTheme.padding.medium)
                    RSAButton(
                        title = "Grant Permissions",
                        onClick = { dispatch(IntroContract.Event.OnRequestToGrantPermissions) }
                    )
                }

            }

        }

    }
}

@Preview
@Composable
private fun IntroScreenPrev() {
    IntroScreen(
        modifier = Modifier,
        state = IntroContract.State(),
        dispatch = {}
    )
}