package com.example.testtcgpocket.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.testtcgpocket.R

const val MIN_ROTATION = -5f
const val MAX_ROTATION = 5f
const val CARD_WIDTH_RATIO = 2.5f
const val CARD_HEIGHT_RATIO = 3.5f

@Composable
fun CardComponent(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var rotationOffset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { _, offsetChange, _ ->
        val (width, height) = with(density) {
            val ratio = CARD_WIDTH_RATIO / CARD_HEIGHT_RATIO
            val width = configuration.screenWidthDp.dp.toPx()
            val height = width / ratio
            width to height
        }

        val maxXOffset = (width / 4f)
        val minXOffset = -maxXOffset

        val maxYOffset = (height / 4f)
        val minYOffset = -maxYOffset

        dragOffset = Offset(
            x = (dragOffset.x + offsetChange.x).coerceIn(
                minimumValue = minXOffset,
                maximumValue = maxXOffset,
            ),
            y = (dragOffset.y + offsetChange.y).coerceIn(
                minimumValue = minYOffset,
                maximumValue = maxYOffset,
            ),
        )

        rotationOffset = Offset(
            x = -dragOffset.y.interpolateRange(
                initialRange = minYOffset..maxYOffset,
                finalRange = MIN_ROTATION..MAX_ROTATION,
            ),
            y = dragOffset.x.interpolateRange(
                initialRange = minXOffset..maxXOffset,
                finalRange = MIN_ROTATION..MAX_ROTATION,
            ),
        )
    }

    LaunchedEffect(transformState.isTransformInProgress) {
        if (!transformState.isTransformInProgress) {
            dragOffset = Offset.Zero
            rotationOffset = Offset.Zero
        }
    }

    val animatedRotationX by animateFloatAsState(
        targetValue = rotationOffset.x,
        label = "rotationX",
    )

    val animatedRotationY by animateFloatAsState(
        targetValue = rotationOffset.y,
        label = "rotationX",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black),
    ) {
        Image(
            painter = painterResource(id = R.drawable.card),
            contentScale = ContentScale.FillBounds,
            contentDescription = "card",
            modifier = Modifier
                .graphicsLayer(
                    rotationX = animatedRotationX,
                    rotationY = animatedRotationY,
                )
                .transformable(state = transformState)
                .align(alignment = Alignment.Center)
                .fillMaxSize()
                .aspectRatio(CARD_WIDTH_RATIO / CARD_HEIGHT_RATIO)
                .padding(32.dp)
                .clip(RoundedCornerShape(15.dp))
        )
        Column(
            modifier = Modifier.align(alignment = Alignment.BottomCenter)
        ) {
            Text(
                text = "Drag : x : ${dragOffset.x} | y : ${dragOffset.y}",
                color = Color.White,
            )
            Text(
                text = "Rotation : x : ${rotationOffset.x} | y : ${rotationOffset.y}",
                color = Color.White,
            )
        }
    }
}

fun Float.interpolateRange(
    initialRange: ClosedFloatingPointRange<Float>,
    finalRange: ClosedFloatingPointRange<Float>,
): Float {
    return finalRange.start + (this - initialRange.start) * (finalRange.endInclusive - finalRange.start) / (initialRange.endInclusive - initialRange.start)
}

@Preview
@Composable
private fun CardComponentPreview() {
    CardComponent()
}