package io.anesabml.composeplayground.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Glow(
    val radius: Dp = 8.dp,    // Controls glow size
    val xShifting: Dp = 0.dp, // Adjusts horizontal position
    val yShifting: Dp = 0.dp  // Adjusts vertical position
)

@Composable
fun InfiniteLoader(
    brush: Brush,
    modifier: Modifier = Modifier,
    duration: Int = 3_000,
    strokeWidth: Dp = 4.dp,
    strokeCap: StrokeCap = StrokeCap.Round,
    glow: Glow? = null,
    placeholderColor: Color? = null,
) {
    val infiniteTransition = rememberInfiniteTransition("PathTransition")
    val pathCompletion by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing)
        ),
        label = "Progress"
    )

    Canvas(modifier = modifier) {
        val path = createInfiniteShape(size.width, size.height)

        val paint = setupPaint(strokeWidth, strokeCap, brush)
        glow?.applyToPaint(paint, this)
        placeholderColor?.let { color -> drawPathPlaceholder(path, strokeWidth, strokeCap, color) }

        val pathSegment = calculatePathSegment(path, pathCompletion)
        drawPathSegment(pathSegment, paint)
    }
}

@Preview(showBackground = true)
@Composable
fun InfiniteLoaderPreview() {
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

private fun DrawScope.drawPathSegment(pathSegment: Path, paint: Paint) {
    drawIntoCanvas { canvas -> canvas.drawPath(pathSegment, paint) }
}

private fun DrawScope.drawPathPlaceholder(
    path: Path,
    strokeWidth: Dp,
    strokeCap: StrokeCap,
    placeholderColor: Color
) {
    drawPath(
        path = path,
        color = placeholderColor,
        style = Stroke(
            width = strokeWidth.toPx(),
            cap = strokeCap
        )
    )
}

private fun Glow.applyToPaint(
    paint: Paint,
    density: Density
) = with(density) {
    val frameworkPaint = paint.asFrameworkPaint()
    frameworkPaint.setShadowLayer(
        radius.toPx(),
        xShifting.toPx(),
        yShifting.toPx(),
        android.graphics.Color.WHITE
    )
}

private fun DrawScope.setupPaint(
    strokeWidth: Dp,
    strokeCap: StrokeCap,
    brush: Brush
): Paint = Paint().apply {
    this.isAntiAlias = true
    this.style = PaintingStyle.Stroke
    this.strokeWidth = strokeWidth.toPx()
    this.strokeCap = strokeCap

    brush.applyTo(size, this, 1f)
}

private fun calculatePathSegment(path: Path, pathCompletion: Float): Path {
    val pathMeasure = PathMeasure().apply { setPath(path, false) }

    val startDistance = when {
        (pathCompletion > 1f) -> ((pathCompletion - 1f) * pathMeasure.length)
        else -> 0f
    }

    val stopDistance = when {
        (pathCompletion < 1f) -> (pathCompletion * pathMeasure.length)
        else -> pathMeasure.length
    }

    val pathSegment = Path()
    pathMeasure.getSegment(startDistance, stopDistance, pathSegment, true)
    return pathSegment
}

private fun createInfiniteShape(width: Float, height: Float): Path =
    Path().apply {
        moveTo((width / 2), (height / 2))
        cubicTo(
            x1 = width, y1 = 0f,
            x2 = width, y2 = height,
            x3 = (width / 2), y3 = (height / 2)
        )
        cubicTo(
            x1 = 0f, y1 = 0f,
            x2 = 0f, y2 = height,
            x3 = (width / 2), (height / 2)
        )
    }
