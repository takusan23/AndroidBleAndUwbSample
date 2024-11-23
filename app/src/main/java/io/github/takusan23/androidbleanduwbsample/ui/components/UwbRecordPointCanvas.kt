package io.github.takusan23.androidbleanduwbsample.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class PointData(val distance: Float, val azimuth: Float)

@Composable
fun UwbRecordPointCanvas(
    modifier: Modifier = Modifier,
    isInvert: Boolean,
    distance: Float,
    azimuth: Float
) {
    val isRecord = remember { mutableStateOf(false) }
    val recordList = remember { mutableStateOf(emptyList<PointData>()) }

    if (isRecord.value) {
        SideEffect {
            // 数が多いので適当に捨てる
            if (Random.nextBoolean()) {
                recordList.value += PointData(distance, azimuth)
            }
        }
    }

    Column {
        Row {
            Button(onClick = { isRecord.value = !isRecord.value }) {
                Text(text = if (!isRecord.value) "記録開始" else "終了")
            }
            Button(onClick = { recordList.value = emptyList() }) {
                Text(text = "クリア")
            }
        }
        Canvas(modifier = modifier.border(1.dp, Color.Black)) {

            // 自分（isInvert した場合は相手）
            drawCircle(Color.Red, radius = 15.0f)

            // 配列に入れたものを表示
            recordList.value.forEach { (distance, azimuth) ->

                val scale = size.minDimension / 20.0f
                val angle = azimuth * PI / 180
                val x = distance * sin(angle).toFloat()
                val y = distance * cos(angle).toFloat()

                // UWB デバイスの位置
                drawCircle(
                    center = center.plus(
                        if (isInvert) {
                            Offset(-x * scale, y * scale)
                        } else {
                            Offset(x * scale, -y * scale)
                        }
                    ),
                    color = Color.Blue,
                    radius = 15.0f
                )
            }
        }
    }
}