package io.anesabml.composeplayground.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.core.ScrollArea
import com.composables.core.Thumb
import com.composables.core.VerticalScrollbar
import com.composables.core.rememberScrollAreaState
import io.anesabml.composeplayground.ui.theme.ComposePlaygroundTheme
import kotlin.random.Random

@Composable
fun ScrollArea(modifier: Modifier = Modifier) {
    val lazyListState = rememberLazyListState()
    val state = rememberScrollAreaState(lazyListState)

    ScrollArea(
        state = state,
        modifier = modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize()
        ) {
            repeat(50) {
                item {
                    Box(
                        Modifier.height(Random.nextInt(50, 80).dp)
                    ) {
                        Text(text = "Item $it")
                    }
                }
            }
        }

        VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxHeight()
                .width(4.dp)
        ) {
            Thumb(Modifier.background(MaterialTheme.colorScheme.tertiary))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScrollAreaPreview() {
    ComposePlaygroundTheme {
        ScrollArea()
    }
}
