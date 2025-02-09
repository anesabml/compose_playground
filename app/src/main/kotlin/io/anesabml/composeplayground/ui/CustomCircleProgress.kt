package io.anesabml.composeplayground.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath

@Composable
fun SquigglyCircleProgressBar(
    modifier: Modifier = Modifier,
) {
    val starPolygon = remember {
        RoundedPolygon.star(
            numVerticesPerRadius = 12,
            innerRadius = 2f / 3f,
            rounding = CornerRounding(1f / 6f)
        )
    }

    val circlePolygon = remember {
        RoundedPolygon.circle(
            numVertices = 12
        )
    }

    val morph = remember {
        Morph(starPolygon, circlePolygon)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "shape")
    val progress = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "progress"
    )

    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "rotation"
    )
    val pathMeasure = remember { PathMeasure() }
    var androidPath = remember { android.graphics.Path() }
    var composePath = remember { Path() }
    val destinationPath = remember { Path() }
    val matrix = remember { Matrix() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .drawWithCache {
                // TODO investigate performance hits if androidPath is not specified
                androidPath = morph.toPath(progress.value, androidPath)
                composePath = androidPath.asComposePath()
                matrix.reset()
                matrix.scale(size.minDimension / 2f, size.minDimension / 2f)
                composePath.transform(matrix)

                pathMeasure.setPath(composePath, false)
                destinationPath.reset()
                pathMeasure.getSegment(0f, pathMeasure.length * progress.value, destinationPath)

                onDrawBehind {
                    rotate(rotation.value) {
                        translate(size.width / 2f, size.height / 2f) {
                            val brush = Brush.sweepGradient(colors, Offset(0.5f, 0.5f))
                            drawPath(destinationPath, brush = brush, style = Stroke(16.dp.toPx(), cap = StrokeCap.Round))
                        }
                    }
                }
            }
    )
}

private val colors = listOf(
    Color(0xFF3FCEBC),
    Color(0xFF3CBCEB),
    Color(0xFF5F96E7),
    Color(0xFF816FE3),
    Color(0xFF9F5EE2),
    Color(0xFFBD4CE0),
    Color(0xFFDE589F),
    Color(0xFF3FCEBC),
)

@Preview(showBackground = true)
@Composable
fun PreviewSquigglyCircleProgressBar() {
    SquigglyCircleProgressBar()
}