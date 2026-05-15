package io.anesabml.composeplayground.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridFlow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalGridApi::class)
@Preview(widthDp = 1550)
@Composable
fun CustomizableGrid() {
    var redSpan by remember { mutableStateOf(1 to 2) }
    var greenSpan by remember { mutableStateOf(1 to 1) }
    var yellowSpan by remember { mutableStateOf(1 to 1) }

    Grid(
        config = {
            repeat(5) {
                column(200.dp)
            }
            repeat(6) {
                row(100.dp)
            }
            gap(24.dp)
            flow = GridFlow.Column
        }
    ) {
        ColoredBox(
            modifier = Modifier
                .gridItem(rowSpan = redSpan.second, columnSpan = redSpan.first),
            color = Color.Red,
            columnSpan = redSpan.first,
            rowSpan = redSpan.second,
            onSpanChange = { redSpan = it }
        )
        ColoredBox(
            modifier = Modifier
                .gridItem(rowSpan = greenSpan.second, columnSpan = greenSpan.first),
            color = Color.Green,
            columnSpan = greenSpan.first,
            rowSpan = greenSpan.second,
            onSpanChange = { greenSpan = it }
        )
        ColoredBox(
            modifier = Modifier
                .gridItem(rowSpan = yellowSpan.second, columnSpan = yellowSpan.first),
            color = Color.Yellow,
            columnSpan = yellowSpan.first,
            rowSpan = yellowSpan.second,
            onSpanChange = { yellowSpan = it }
        )
    }
}

@Composable
private fun ColoredBox(
    modifier: Modifier = Modifier,
    color: Color,
    columnSpan: Int,
    rowSpan: Int,
    onSpanChange: (Pair<Int, Int>) -> Unit
) {
    val density = LocalDensity.current
    val colStepPx = with(density) { (200.dp + 24.dp).toPx() }
    val rowStepPx = with(density) { (100.dp + 24.dp).toPx() }

    val rowSpanUpdated by rememberUpdatedState(rowSpan)
    val columnSpanUpdated by rememberUpdatedState(columnSpan)
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    Box(
        modifier
            .fillMaxSize()
            .background(color = color, shape = RoundedCornerShape(size = 16.dp))
    ) {
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .size(32.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 16.dp, bottomEnd = 16.dp)
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { dragOffset = Offset.Zero },
                        onDragEnd = { dragOffset = Offset.Zero },
                        onDragCancel = { dragOffset = Offset.Zero },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount

                            val colChange = (dragOffset.x / colStepPx).roundToInt()
                            val rowChange = (dragOffset.y / rowStepPx).roundToInt()

                            if (colChange != 0 || rowChange != 0) {
                                val newColSpan = (columnSpanUpdated + colChange).coerceAtLeast(1)
                                val newRowSpan = (rowSpanUpdated + rowChange).coerceAtLeast(1)

                                if (newColSpan != columnSpanUpdated || newRowSpan != rowSpanUpdated) {
                                    onSpanChange(newColSpan to newRowSpan)
                                    dragOffset = Offset(
                                        dragOffset.x - colChange * colStepPx,
                                        dragOffset.y - rowChange * rowStepPx
                                    )
                                }
                            }
                        }
                    )
                }
        )
    }
}
