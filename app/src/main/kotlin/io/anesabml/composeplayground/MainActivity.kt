package io.anesabml.composeplayground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.anesabml.composeplayground.ui.Glow
import io.anesabml.composeplayground.ui.InfiniteLoader
import io.anesabml.composeplayground.ui.SquigglyCircleProgressBar
import io.anesabml.composeplayground.ui.theme.ComposePlaygroundTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposePlaygroundTheme {
                Scaffold { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        item {
                            InfiniteLoader(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.Red, Color.Blue)
                                ),
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(150.dp),
                                glow = Glow(),
                                placeholderColor = Color.Black.copy(.16f)
                            )
                        }

                        item {
                            SquigglyCircleProgressBar(
                                modifier = Modifier
                                    .size(200.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
