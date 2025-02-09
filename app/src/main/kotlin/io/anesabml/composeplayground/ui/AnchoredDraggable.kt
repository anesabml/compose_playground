@file:OptIn(ExperimentalFoundationApi::class)

package io.anesabml.composeplayground.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.anesabml.composeplayground.ui.theme.ComposePlaygroundTheme
import kotlin.math.roundToInt

enum class BoxDraggableAnchors {
    Start, End
}

private const val ColumnCount = 3
private val RowHorizontalPadding = 8.dp

@Composable
fun AnchoredDraggable(modifier: Modifier = Modifier) {
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val dragState = remember {
        AnchoredDraggableState(
            initialValue = BoxDraggableAnchors.Start,
            positionalThreshold = { 10f },
            velocityThreshold = { 100f },
            snapAnimationSpec = tween(),
            decayAnimationSpec = decayAnimationSpec
        )
    }

    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = modifier
            .padding(16.dp)
    ) {
        val columnWidth = maxWidth / ColumnCount
        val horizontalSpacingPx = RowHorizontalPadding.toPx()

        val anchors = remember(density) {
            val columnWidthPx = columnWidth.toPx(density)
            val startOffset =
                maxWidth.toPx(density) - columnWidthPx - horizontalSpacingPx
            DraggableAnchors {
                BoxDraggableAnchors.Start at startOffset
                BoxDraggableAnchors.End at 0f
            }
        }

        SideEffect {
            dragState.updateAnchors(anchors)
        }

        Row(
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
                .offset {
                    IntOffset(
                        x = dragState.requireOffset().roundToInt(),
                        y = 0
                    )
                },
            horizontalArrangement = Arrangement.spacedBy(RowHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val dragIconSize = 16.dp
            val icon = when(dragState.settledValue) {
                BoxDraggableAnchors.Start -> Icons.AutoMirrored.Filled.KeyboardArrowLeft
                BoxDraggableAnchors.End -> Icons.AutoMirrored.Filled.KeyboardArrowRight
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(dragIconSize)
                    .anchoredDraggable(
                        dragState,
                        Orientation.Horizontal,
                    )
            )

            val availableWidth =
                this@BoxWithConstraints.maxWidth - (ColumnCount - 1) * RowHorizontalPadding - dragIconSize - RowHorizontalPadding
            val cellWidth = availableWidth / ColumnCount
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(cellWidth)
                    .background(Color.Red)
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(cellWidth)
                    .background(Color.Cyan)
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(cellWidth)
                    .background(Color.Yellow)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnchoredDraggablePreview() {
    ComposePlaygroundTheme {
        AnchoredDraggable()
    }
}
