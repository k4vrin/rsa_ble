package dev.kavrin.rsable.presentation.screens.ble_list

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startForegroundService
import dev.kavrin.rsable.presentation.service.BleForegroundService
import dev.kavrin.rsable.presentation.theme.DarkGreen
import dev.kavrin.rsable.presentation.theme.RsPink
import dev.kavrin.rsable.presentation.theme.padding
import dev.kavrin.rsable.presentation.util.VerticalSpacer
import dev.kavrin.rsable.presentation.util.collectInLaunchedEffect
import dev.kavrin.rsable.presentation.util.use
import org.koin.androidx.compose.koinViewModel

private const val TAG = "ClientBleListScreen"

@Composable
fun ClientBleListScreenRoot(
    modifier: Modifier = Modifier,
    viewModel: ClientBleListViewModel = koinViewModel(),
) {

    val (state, effect, dispatch) = use(viewModel)
    val context = LocalContext.current.applicationContext

    effect.collectInLaunchedEffect { eff ->
        when (eff) {
            ClientBleListContract.Effect.StartScan -> {
                Log.d(TAG, "ClientBleListScreenRoot:StartScan ")
                context.manageBleService(eff)
            }
            ClientBleListContract.Effect.StopScan -> {
                context.manageBleService(eff)
            }
        }
    }

    ClientBleListScreen(
        modifier = modifier,
        state = state,
        dispatch = dispatch
    )
}

@Composable
fun ClientBleListScreen(
    modifier: Modifier = Modifier,
    state: ClientBleListContract.State,
    dispatch: (ClientBleListContract.Event) -> Unit,
) {

    LaunchedEffect(Unit) {
        dispatch(ClientBleListContract.Event.OnStartScan)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = DarkGreen
            )
            .padding(MaterialTheme.padding.medium)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.padding.medium),
        ) {

            items(
                items = state.bleDevices.values.toList(),
                key = { it.macAddress.value }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RsPink)
                        .padding(MaterialTheme.padding.medium)
                ) {
                    Row {
                        Text(
                            modifier = Modifier,
                            text = it.macAddress.value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkGreen
                        )
                        VerticalSpacer(MaterialTheme.padding.small)
                        Text(
                            modifier = Modifier,
                            text = it.name ?: "Unknown",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkGreen
                        )
                        VerticalSpacer(MaterialTheme.padding.small)
                        AnimatedContent(
                            targetState = it.rssi
                        ) {
                            Text(
                                modifier = Modifier,
                                text = it.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkGreen
                            )
                        }
                    }
                }
            }

        }

    }

}


fun Context.manageBleService(action: ClientBleListContract.Effect) {
    val intent = Intent(this, BleForegroundService::class.java)

    when (action) {
        ClientBleListContract.Effect.StartScan -> {
            intent.action = BleForegroundService.ACTION_START_SCAN
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
        ClientBleListContract.Effect.StopScan -> {
            intent.action = BleForegroundService.ACTION_STOP_SCAN
            stopService(intent)
        }
    }
}