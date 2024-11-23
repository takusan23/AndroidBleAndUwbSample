package io.github.takusan23.androidbleanduwbsample.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 自分と通信相手を点で表示する Canvas
 * https://github.com/android/connectivity-samples/blob/main/UwbRanging/app/src/main/java/com/google/apps/hellouwb/ui/home/HomeScreen.kt
 *
 * @param modifier [Modifier]
 * @param distance 距離
 * @param azimuth 角度
 * @param isInvert 動かす側の場合は反転する必要があるので
 */
@Composable
fun UwbPointCanvas(
    modifier: Modifier = Modifier,
    isInvert: Boolean,
    distance: Float,
    azimuth: Float
) {
    Canvas(modifier = modifier.border(1.dp, Color.Black)) {

        // 自分（isInvert した場合は相手）
        drawCircle(Color.Red, radius = 15.0f)

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