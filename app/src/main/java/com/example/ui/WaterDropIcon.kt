package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

@Composable
fun WaterDropIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.08f)
            cubicTo(
                size.width * 0.48f, size.height * 0.08f,
                size.width * 0.42f, size.height * 0.17f,
                size.width * 0.33f, size.height * 0.29f
            )
            cubicTo(
                size.width * 0.25f, size.height * 0.42f,
                size.width * 0.21f, size.height * 0.52f,
                size.width * 0.21f, size.height * 0.63f
            )
            cubicTo(
                size.width * 0.21f, size.height * 0.79f,
                size.width * 0.34f, size.height * 0.92f,
                size.width * 0.5f, size.height * 0.92f
            )
            cubicTo(
                size.width * 0.66f, size.height * 0.92f,
                size.width * 0.79f, size.height * 0.79f,
                size.width * 0.79f, size.height * 0.63f
            )
            cubicTo(
                size.width * 0.79f, size.height * 0.52f,
                size.width * 0.75f, size.height * 0.42f,
                size.width * 0.67f, size.height * 0.29f
            )
            cubicTo(
                size.width * 0.58f, size.height * 0.17f,
                size.width * 0.52f, size.height * 0.08f,
                size.width * 0.5f, size.height * 0.08f
            )
            close()
        }
        drawPath(path = path, color = tint)
    }
}
