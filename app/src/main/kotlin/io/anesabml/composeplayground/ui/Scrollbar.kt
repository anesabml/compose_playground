package io.anesabml.composeplayground.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Preview(showBackground = true)
@Composable
fun FastScrollDemo() {
    val items = (1..500).map { "Item $it" }
    val listState = rememberLazyListState()
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(state = listState) {
            items(items.size) { index ->
                Text(
                    text = items[index],
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
            }
        }
        val scrollbarState = listState.rememberScrollbarState(
            itemsAvailable = items.size,
        )
        val draggableScroller = listState.rememberDraggableScroller(
            itemsAvailable = items.size,
        )
        DraggableScrollbar(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight()
                .padding(horizontal = 4.dp)
                .align(Alignment.TopEnd),
            state = scrollbarState,
            listState = listState,
            onThumbMoved = draggableScroller,
        )
    }
}

private enum class ThumbState {
    Active,   // User is dragging, pressing, or list is scrolling
    Dormant,  // Content is scrollable but idle (subtle hint)
    Inactive, // Content fits on screen, thumb is hidden
}

@Composable
private fun ScrollableState.scrollbarThumbColor(
    interactionSource: InteractionSource,
): Color {
    var state by remember { mutableStateOf(ThumbState.Inactive) }
    val canScroll = canScrollBackward || canScrollForward
    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val dragged by interactionSource.collectIsDraggedAsState()
    val active = canScroll && (pressed || hovered || dragged || isScrollInProgress)
    LaunchedEffect(active) {
        state = when {
            active -> ThumbState.Active
            canScroll -> ThumbState.Dormant
            else -> ThumbState.Inactive
        }
    }
    val color by animateColorAsState(
        targetValue = when (state) {
            ThumbState.Active -> Color.DarkGray.copy(alpha = 0.5f)
            ThumbState.Dormant -> Color.DarkGray.copy(alpha = 0.2f)
            ThumbState.Inactive -> Color.Transparent
        },
        animationSpec = SpringSpec(stiffness = Spring.StiffnessLow),
        label = "thumb color",
    )
    return color
}

@Composable
fun DraggableScrollbar(
    modifier: Modifier = Modifier,
    state: ScrollbarState,
    listState: LazyListState,
    onThumbMoved: (Float) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Scrollbar(
        modifier = modifier,
        state = state,
        interactionSource = interactionSource,
        thumb = {
            val isDragged by interactionSource.collectIsDraggedAsState()
            val color by animateColorAsState(
                targetValue = if (isDragged || listState.isScrollInProgress) {
                    Color.DarkGray.copy(alpha = 0.5f)
                } else {
                    Color.Transparent
                },
                animationSpec = SpringSpec(stiffness = Spring.StiffnessLow),
                label = "thumb color",
            )
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(
                        color = listState.scrollbarThumbColor(interactionSource)
                    ),
            )
        },
        onThumbMoved = onThumbMoved,
    )
}

@Composable
fun Scrollbar(
    modifier: Modifier = Modifier,
    state: ScrollbarState,
    minThumbSize: Dp = 40.dp,
    interactionSource: MutableInteractionSource? = null,
    thumb: @Composable () -> Unit,
    onThumbMoved: ((Float) -> Unit)? = null,
) {
    val localDensity = LocalDensity.current
    val currentState by rememberUpdatedState(state)
    var draggedOffset by remember { mutableStateOf(Offset.Unspecified) }
    var interactionThumbTravelPercent by remember { mutableFloatStateOf(Float.NaN) }
    var trackSize by remember { mutableFloatStateOf(0f) }
    val thumbTravelPercent = when {
        interactionThumbTravelPercent.isNaN() -> state.thumbMovedPercent
        else -> interactionThumbTravelPercent
    }
    val thumbSizePx = max(
        a = state.thumbSizePercent * trackSize,
        b = with(localDensity) { minThumbSize.toPx() },
    )
    val thumbSizeDp by animateDpAsState(
        targetValue = with(localDensity) { thumbSizePx.toDp() },
        label = "thumb size",
    )
    val thumbMovedPx = min(
        a = trackSize * thumbTravelPercent,
        b = trackSize - thumbSizePx,
    )
    // Track
    Box(
        modifier = modifier
            .fillMaxHeight()
            .onGloballyPositioned { coordinates ->
                trackSize = coordinates.size.height.toFloat()
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val dragInteraction = DragInteraction.Start()
                    interactionSource?.tryEmit(dragInteraction)
                    // Anchor to the thumb's current position so it does not
                    // jump to the touch location on first contact.
                    val currentThumbOffset =
                        currentState.thumbMovedPercent * trackSize
                    draggedOffset = Offset(down.position.x, currentThumbOffset)
                    val dragResult = drag(down.id) { change ->
                        draggedOffset = draggedOffset.copy(
                            y = draggedOffset.y + change.position.y
                                    - change.previousPosition.y,
                        )
                        change.consume()
                    }
                    val interaction = if (dragResult)
                        DragInteraction.Stop(dragInteraction)
                    else DragInteraction.Cancel(dragInteraction)
                    interactionSource?.tryEmit(interaction)
                    draggedOffset = Offset.Unspecified
                }
            },
    ) {
        val thumbOffsetDp = androidx.compose.ui.unit.max(
            a = with(localDensity) { thumbMovedPx.toDp() },
            b = 0.dp,
        )
        // Thumb
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .height(thumbSizeDp)
                .offset(y = thumbOffsetDp),
        ) {
            thumb()
        }
    }
    if (onThumbMoved == null) return
    // Process drag position into a 0..1 travel percentage
    LaunchedEffect(draggedOffset) {
        if (draggedOffset == Offset.Unspecified) {
            interactionThumbTravelPercent = Float.NaN
            return@LaunchedEffect
        }
        if (trackSize <= 0f) return@LaunchedEffect
        val currentTravel = (draggedOffset.y / trackSize).coerceIn(0f, 1f)
        onThumbMoved(currentTravel)
        interactionThumbTravelPercent = currentTravel
    }
}

data class ScrollbarState(
    val thumbSizePercent: Float,
    val thumbMovedPercent: Float,
) {
    companion object {
        val FULL = ScrollbarState(
            1f,
            0f,
        )
    }
}

@Composable
fun LazyListState.rememberScrollbarState(
    itemsAvailable: Int,
): ScrollbarState {
    var state by remember { mutableStateOf(ScrollbarState.FULL) }
    LaunchedEffect(this, itemsAvailable) {
        snapshotFlow {
            if (itemsAvailable == 0) return@snapshotFlow null
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@snapshotFlow null
            val firstItem = visibleItems.first()
            if (firstItem.size == 0) return@snapshotFlow null

            val offsetPercent = abs(firstItem.offset.toFloat()) / firstItem.size
            val firstIndex = min(
                firstItem.index + offsetPercent,
                itemsAvailable.toFloat()
            )
            val itemsVisible = visibleItems.sumOf { item ->
                itemVisibilityPercentage(
                    itemSize = item.size,
                    itemStartOffset = item.offset,
                    viewportStartOffset = layoutInfo.viewportStartOffset,
                    viewportEndOffset = layoutInfo.viewportEndOffset,
                ).toDouble()
            }.toFloat()
            ScrollbarState(
                thumbSizePercent = min(itemsVisible / itemsAvailable, 1f),
                thumbMovedPercent = min(firstIndex / itemsAvailable, 1f),
            )
        }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { state = it }
    }
    return state
}

private fun itemVisibilityPercentage(
    itemSize: Int,
    itemStartOffset: Int,
    viewportStartOffset: Int,
    viewportEndOffset: Int,
): Float {
    if (itemSize == 0) return 0f
    val itemEnd = itemStartOffset + itemSize
    val startOffset = when {
        itemStartOffset > viewportStartOffset -> 0
        else -> abs(abs(viewportStartOffset) - abs(itemStartOffset))
    }
    val endOffset = when {
        itemEnd < viewportEndOffset -> 0
        else -> abs(abs(itemEnd) - abs(viewportEndOffset))
    }
    val size = itemSize.toFloat()
    return (size - startOffset - endOffset) / size
}

@Composable
fun LazyListState.rememberDraggableScroller(
    itemsAvailable: Int,
): (Float) -> Unit {
    var percentage by remember { mutableFloatStateOf(Float.NaN) }
    val itemCount by rememberUpdatedState(itemsAvailable)
    LaunchedEffect(percentage) {
        if (percentage.isNaN()) return@LaunchedEffect
        val indexToFind = (itemCount * percentage).toInt()
        scrollToItem(indexToFind)
    }
    return remember {
        { newPercentage: Float -> percentage = newPercentage }
    }
}
