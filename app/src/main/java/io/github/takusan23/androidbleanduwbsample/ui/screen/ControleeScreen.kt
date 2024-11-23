package io.github.takusan23.androidbleanduwbsample.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.RangingPosition
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbComplexChannel
import androidx.core.uwb.UwbDevice
import androidx.core.uwb.UwbManager
import io.github.takusan23.androidbleanduwbsample.UwbControllerParams
import io.github.takusan23.androidbleanduwbsample.ble.BleCentral
import io.github.takusan23.androidbleanduwbsample.ui.components.UwbArrow
import io.github.takusan23.androidbleanduwbsample.ui.components.UwbPointCanvas
import io.github.takusan23.androidbleanduwbsample.ui.components.UwbRecordPointCanvas
import kotlinx.coroutines.launch

/** Controlee(Guest) 側の画面 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControleeScreen() {
    val context = LocalContext.current

    // controller の位置
    val uwbPosition = remember { mutableStateOf<RangingPosition?>(null) }

    LaunchedEffect(key1 = Unit) {
        val uwbManager = UwbManager.createInstance(context)
        val controleeSession = uwbManager.controleeSessionScope()

        // controller に送る
        val addressByteArray = controleeSession.localAddress.address

        // BLE GATT サーバーへ接続し、UWB ホストと接続に必要なパラメーターを送受信する
        val bleCentral = BleCentral(context)
        bleCentral.connectGattServer()
        val uwbControllerParamsByteArray = bleCentral.readCharacteristic()
        val uwbControllerParams = UwbControllerParams.decode(uwbControllerParamsByteArray)
        bleCentral.writeCharacteristic(addressByteArray)
        bleCentral.destroy()

        // RangingParameters を作り UWB 接続を開始する
        val rangingParameters = RangingParameters(
            uwbConfigType = RangingParameters.CONFIG_MULTICAST_DS_TWR,
            complexChannel = UwbComplexChannel(uwbControllerParams.channel, uwbControllerParams.preambleIndex),
            peerDevices = listOf(UwbDevice.createForAddress(uwbControllerParams.address)),
            updateRateType = RangingParameters.RANGING_UPDATE_RATE_AUTOMATIC,
            sessionId = uwbControllerParams.sessionId,
            sessionKeyInfo = uwbControllerParams.sessionKeyInfo,
            subSessionId = 0, // SESSION_ID_UNSET ？
            subSessionKeyInfo = null // ？
        )

        launch {
            controleeSession.prepareSession(rangingParameters).collect { rangingResult ->
                when (rangingResult) {
                    is RangingResult.RangingResultPosition -> {
                        uwbPosition.value = rangingResult.position
                    }

                    is RangingResult.RangingResultPeerDisconnected -> {
                        uwbPosition.value = null
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "UWB Controlee") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // null になりえるので注意
            Text(text = "距離 = ${uwbPosition.value?.distance?.value} m")
            Text(text = "方位角 = ${uwbPosition.value?.azimuth?.value} 度")
            Text(text = "仰角 = ${uwbPosition.value?.elevation?.value} 度")

            UwbArrow(
                azimuth = uwbPosition.value?.azimuth?.value ?: 0f
            )

            val isCanvasInvert = remember { mutableStateOf(false) }
            Row {
                Text(text = "canvas を反転")
                Switch(checked = isCanvasInvert.value, onCheckedChange = { isCanvasInvert.value = it })
            }
            UwbPointCanvas(
                modifier = Modifier.size(300.dp),
                isInvert = isCanvasInvert.value,
                distance = uwbPosition.value?.distance?.value ?: 0f,
                azimuth = uwbPosition.value?.azimuth?.value ?: 0f
            )
            UwbRecordPointCanvas(
                modifier = Modifier.size(300.dp),
                isInvert = isCanvasInvert.value,
                distance = uwbPosition.value?.distance?.value ?: 0f,
                azimuth = uwbPosition.value?.azimuth?.value ?: 0f
            )
        }
    }
}