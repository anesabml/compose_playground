package io.anesabml.composeplayground.ui

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import io.anesabml.composeplayground.ui.theme.ComposePlaygroundTheme

@Composable
fun MarqueeText(modifier: Modifier = Modifier) {
    Text(
        text = "نص طوييل جداااا",
        modifier = modifier.width(80.dp),
        maxLines = 1,
        textAlign = TextAlign.Right
    )
}

@Preview(showBackground = true)
@Composable
fun MarqueeTextPreview() {
    ComposePlaygroundTheme {
        MarqueeText(modifier = Modifier.width(80.dp))
    }
}