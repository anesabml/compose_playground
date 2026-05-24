package io.anesabml.composeplayground.ui

import android.util.Log
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

data class GridItemData(
    val id: Int,
    val color: Color,
    val col: Int,
    val row: Int,
    val colSpan: Int,
    val rowSpan: Int
)

private const val MAX_COLUMN_COUNT = 3
private const val MAX_ROW_COUNT = 5
private const val GRID_GAP = 24

@OptIn(ExperimentalGridApi::class)
@Preview(widthDp = 1550)
@Composable
fun CustomizableGrid() {
    var items by remember {
        mutableStateOf(
            listOf(
                GridItemData(1, Color.Red, 1, 1, 1, 2),
                GridItemData(2, Color.Green, 1, 3, 1, 1),
                GridItemData(3, Color.Yellow, 2, 1, 1, 3)
            )
        )
    }
    var cellWidth by remember { mutableIntStateOf(0) }
    var cellHeight by remember { mutableIntStateOf(0) }

    Grid(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged {
                cellHeight = it.height / MAX_ROW_COUNT
                cellWidth = it.width / MAX_COLUMN_COUNT
            }
            .background(Color.Magenta),
        config = {
            if (cellWidth != 0) {
                repeat(MAX_COLUMN_COUNT) {
//                    column(1.fr)
                    column(
                        (
                                cellWidth.toDp() * MAX_COLUMN_COUNT - GRID_GAP.dp * (MAX_COLUMN_COUNT - 1)
                                )
                                / MAX_COLUMN_COUNT
                    )
                }
            }
            if (cellHeight != 0) {
                repeat(MAX_ROW_COUNT) {
//                row(1.fr)
                    row(
                        (
                                cellHeight.toDp() * MAX_ROW_COUNT - GRID_GAP.dp * (MAX_ROW_COUNT - 1)
                                )
                                / MAX_ROW_COUNT
                    )
                }
            }
            gap(GRID_GAP.dp)
            flow = GridFlow.Column
        }
    ) {

        items.forEach { item ->
            ColoredBox(
                items = items,
                item = item,
                cellHeight = cellHeight,
                cellWidth = cellWidth,
                onUpdateItems = {
                    items = it
                },
                modifier = Modifier
                    .gridItem(
                        row = item.row,
                        column = item.col,
                        rowSpan = item.rowSpan,
                        columnSpan = item.colSpan
                    )
            )
        }
    }
}

fun canItemMoveTo(
    items: List<GridItemData>,
    item: GridItemData,
    targetCol: Int,
    targetRow: Int,
    targetColSpan: Int,
    targetRowSpan: Int
): Boolean {
    if (
        targetCol < 1 || targetRow < 1 ||
        targetCol + targetColSpan - 1 > MAX_COLUMN_COUNT ||
        targetRow + targetRowSpan - 1 > MAX_ROW_COUNT
    ) {
        return false
    }
    return items.filter { it.id != item.id }.none { other ->
        !(targetCol + targetColSpan - 1 < other.col ||
                targetCol > other.col + other.colSpan - 1 ||
                targetRow + targetRowSpan - 1 < other.row ||
                targetRow > other.row + other.rowSpan - 1)
    }
}

@Composable
private fun ColoredBox(
    items: List<GridItemData>,
    item: GridItemData,
    cellHeight: Int,
    cellWidth: Int,
    onUpdateItems: (List<GridItemData>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colStepPxUpdated by rememberUpdatedState(cellWidth)
    val rowStepPxUpdated by rememberUpdatedState(cellHeight)
    val itemUpdated by rememberUpdatedState(item)
    val itemsUpdated by rememberUpdatedState(items)

    var resizeDragOffset by remember { mutableStateOf(Offset.Zero) }
    var moveDragOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier
            .fillMaxSize()
            .background(color = item.color, shape = RoundedCornerShape(size = 16.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { moveDragOffset = Offset.Zero },
                    onDragEnd = { moveDragOffset = Offset.Zero },
                    onDragCancel = { moveDragOffset = Offset.Zero },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        moveDragOffset += dragAmount

                        val colChange = (moveDragOffset.x / colStepPxUpdated).roundToInt()
                        val rowChange = (moveDragOffset.y / rowStepPxUpdated).roundToInt()

                        if (colChange != 0 || rowChange != 0) {
                            val newCol = itemUpdated.col + colChange
                            val newRow = itemUpdated.row + rowChange
                            val canItemMoveTo =
                                canItemMoveTo(
                                    itemsUpdated,
                                    itemUpdated,
                                    newCol,
                                    newRow,
                                    itemUpdated.colSpan,
                                    itemUpdated.rowSpan
                                )
                            Log.d(
                                "CustomizableGrid",
                                "CustomizableGrid: $newCol, $newRow, $canItemMoveTo"
                            )
                            if (canItemMoveTo) {
                                moveDragOffset = Offset(
                                    moveDragOffset.x - colChange * colStepPxUpdated,
                                    moveDragOffset.y - rowChange * rowStepPxUpdated
                                )
                                onUpdateItems(itemsUpdated.map {
                                    if (it.id == itemUpdated.id) it.copy(
                                        col = newCol,
                                        row = newRow
                                    ) else it
                                }
                                )
                            } else {
//                                Toast.makeText(context, "No enough space", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
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
                        onDragStart = { resizeDragOffset = Offset.Zero },
                        onDragEnd = { resizeDragOffset = Offset.Zero },
                        onDragCancel = { resizeDragOffset = Offset.Zero },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            resizeDragOffset += dragAmount

                            val colChange = (resizeDragOffset.x / colStepPxUpdated).roundToInt()
                            val rowChange = (resizeDragOffset.y / rowStepPxUpdated).roundToInt()

                            if (colChange != 0 || rowChange != 0) {
                                val newColSpan = (itemUpdated.colSpan + colChange).coerceAtLeast(1)
                                val newRowSpan = (itemUpdated.rowSpan + rowChange).coerceAtLeast(1)
                                val canItemMoveTo = canItemMoveTo(
                                    itemsUpdated,
                                    itemUpdated,
                                    itemUpdated.col,
                                    itemUpdated.row,
                                    newColSpan,
                                    newRowSpan
                                )
                                Log.d(
                                    "CustomizableGrid",
                                    "Resize: $newColSpan, $newRowSpan, $resizeDragOffset, $canItemMoveTo"
                                )
                                if (canItemMoveTo) {
                                    resizeDragOffset = Offset(
                                        resizeDragOffset.x - colChange * colStepPxUpdated,
                                        resizeDragOffset.y - rowChange * rowStepPxUpdated
                                    )
                                    onUpdateItems(itemsUpdated.map {
                                        if (it.id == itemUpdated.id) it.copy(
                                            colSpan = newColSpan,
                                            rowSpan = newRowSpan
                                        ) else it
                                    })

                                } else {
//                                    Toast.makeText(context, "No enough space", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
        )
    }
}
