package com.example.testtcgpocket.ui

import android.os.Build
import android.view.View
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.testtcgpocket.R
import kotlin.math.abs

const val ROTATION_X = 5f
const val ROTATION_Y = 10f
const val ROTATION_Z = 2.5f
const val CARD_WIDTH_RATIO = 2.5f
const val CARD_HEIGHT_RATIO = 3.5f
const val OVERLAY_JITTER = 100f

private data class Rotation(
    val x: Float,
    val y: Float,
    val z: Float,
) {
    companion object {
        @Stable
        val Zero = Rotation(x = 0.0f, y = 0.0f, z = 0.0f)
    }
}

@Composable
fun CardComponent(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var rotation by remember { mutableStateOf(Rotation.Zero) }
    val transformState = rememberTransformableState { _, offsetChange, _ ->
        val (width, height) = with(density) {
            val ratio = CARD_WIDTH_RATIO / CARD_HEIGHT_RATIO
            val width = configuration.screenWidthDp.dp.toPx()
            val height = width / ratio
            width to height
        }

        val maxXOffset = (width / 8f)
        val minXOffset = -maxXOffset

        val maxYOffset = (height / 8f)
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

        rotation = Rotation(
            x = -dragOffset.y.interpolateRange(
                initialRange = minYOffset..maxYOffset,
                finalRange = -ROTATION_X..ROTATION_X,
            ),
            y = dragOffset.x.interpolateRange(
                initialRange = minXOffset..maxXOffset,
                finalRange = -ROTATION_Y..ROTATION_Y,
            ),
            z = (-rotation.x * rotation.y).coerceIn(
                minimumValue = -(ROTATION_X * ROTATION_Y),
                maximumValue = ROTATION_X * ROTATION_Y,
            ).interpolateRange(
                initialRange = -(ROTATION_X * ROTATION_Y)..(ROTATION_X * ROTATION_Y),
                finalRange = -ROTATION_Z..ROTATION_Z,
            ),
        )
    }

    LaunchedEffect(transformState.isTransformInProgress) {
        if (!transformState.isTransformInProgress) {
            dragOffset = Offset.Zero
            rotation = Rotation.Zero
        }
    }

    val animatedRotationX by animateFloatAsState(
        targetValue = rotation.x,
        label = "rotationX",
    )

    val animatedRotationY by animateFloatAsState(
        targetValue = rotation.y,
        label = "rotationY",
    )

    val animatedRotationZ by animateFloatAsState(
        targetValue = rotation.z,
        label = "rotationZ",
    )

    val overlay = ImageBitmap.imageResource(id = R.drawable.overlay)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black),
    ) {
        CardView {
            Image(
                painter = painterResource(id = R.drawable.card),
                contentScale = ContentScale.FillBounds,
                contentDescription = "card",
                modifier = Modifier
                    .graphicsLayer(
                        rotationX = animatedRotationX,
                        rotationY = animatedRotationY,
                        rotationZ = animatedRotationZ,
                    )
                    .transformable(state = transformState)
                    .align(alignment = Alignment.Center)
                    .fillMaxSize()
                    .padding(64.dp)
                    .aspectRatio(CARD_WIDTH_RATIO / CARD_HEIGHT_RATIO)
                    .clip(RoundedCornerShape(15.dp))
                    .drawWithCache {
                        onDrawWithContent {
                            drawContent()
                            val absoluteRotationX = abs(animatedRotationX)
                            val absoluteRotationY = abs(animatedRotationY)

                            val overlayAlpha =
                                (absoluteRotationX + absoluteRotationY).interpolateRange(
                                    initialRange = 0f..ROTATION_X + ROTATION_Y,
                                    finalRange = 0f..1f
                                )

                            val xOffset = animatedRotationX
                                .interpolateRange(
                                    initialRange = -ROTATION_X..ROTATION_X,
                                    finalRange = -OVERLAY_JITTER..OVERLAY_JITTER
                                )
                                .toInt()
                            val yOffset = animatedRotationY
                                .interpolateRange(
                                    initialRange = -ROTATION_Y..ROTATION_Y,
                                    finalRange = -OVERLAY_JITTER..OVERLAY_JITTER
                                )
                                .toInt()

                            val overlayOffset = IntOffset(
                                x = xOffset - OVERLAY_JITTER.toInt(),
                                y = yOffset - OVERLAY_JITTER.toInt(),
                            )

                            drawImage(
                                image = overlay,
                                alpha = overlayAlpha,
                                dstOffset = overlayOffset,
                                dstSize = IntSize(
                                    width = size.width.toInt() + OVERLAY_JITTER.toInt() * 2,
                                    height = size.height.toInt() + OVERLAY_JITTER.toInt() * 2,
                                ),
                                blendMode = BlendMode.Overlay,
                            )
                        }
                    }
            )
        }

        Column(
            modifier = Modifier.align(alignment = Alignment.BottomCenter)
        ) {
            Text(
                text = "Drag : x : ${dragOffset.x} | y : ${dragOffset.y}",
                color = Color.White,
            )
            Text(
                text = "Rotation : x : ${rotation.x} | y : ${rotation.y} | z : ${rotation.z}",
                color = Color.White,
            )
        }
    }
}

/**
 * Disables hardware acceleration on API below 28 due to limitations rendering the OVERLAY blend mode.
 */
@Composable
private fun CardView(content: @Composable () -> Unit) {
    AndroidView(
        factory = { context ->
            ComposeView(context).apply {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                }
                setContent(content)
            }
        },
    )
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