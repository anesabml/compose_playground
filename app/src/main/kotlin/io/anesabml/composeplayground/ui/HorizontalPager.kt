package io.anesabml.composeplayground.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

@Composable
fun rememberLimitedFlingPagerBehavior(
    pagerState: PagerState,
    maxPagesToFling: Int = 1
): TargetedFlingBehavior {
    // Get the default fling behavior from PagerDefaults.
    // We will use its internal logic if we decide not to override.
    val defaultPagerFlingBehavior = PagerDefaults.flingBehavior(state = pagerState)

    return remember(pagerState, defaultPagerFlingBehavior, maxPagesToFling) {
        object : TargetedFlingBehavior {
            override suspend fun ScrollScope.performFling(
                initialVelocity: Float,
                onRemainingDistanceUpdated: (Float) -> Unit // New parameter in recent versions
            ): Float {
                val startPage = pagerState.currentPage
                val decayDistance = calculateDecayDistance(initialVelocity, pagerState.layoutInfo.pageSize.toFloat()) // Approximate

                // Predict the target page based on initial velocity and some decay logic
                // This is a simplified prediction. Pager's internal logic is more complex.
                val projectedTargetPage = if (abs(initialVelocity) > 0) {
                    // A simple heuristic: estimate how many pages the fling might cover
                    // This could be improved with a more accurate decay model
                    val pagesToFling = (decayDistance / pagerState.layoutInfo.pageSize).roundToInt() * initialVelocity.sign
                    (startPage + pagesToFling).roundToInt()
                } else {
                    startPage
                }

                val constrainedTargetPage = if (initialVelocity > 0) { // Flinging forward
                    minOf(projectedTargetPage, startPage + maxPagesToFling)
                } else if (initialVelocity < 0) { // Flinging backward
                    maxOf(projectedTargetPage, startPage - maxPagesToFling)
                } else {
                    startPage
                }.coerceIn(0, pagerState.pageCount - 1)

                Log.d("LimitedFling", "InitialV: $initialVelocity, Start: $startPage, Projected: $projectedTargetPage, Constrained: $constrainedTargetPage")

                if (constrainedTargetPage != startPage || abs(pagerState.currentPageOffsetFraction) > 0.001f) {
                    // If we are flinging to a new page or need to settle the current one
                    pagerState.animateScrollToPage(
                        page = constrainedTargetPage,
                        // animationSpec = tween(durationMillis = 300) // Adjust as needed
                    )
                    // Since animateScrollToPage consumes the fling, we return the initial velocity
                    // to indicate it has been handled.
                    return initialVelocity
                }

                // If no real fling or already settled, let the default behavior handle it (e.g., for small adjustments)
                // Or, if we are not actually moving, consume no velocity.
                return 0f // No velocity consumed if no action taken
            }
        }
    }
}

// Helper to estimate decay distance - this is a placeholder and might need adjustment
// based on the actual scroll physics used by Pager.
// For a more accurate version, you'd look into Android's Scroller or OverScroller physics.
private fun calculateDecayDistance(velocity: Float, itemSize: Float): Float {
    // This is a very rough approximation.
    // A proper calculation involves friction and other parameters.
    // The actual decay used by Compose might be different.
    // For simplicity, let's assume velocity is pixels/second and we want a rough idea.
    // This factor is arbitrary and needs tuning if a precise prediction is required.
    val arbitraryFactor = 0.3f
    return velocity * arbitraryFactor
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SimpleHorizontalPager(modifier: Modifier = Modifier) {
    val pageCount = 10
    val pagerState = rememberPagerState(pageCount = { pageCount })

    // Use the custom fling behavior
    val limitedFlingBehavior = rememberLimitedFlingPagerBehavior(
        pagerState = pagerState,
        maxPagesToFling = 1 // Ensure only one page can be flung at a time
    )

    Box(
        modifier = modifier
            .size(300.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize(),
            flingBehavior = limitedFlingBehavior, // Apply the custom fling behavior
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red),
                contentAlignment = Alignment.Center
            ) {
                Text("Page: $page", fontSize = 24.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HorizontalPagerPreview() {
    SimpleHorizontalPager()
}