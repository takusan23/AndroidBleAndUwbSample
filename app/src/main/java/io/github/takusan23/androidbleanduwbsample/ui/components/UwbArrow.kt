package io.github.takusan23.androidbleanduwbsample.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp

/** UWB の方角を表示する矢印 */
@Composable
fun UwbArrow(
    modifier: Modifier = Modifier,
    azimuth: Float
) {
    val animateAzimuth = animateFloatAsState(azimuth, label = "animateAzimuth")

    Box(
        modifier = modifier.graphicsLayer {
            rotationZ = animateAzimuth.value
        },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "↑",
            fontSize = 100.sp
        )
    }
}