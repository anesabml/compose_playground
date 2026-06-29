package io.anesabml.composeplayground.ui

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

data class GridItemData(
    val id: Int,
    val color: Color,
    val col: Int,
    val row: Int,
    val colSpan: Int,
    val rowSpan: Int
)

fun itemsOverlap(a: GridItemData, b: GridItemData): Boolean {
    return !(a.col + a.colSpan - 1 < b.col ||
            a.col > b.col + b.colSpan - 1 ||
            a.row + a.rowSpan - 1 < b.row ||
            a.row > b.row + b.rowSpan - 1)
}

fun tryPushItems(
    items: List<GridItemData>,
    movedItem: GridItemData,
    newCol: Int,
    newRow: Int
): List<GridItemData>? {
    val result = items.map {
        if (it.id == movedItem.id) it.copy(col = newCol, row = newRow) else it
    }.toMutableList()
    val settledIds = mutableSetOf(movedItem.id)

    fun getOverlapsWithSettled(item: GridItemData): List<GridItemData> {
        return result.filter { it.id in settledIds && it.id != item.id && itemsOverlap(it, item) }
    }

    var hasOverlaps = true
    while (hasOverlaps) {
        val overlappingItem = result.find { it.id !in settledIds && getOverlapsWithSettled(it).isNotEmpty() }
        if (overlappingItem == null) {
            hasOverlaps = false
            continue
        }

        // Try to push this item to the right
        var pushed = false
        val originalCol = overlappingItem.col
        for (c in (originalCol + 1)..(MAX_COLUMN_COUNT - overlappingItem.colSpan + 1)) {
            val potential = overlappingItem.copy(col = c)
            if (getOverlapsWithSettled(potential).isEmpty()) {
                result[result.indexOfFirst { it.id == overlappingItem.id }] = potential
                settledIds.add(overlappingItem.id)
                pushed = true
                break
            }
        }

        if (!pushed) return null // Cannot push right anymore
    }

    return result
}

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

    val density = LocalDensity.current

    Grid(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged {
                val gapPx = with(density) { GRID_GAP.dp.toPx() }
                cellHeight = ((it.height + gapPx) / MAX_ROW_COUNT).toInt()
                cellWidth = ((it.width + gapPx) / MAX_COLUMN_COUNT).toInt()
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
            key(item.id) {
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
    val newItem = item.copy(
        col = targetCol,
        row = targetRow,
        colSpan = targetColSpan,
        rowSpan = targetRowSpan
    )
    return items.filter { it.id != item.id }.none { other ->
        itemsOverlap(newItem, other)
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

    var isDragging by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isDragging) 1.05f else 1f, label = "scale")
    val elevation by animateFloatAsState(if (isDragging) 8f else 0f, label = "elevation")

    var resizeDragOffset by remember { mutableStateOf(Offset.Zero) }
    var moveDragOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = moveDragOffset.x
                translationY = moveDragOffset.y
            }
            .shadow(elevation.dp, RoundedCornerShape(16.dp))
            .fillMaxSize()
            .background(color = item.color, shape = RoundedCornerShape(size = 16.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        moveDragOffset = Offset.Zero
                        isDragging = true
                    },
                    onDragEnd = {
                        moveDragOffset = Offset.Zero
                        isDragging = false
                    },
                    onDragCancel = {
                        moveDragOffset = Offset.Zero
                        isDragging = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        moveDragOffset += dragAmount

                        val colChange = (moveDragOffset.x / colStepPxUpdated).roundToInt()
                        val rowChange = (moveDragOffset.y / rowStepPxUpdated).roundToInt()

                        if (colChange != 0 || rowChange != 0) {
                            val newCol = (itemUpdated.col + colChange).coerceIn(
                                1,
                                MAX_COLUMN_COUNT - itemUpdated.colSpan + 1
                            )
                            val newRow = (itemUpdated.row + rowChange).coerceIn(
                                1,
                                MAX_ROW_COUNT - itemUpdated.rowSpan + 1
                            )

                            if (newCol != itemUpdated.col || newRow != itemUpdated.row) {
                                val pushResult = tryPushItems(itemsUpdated, itemUpdated, newCol, newRow)
                                if (pushResult != null) {
                                    val actualColChange = newCol - itemUpdated.col
                                    val actualRowChange = newRow - itemUpdated.row
                                    moveDragOffset = Offset(
                                        moveDragOffset.x - actualColChange * colStepPxUpdated,
                                        moveDragOffset.y - actualRowChange * rowStepPxUpdated
                                    )
                                    onUpdateItems(pushResult)
                                } else {
                                    val others = itemsUpdated.filter { it.id != itemUpdated.id }
                                    val overlapping = others.filter { other ->
                                        itemsOverlap(itemUpdated.copy(col = newCol, row = newRow), other)
                                    }

                                    if (overlapping.size == 1) {
                                        val other = overlapping.first()
                                        // Try to swap, can the other item move to our current position?
                                        val canOtherMoveToOurOldPos = canItemMoveTo(
                                            others.filter { it.id != other.id },
                                            other,
                                            itemUpdated.col,
                                            itemUpdated.row,
                                            other.colSpan,
                                            other.rowSpan
                                        )
                                        if (canOtherMoveToOurOldPos) {
                                            val actualColChange = newCol - itemUpdated.col
                                            val actualRowChange = newRow - itemUpdated.row
                                            moveDragOffset = Offset(
                                                moveDragOffset.x - actualColChange * colStepPxUpdated,
                                                moveDragOffset.y - actualRowChange * rowStepPxUpdated
                                            )
                                            onUpdateItems(itemsUpdated.map {
                                                when (it.id) {
                                                    itemUpdated.id -> it.copy(
                                                        col = newCol,
                                                        row = newRow
                                                    )

                                                    other.id -> it.copy(
                                                        col = itemUpdated.col,
                                                        row = itemUpdated.row
                                                    )

                                                    else -> it
                                                }
                                            })
                                        }
                                    }
                                }
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
                                }
                            }
                        }
                    )
                }
        )
    }
}
