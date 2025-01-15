package dev.kavrin.rsable.presentation.screens.ble_list

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.kavrin.rsable.R
import dev.kavrin.rsable.domain.model.BleDeviceType
import dev.kavrin.rsable.presentation.service.BleForegroundService
import dev.kavrin.rsable.presentation.theme.DarkGreen
import dev.kavrin.rsable.presentation.theme.RsOrange
import dev.kavrin.rsable.presentation.theme.RsPink
import dev.kavrin.rsable.presentation.theme.RsRed
import dev.kavrin.rsable.presentation.theme.RsYellow
import dev.kavrin.rsable.presentation.theme.padding
import dev.kavrin.rsable.presentation.util.HorizontalSpacer
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
                Log.d(TAG, "stop scan: $eff ")
                context.manageBleService(eff)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "ClientBleListScreen onDispose: ")
            dispatch(ClientBleListContract.Event.OnStopScan)
            context.manageBleService(ClientBleListContract.Effect.StopScan)
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
                .padding(top = MaterialTheme.padding.medium),
        ) {

            items(
                items = state.bleDevices.values.toList(),
                key = { it.macAddress.value }
            ) {

                Box {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = MaterialTheme.padding.medium)
                            .animateItem(),
                        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.padding.extraSmall)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(RsOrange)
                                .padding(MaterialTheme.padding.small)
                        ) {



                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                ElevatedCard(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .border(
                                            width = 2.dp,
                                            color = RsYellow,
                                            shape = CircleShape
                                        ),
                                    shape = CircleShape,
                                    colors = CardDefaults.cardColors(
                                        containerColor = RsPink,
                                        contentColor = RsOrange
                                    )
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                        ,
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            modifier = Modifier
                                                .size(40.dp),
                                            painter = when (it.bleDeviceType) {
                                                BleDeviceType.HEART_RATE_MONITOR -> painterResource(R.drawable.ic_monitor_heart_24)
                                                BleDeviceType.PROXIMITY_MONITOR -> painterResource(R.drawable.ic_screenshot_monitor_24)
                                                BleDeviceType.BLOOD_PRESSURE_MONITOR -> painterResource(R.drawable.ic_bloodtype_24)
                                                BleDeviceType.CYCLING_MONITOR -> painterResource(R.drawable.ic_cyclone_24)
                                                BleDeviceType.RUNNING_MONITOR -> painterResource(R.drawable.ic_directions_run_24)
                                                BleDeviceType.UNKNOWN -> painterResource(R.drawable.ic_bluetooth_scanning)
                                            },
                                            contentDescription = ""
                                        )
                                    }

                                }

                                HorizontalSpacer(MaterialTheme.padding.small)

                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Device name row
                                    Text(
                                        text = it.name ?: "Unknown",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DarkGreen
                                    )

                                    VerticalSpacer(MaterialTheme.padding.extraSmall)

                                    // MAC address
                                    Text(
                                        text = it.macAddress.value,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DarkGreen.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd),
                        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.padding.extraSmall),
                        colors = CardDefaults.cardColors(containerColor = RsRed)
                    ) {
                        AnimatedContent(
                            targetState = it.rssi,
                            label = "RSSI"
                        ) { rssi ->
                            Text(
                                modifier = Modifier
                                    .padding(MaterialTheme.padding.extraSmall),
                                text = "${rssi}dBm",
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