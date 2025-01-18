package dev.kavrin.rsable.presentation.screens.client.ble_list

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import dev.kavrin.rsable.R
import dev.kavrin.rsable.domain.model.BleDevice
import dev.kavrin.rsable.domain.model.BleDeviceType
import dev.kavrin.rsable.domain.model.GattService
import dev.kavrin.rsable.presentation.screens.client.component.BackHandler
import dev.kavrin.rsable.presentation.service.BleForegroundService
import dev.kavrin.rsable.presentation.theme.DarkGreen
import dev.kavrin.rsable.presentation.theme.RsLight
import dev.kavrin.rsable.presentation.theme.RsOrange
import dev.kavrin.rsable.presentation.theme.RsPink
import dev.kavrin.rsable.presentation.theme.RsRed
import dev.kavrin.rsable.presentation.theme.RsYellow
import dev.kavrin.rsable.presentation.theme.padding
import dev.kavrin.rsable.presentation.util.HorizontalSpacer
import dev.kavrin.rsable.presentation.util.VerticalSpacer
import dev.kavrin.rsable.presentation.util.collectInLaunchedEffect
import dev.kavrin.rsable.presentation.util.isBluetoothEnabled
import dev.kavrin.rsable.presentation.util.isLocationEnabled
import dev.kavrin.rsable.presentation.util.observeLocationState
import dev.kavrin.rsable.presentation.util.use
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.seconds

private const val TAG = "ClientBleListScreen"

@SuppressLint("MissingPermission")
@Composable
fun BleListScreenRoot(
    modifier: Modifier = Modifier,
    viewModel: BleListViewModel = koinViewModel(),
    onNavigateToDetail: (BleDevice, List<GattService>) -> Unit,
    onNavigateBack: () -> Unit
) {

    val (state, effect, dispatch) = use(viewModel)
    val context = LocalContext.current.applicationContext

    effect.collectInLaunchedEffect { eff ->
        when (eff) {
            BleListContract.Effect.StartScan -> {
                val isBlEnable = context.getSystemService(BluetoothManager::class.java).adapter.isEnabled
                val isLocationEnabled = context.isLocationEnabled()
                Log.d(TAG, "ClientBleListScreenRoot:StartScan ")
                if (isBlEnable && isLocationEnabled) {
                    context.manageBleService(eff)
                } else {
                    dispatch(BleListContract.Event.OnBluetoothStatusChange(isBlEnable))
                    dispatch(BleListContract.Event.OnLocationStatusChange(isLocationEnabled))
                }
            }

            BleListContract.Effect.StopScan -> {
                Log.d(TAG, "stop scan: $eff ")
                context.manageBleService(eff)
            }

            is BleListContract.Effect.NavigateToDetail -> {
                Log.d(TAG, "NavigateToDetail")
                dispatch(BleListContract.Event.OnStopScan)
                context.manageBleService(BleListContract.Effect.StopScan)
                onNavigateToDetail(eff.bleDevice, eff.gattServices)
            }

            BleListContract.Effect.NavigateBack -> onNavigateBack()
        }
    }

    BackHandler {
        Log.d(TAG, "ClientBleListScreen onDispose: ")
        dispatch(BleListContract.Event.OnStopScan)
        context.manageBleService(BleListContract.Effect.StopScan)
        dispatch(BleListContract.Event.OnNavigateBack)
    }

    BleListScreen(
        modifier = modifier,
        state = state,
        dispatch = dispatch
    )
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BleListScreen(
    modifier: Modifier = Modifier,
    state: BleListContract.State,
    dispatch: (BleListContract.Event) -> Unit,
) {

    val currentError = remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(state.errors) {
        for (err in state.errors) {
            currentError.value = err
            delay(2.seconds)
            currentError.value = null
            delay(500)
        }
        dispatch(BleListContract.Event.OnClearErrors)
    }

    LaunchedEffect(Unit) {
        dispatch(BleListContract.Event.OnStartScan)
    }


    var scanIcon by remember(state.isScanning) {
        mutableStateOf(if (state.isScanning) R.drawable.baseline_stop_circle_24 else R.drawable.baseline_play_circle_24)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = DarkGreen
            )
            .padding(MaterialTheme.padding.medium)
    ) {

        AnimatedVisibility(
            modifier = Modifier
                .zIndex(999f),
            visible = currentError.value != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            currentError.value?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color.Red, shape = RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = error,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = MaterialTheme.padding.medium),
        ) {

            stickyHeader {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.padding.medium)
                        .animateItem(),
                    colors = CardDefaults.cardColors(
                        containerColor = RsOrange,
                        contentColor = RsRed
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.padding.extraSmall)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.padding.medium),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BLE Devices",
                            style = MaterialTheme.typography.titleLarge,
                        )

                        AnimatedContent(
                            targetState = scanIcon
                        ) {
                            IconButton(
                                modifier = Modifier
                                    .size(60.dp),
                                onClick = {
                                    if (state.isScanning) {
                                        dispatch(BleListContract.Event.OnStopScan)
                                    } else {
                                        dispatch(BleListContract.Event.OnStartScan)
                                    }
                                }
                            ) {
                                Icon(
                                    modifier = Modifier,
                                    painter = painterResource(it),
                                    contentDescription = ""
                                )
                            }
                        }
                    }
                }

            }

            items(
                items = state.bleDevices.values.toList(),
                key = { it.macAddress.value }
            ) { bleDevice ->

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {

                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .zIndex(99f),
                        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.padding.extraSmall),
                        colors = CardDefaults.cardColors(containerColor = RsRed)
                    ) {
                        AnimatedContent(
                            targetState = bleDevice.rssi,
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

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .padding(vertical = MaterialTheme.padding.medium)
                            .animateItem(),
                        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.padding.extraSmall)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(RsOrange)
                                .padding(
                                    vertical = MaterialTheme.padding.medium,
                                    horizontal = MaterialTheme.padding.small
                                )
                        ) {


                            Row(
                                modifier = Modifier
                                    .fillMaxSize(),
                                horizontalArrangement = Arrangement.Start,
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
                                            .fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            modifier = Modifier
                                                .size(40.dp),
                                            painter = when (bleDevice.type) {
                                                BleDeviceType.HEART_RATE_MONITOR -> painterResource(
                                                    R.drawable.ic_monitor_heart_24
                                                )

                                                BleDeviceType.PROXIMITY_MONITOR -> painterResource(R.drawable.ic_screenshot_monitor_24)
                                                BleDeviceType.BLOOD_PRESSURE_MONITOR -> painterResource(
                                                    R.drawable.ic_bloodtype_24
                                                )

                                                BleDeviceType.CYCLING_MONITOR -> painterResource(R.drawable.ic_cyclone_24)
                                                BleDeviceType.RUNNING_MONITOR -> painterResource(R.drawable.ic_directions_run_24)
                                                BleDeviceType.UNKNOWN -> painterResource(R.drawable.ic_bluetooth_scanning)
                                            },
                                            contentDescription = ""
                                        )
                                    }

                                }

                                HorizontalSpacer(MaterialTheme.padding.small)



                                Column {
                                    // Device name row
                                    Text(
                                        text = bleDevice.name ?: "N/A",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DarkGreen
                                    )

                                    VerticalSpacer(MaterialTheme.padding.extraSmall)

                                    // MAC address
                                    Text(
                                        text = bleDevice.macAddress.value,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DarkGreen.copy(alpha = 0.7f)
                                    )
                                }

                                if (bleDevice.isConnectable) {
                                    HorizontalSpacer(MaterialTheme.padding.large)
                                    Button(
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = DarkGreen,
                                            contentColor = RsLight
                                        ),
                                        shape = CircleShape,
                                        onClick = {
                                            dispatch(
                                                BleListContract.Event.OnDeviceClicked(
                                                    bleDevice
                                                )
                                            )
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_bluetooth_connected_24),
                                            tint = RsLight,
                                            contentDescription = ""
                                        )
                                    }
                                }

                            }
                        }
                    }


                }
            }

        }

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.Center),
            visible = state.isLoading
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .size(60.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

    }

}


fun Context.manageBleService(action: BleListContract.Effect) {
    val intent = Intent(this, BleForegroundService::class.java)
    Log.d(TAG, "manageBleService: $intent, $action")
    when (action) {
        BleListContract.Effect.StartScan -> {
            intent.action = BleForegroundService.ACTION_START_SCAN
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

        BleListContract.Effect.StopScan -> {
            intent.action = BleForegroundService.ACTION_STOP_SCAN
            stopService(intent)
        }

        is BleListContract.Effect.NavigateToDetail -> {
            intent.action = BleForegroundService.ACTION_STOP_SCAN
            stopService(intent)
        }

        BleListContract.Effect.NavigateBack -> {
            intent.action = BleForegroundService.ACTION_STOP_SCAN
            stopService(intent)
        }
    }
}