package dev.kavrin.rsable.presentation.screens.client.ble_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.kavrin.rsable.R
import dev.kavrin.rsable.presentation.screens.client.component.BackHandler
import dev.kavrin.rsable.presentation.screens.client.component.HeartRateChartWithGrid
import dev.kavrin.rsable.presentation.theme.DarkGreen
import dev.kavrin.rsable.presentation.theme.Dimen
import dev.kavrin.rsable.presentation.theme.RsLight
import dev.kavrin.rsable.presentation.theme.RsOrange
import dev.kavrin.rsable.presentation.theme.RsPink
import dev.kavrin.rsable.presentation.theme.RsRed
import dev.kavrin.rsable.presentation.theme.padding
import dev.kavrin.rsable.presentation.util.HorizontalSpacer
import dev.kavrin.rsable.presentation.util.VerticalSpacer
import dev.kavrin.rsable.presentation.util.collectInLaunchedEffect
import dev.kavrin.rsable.presentation.util.use
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.seconds

private const val TAG = "BleDetailScreen"

@Composable
fun BleDetailScreenRoot(
    modifier: Modifier = Modifier,
    viewModel: BleDetailViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {

    val (state, effect, dispatch) = use(viewModel)
    val context = LocalContext.current.applicationContext

    effect.collectInLaunchedEffect { eff ->
        when (eff) {
            BleDetailContract.Effect.NavigateBack -> onNavigateBack()
        }
    }

    BackHandler {
        dispatch(BleDetailContract.Event.OnNavigateBack)
    }

    BleDetailScreen(
        modifier = modifier,
        state = state,
        dispatch = dispatch
    )

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BleDetailScreen(
    modifier: Modifier = Modifier,
    state: BleDetailContract.State,
    dispatch: (BleDetailContract.Event) -> Unit
) {

    val currentError = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.errors) {
        for (err in state.errors) {
            currentError.value = err
            delay(2.seconds)
            currentError.value = null
            delay(500)
        }
        dispatch(BleDetailContract.Event.OnClearErrors)
    }

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
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

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            HeartRateChartWithGrid(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                data = state.notifValues,
            )

            VerticalSpacer(MaterialTheme.padding.medium)

            Card(
                modifier = Modifier
                    .padding(vertical = MaterialTheme.padding.medium)
                    .fillMaxWidth()
                    .weight(1.5f),
                colors = CardDefaults.cardColors(
                    containerColor = RsOrange,
                    contentColor = RsRed
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.padding.extraSmall)
            ) {
                Column (
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(MaterialTheme.padding.extraMedium)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Heart Rate Service:",
                            style = MaterialTheme.typography.titleSmall,
                            color = DarkGreen
                        )

                        HorizontalSpacer(MaterialTheme.padding.small)

                        Text(
                            text = BleDetailViewModel.parseUuidToShortForm(state.bleDevice?.services?.firstOrNull()?.uuid),
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkGreen
                        )
                    }

                    VerticalSpacer(MaterialTheme.padding.medium)

                    Text(
                        text = "Characteristics:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DarkGreen
                    )

                    state.bleDevice?.services?.firstOrNull()?.gattCharacteristics?.forEach { characteristic ->
                        Column (
                            modifier = Modifier
                                .padding(vertical = MaterialTheme.padding.medium),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = parseUuidName(uuid = characteristic.uuid),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = DarkGreen
                                )
                                HorizontalSpacer(MaterialTheme.padding.extraSmall)
                                Text(
                                    text = BleDetailViewModel.parseUuidToShortForm(characteristic.uuid),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkGreen
                                )
                            }
                            VerticalSpacer(MaterialTheme.padding.medium)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                parseUuidProperty(characteristic.uuid).let {
                                    when (it) {
                                        BleDetailViewModel.Companion.CharacteristicProperty.READABLE -> {
                                            Text(
                                                text = "Read value: ",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = DarkGreen
                                            )
                                            HorizontalSpacer(MaterialTheme.padding.small)
                                            Text(
                                                modifier = Modifier
                                                    .weight(1.5f),
                                                text = state.readValue,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = DarkGreen
                                            )
                                            HorizontalSpacer(MaterialTheme.padding.small)
                                            Button(
                                                modifier = Modifier
                                                    .weight(1f),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = DarkGreen,
                                                    contentColor = RsLight
                                                ),
                                                shape = CircleShape,
                                                onClick = {
                                                    dispatch(BleDetailContract.Event.OnReadClick(characteristic.uuid))
                                                }
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.baseline_refresh_24),
                                                    tint = RsLight,
                                                    contentDescription = ""
                                                )
                                            }
                                        }
                                        BleDetailViewModel.Companion.CharacteristicProperty.WRITABLE -> {
                                            TextField(
                                                modifier = Modifier
                                                    .widthIn(
                                                        min = 100.dp,
                                                        max = 150.dp
                                                    ),
                                                value = state.writeValue,
                                                onValueChange = { value ->
                                                    dispatch(BleDetailContract.Event.OnWriteValueChange(value = value))
                                                },
                                                label = {
                                                    Text(
                                                        text = "Write value",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = DarkGreen.copy(alpha = Dimen.DISABLED_ALPHA)
                                                    )
                                                },
                                                textStyle = MaterialTheme.typography.labelMedium,
                                                colors = TextFieldDefaults.colors(
                                                    focusedTextColor = DarkGreen,
                                                    unfocusedTextColor = DarkGreen,
                                                    unfocusedLabelColor = RsOrange,
                                                    focusedLabelColor = RsOrange,
                                                    focusedContainerColor = RsPink,
                                                    unfocusedContainerColor = RsPink
                                                )
                                            )

                                            HorizontalSpacer(MaterialTheme.padding.small)
                                            Button(
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = DarkGreen,
                                                    contentColor = RsLight
                                                ),
                                                shape = CircleShape,
                                                onClick = {
                                                    dispatch(BleDetailContract.Event.OnWriteClick(characteristic.uuid, state.writeValue))
                                                }
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.round_textsms_24),
                                                    tint = RsLight,
                                                    contentDescription = ""
                                                )
                                            }
                                        }
                                        BleDetailViewModel.Companion.CharacteristicProperty.NOTIFIABLE -> {
                                            Text(
                                                text = "value: ",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = DarkGreen
                                            )
                                            HorizontalSpacer(MaterialTheme.padding.medium)
                                            Text(
                                                modifier = Modifier
                                                    .weight(1.5f),
                                                text = state.notifValues.toString(),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = DarkGreen
                                            )
                                            HorizontalSpacer(MaterialTheme.padding.extraMedium)
                                            Button(
                                                modifier = Modifier
                                                    .weight(1f),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = DarkGreen,
                                                    contentColor = RsLight
                                                ),
                                                shape = CircleShape,
                                                onClick = {
                                                    dispatch(BleDetailContract.Event.OnNotifyClick(characteristic.uuid))
                                                }
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.baseline_notifications_24),
                                                    tint = RsLight,
                                                    contentDescription = ""
                                                )
                                            }
                                        }
                                        BleDetailViewModel.Companion.CharacteristicProperty.UNKNOWN -> {
                                            Text(
                                                text = "N/A",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = DarkGreen
                                            )
                                        }
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
                .align(Alignment.Center)
                .background(DarkGreen.copy(alpha = Dimen.DISABLED_ALPHA)),
            visible = state.isLoading || state.isReconnecting,
            enter = fadeIn(),
            exit = fadeOut()
        ) {

            Surface(
                modifier = modifier,
                color = DarkGreen.copy(alpha = Dimen.DISABLED_ALPHA),
                shape = RoundedCornerShape(MaterialTheme.padding.medium)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkGreen.copy(alpha = Dimen.DISABLED_ALPHA)),
                    contentAlignment = Alignment.Center
                ) {
                    ElevatedCard(
                        modifier = Modifier
                            .height(120.dp)
                            .width(150.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(MaterialTheme.padding.medium)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                            )
                            Text(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter),
                                text = if (state.isReconnecting) "Reconnecting..." else "Loading...",
                                style = MaterialTheme.typography.labelSmall,
                                color = RsLight
                            )
                        }
                    }
                }

            }


        }

    }
}

fun parseUuidName(uuid: String): String {
    return BleDetailViewModel.Companion.HeartRateChar.entries.firstOrNull {
        it.uuid == uuid
    }?.let {
        when (it) {
            BleDetailViewModel.Companion.HeartRateChar.HEART_RATE_MEASUREMENT_UUID -> "Heart rate:"
            BleDetailViewModel.Companion.HeartRateChar.BODY_SENSOR_LOCATION_UUID -> "Body sensor:"
            BleDetailViewModel.Companion.HeartRateChar.HEART_RATE_CONTROL_POINT_UUID -> "Heart rate control:"
        }
    }?: "Characteristic:"
}

fun parseUuidProperty(uuid: String): BleDetailViewModel.Companion.CharacteristicProperty {
    return BleDetailViewModel.Companion.HeartRateChar.entries.firstOrNull {
        it.uuid == uuid
    }?.let {
        it.operationType
    }?: BleDetailViewModel.Companion.CharacteristicProperty.UNKNOWN
}