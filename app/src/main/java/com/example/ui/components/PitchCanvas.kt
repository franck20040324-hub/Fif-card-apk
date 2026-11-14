package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.PitchTheme

@Composable
fun FootballPitchCanvas(
    pitchTheme: PitchTheme,
    isMatchMode: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Base turf fill
        drawRect(color = pitchTheme.baseColor, size = size)

        // Lightweight horizontal mow stripes (low overdraw, crisp on Adreno 610)
        val stripeCount = if (isMatchMode) 12 else 8
        val stripeH = h / stripeCount
        for (i in 0 until stripeCount) {
            if (i % 2 == 1) {
                drawRect(
                    color = pitchTheme.stripeColor,
                    topLeft = Offset(0f, i * stripeH),
                    size = Size(w, stripeH)
                )
            }
        }

        val strokePx = 1.8.dp.toPx()
        val lineStroke = Stroke(width = strokePx)
        val lineColor = pitchTheme.lineColor.copy(alpha = 0.72f)

        val pad = 8.dp.toPx()
        val pitchW = w - pad * 2
        val pitchH = h - pad * 2

        // Outer pitch boundary
        drawRect(
            color = lineColor,
            topLeft = Offset(pad, pad),
            size = Size(pitchW, pitchH),
            style = lineStroke
        )

        // Halfway line
        val midY = h / 2f
        drawLine(
            color = lineColor,
            start = Offset(pad, midY),
            end = Offset(w - pad, midY),
            strokeWidth = strokePx
        )

        // Center circle & spot
        val centerRadius = (minOf(pitchW, pitchH) * 0.14f).coerceAtLeast(24f)
        drawCircle(
            color = lineColor,
            radius = centerRadius,
            center = Offset(w / 2f, midY),
            style = lineStroke
        )
        drawCircle(
            color = lineColor,
            radius = 3.dp.toPx(),
            center = Offset(w / 2f, midY)
        )

        // Top Penalty Area (Away GK end or attacking goal end)
        val boxW = pitchW * 0.56f
        val boxH = pitchH * 0.135f
        val sixW = pitchW * 0.28f
        val sixH = pitchH * 0.055f

        drawRect(
            color = lineColor,
            topLeft = Offset((w - boxW) / 2f, pad),
            size = Size(boxW, boxH),
            style = lineStroke
        )
        drawRect(
            color = lineColor,
            topLeft = Offset((w - sixW) / 2f, pad),
            size = Size(sixW, sixH),
            style = lineStroke
        )
        drawCircle(
            color = lineColor,
            radius = 2.5.dp.toPx(),
            center = Offset(w / 2f, pad + boxH * 0.72f)
        )

        // Bottom Penalty Area (Home GK end)
        drawRect(
            color = lineColor,
            topLeft = Offset((w - boxW) / 2f, h - pad - boxH),
            size = Size(boxW, boxH),
            style = lineStroke
        )
        drawRect(
            color = lineColor,
            topLeft = Offset((w - sixW) / 2f, h - pad - sixH),
            size = Size(sixW, sixH),
            style = lineStroke
        )
        drawCircle(
            color = lineColor,
            radius = 2.5.dp.toPx(),
            center = Offset(w / 2f, h - pad - boxH * 0.72f)
        )

        // Corner arcs
        val cornerR = 12.dp.toPx()
        drawArc(
            color = lineColor,
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(pad - cornerR, pad - cornerR),
            size = Size(cornerR * 2, cornerR * 2),
            style = lineStroke
        )
        drawArc(
            color = lineColor,
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w - pad - cornerR, pad - cornerR),
            size = Size(cornerR * 2, cornerR * 2),
            style = lineStroke
        )
        drawArc(
            color = lineColor,
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(pad - cornerR, h - pad - cornerR),
            size = Size(cornerR * 2, cornerR * 2),
            style = lineStroke
        )
        drawArc(
            color = lineColor,
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w - pad - cornerR, h - pad - cornerR),
            size = Size(cornerR * 2, cornerR * 2),
            style = lineStroke
        )
    }
}
