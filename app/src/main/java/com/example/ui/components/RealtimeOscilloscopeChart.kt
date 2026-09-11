package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TelemetryPoint
import com.example.viewmodel.MetricChannel
import java.util.Locale

/**
 * High-performance real-time oscilloscope chart component.
 * Modeled after MPAndroidChart / FL Chart architecture with 60/120 FPS
 * hardware-accelerated Compose Canvas rendering, dynamic auto-scaling,
 * multi-channel telemetry inspection, and glowing neon path shaders.
 */
@Composable
fun RealtimeOscilloscopeChart(
    telemetryHistory: List<TelemetryPoint>,
    selectedChannel: MetricChannel,
    onSelectChannel: (MetricChannel) -> Unit,
    modifier: Modifier = Modifier
) {
    var touchXNormalized by remember { mutableStateOf<Float?>(null) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .testTag("oscilloscope_chart_panel"),
        color = Color(0xFF090D16),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header: Title and Channel Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(selectedChannel.colorHex))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REAL-TIME TELEMETRY PLOT",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color(0xFF94A3B8)
                    )
                }

                // Channel Badge Indicator
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(selectedChannel.colorHex).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(selectedChannel.colorHex).copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = "BUFFER: ${telemetryHistory.size} PTS",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(selectedChannel.colorHex)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Channel Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricChannel.values().forEach { channel ->
                    val isSelected = channel == selectedChannel
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectChannel(channel) },
                        label = {
                            Text(
                                text = when (channel) {
                                    MetricChannel.LORENTZ_GAMMA -> "γ Factor"
                                    MetricChannel.VELOCITY_BETA -> "Speed β"
                                    MetricChannel.KINETIC_ENERGY -> "Ek (MeV)"
                                    MetricChannel.LORENTZ_FORCE -> "Force |F|"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF0F172A),
                            labelColor = Color(0xFF94A3B8),
                            selectedContainerColor = Color(channel.colorHex).copy(alpha = 0.2f),
                            selectedLabelColor = Color(channel.colorHex)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color(channel.colorHex) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Extract values for the active channel
            val rawValues = remember(telemetryHistory, selectedChannel) {
                telemetryHistory.map { point ->
                    when (selectedChannel) {
                        MetricChannel.LORENTZ_GAMMA -> point.gamma
                        MetricChannel.VELOCITY_BETA -> point.beta
                        MetricChannel.KINETIC_ENERGY -> point.kineticEnergyMeV
                        MetricChannel.LORENTZ_FORCE -> point.forceMagnitude
                    }
                }
            }

            val currentVal = rawValues.lastOrNull() ?: 0.0
            val minVal = rawValues.minOrNull() ?: 0.0
            val maxVal = rawValues.maxOrNull() ?: 1.0
            val avgVal = if (rawValues.isNotEmpty()) rawValues.average() else 0.0

            // Value Readout HUD Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B1220), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVE VALUE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = formatMetricValue(currentVal, selectedChannel),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color(selectedChannel.colorHex)
                    )
                }

                StatTag(label = "MIN", value = formatMetricValue(minVal, selectedChannel))
                StatTag(label = "AVG", value = formatMetricValue(avgVal, selectedChannel))
                StatTag(label = "MAX", value = formatMetricValue(maxVal, selectedChannel))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Waveform Canvas Plot Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF050811))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                touchXNormalized = (offset.x / size.width).coerceIn(0f, 1f)
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = { touchXNormalized = null },
                            onDragCancel = { touchXNormalized = null }
                        ) { change, _ ->
                            touchXNormalized = (change.position.x / size.width).coerceIn(0f, 1f)
                        }
                    }
            ) {
                val lineColor = Color(selectedChannel.colorHex)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 1. Grid Background (Subdivision lines)
                    val horizontalDivisions = 4
                    val verticalDivisions = 5
                    val gridPaintColor = Color(0xFF1E293B).copy(alpha = 0.5f)

                    for (i in 0..horizontalDivisions) {
                        val y = h * (i.toFloat() / horizontalDivisions)
                        drawLine(
                            color = gridPaintColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }

                    for (j in 0..verticalDivisions) {
                        val x = w * (j.toFloat() / verticalDivisions)
                        drawLine(
                            color = gridPaintColor,
                            start = Offset(x, 0f),
                            end = Offset(x, h),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }

                    if (rawValues.size < 2) return@Canvas

                    // 2. Compute dynamic Y-axis bounds with padding
                    val effectiveMin = if (minVal == maxVal) minVal * 0.9 else minVal
                    val effectiveMax = if (minVal == maxVal) maxVal * 1.1 + 0.001 else maxVal
                    val span = (effectiveMax - effectiveMin).coerceAtLeast(1e-9)

                    // 3. Construct Path
                    val path = Path()
                    val fillPath = Path()
                    val points = ArrayList<Offset>(rawValues.size)

                    rawValues.forEachIndexed { index, value ->
                        val x = w * (index.toFloat() / (rawValues.size - 1))
                        val normalizedY = ((value - effectiveMin) / span).toFloat().coerceIn(0f, 1f)
                        val y = h - (normalizedY * (h - 16f) + 8f)
                        points.add(Offset(x, y))

                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, h)
                            fillPath.lineTo(x, y)
                        } else {
                            // Smooth bezier interpolation
                            val prev = points[index - 1]
                            val cx1 = prev.x + (x - prev.x) / 2f
                            val cy1 = prev.y
                            val cx2 = prev.x + (x - prev.x) / 2f
                            val cy2 = y
                            path.cubicTo(cx1, cy1, cx2, cy2, x, y)
                            fillPath.cubicTo(cx1, cy1, cx2, cy2, x, y)
                        }
                    }

                    fillPath.lineTo(points.last().x, h)
                    fillPath.close()

                    // Gradient under curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.28f),
                                lineColor.copy(alpha = 0.0f)
                            )
                        )
                    )

                    // Path stroke with glowing outline effect
                    drawPath(
                        path = path,
                        color = lineColor.copy(alpha = 0.4f),
                        style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Pulsing latest point
                    val lastPoint = points.last()
                    drawCircle(
                        color = lineColor.copy(alpha = 0.35f),
                        radius = 8f,
                        center = lastPoint
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 4f,
                        center = lastPoint
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2f,
                        center = lastPoint
                    )

                    // Touch inspection crosshair
                    touchXNormalized?.let { normX ->
                        val inspectionX = normX * w
                        val index = ((rawValues.size - 1) * normX).toInt().coerceIn(0, rawValues.lastIndex)
                        val inspectPoint = points[index]

                        drawLine(
                            color = Color.White.copy(alpha = 0.7f),
                            start = Offset(inspectionX, 0f),
                            end = Offset(inspectionX, h),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        )

                        drawCircle(
                            color = Color.White,
                            radius = 5f,
                            center = inspectPoint
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTag(label: String, value: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            ),
            color = Color(0xFF64748B)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            ),
            color = Color(0xFFCBD5E1)
        )
    }
}

private fun formatMetricValue(value: Double, channel: MetricChannel): String {
    return when (channel) {
        MetricChannel.LORENTZ_GAMMA -> String.format(Locale.US, "%.3f", value)
        MetricChannel.VELOCITY_BETA -> String.format(Locale.US, "%.4f c", value)
        MetricChannel.KINETIC_ENERGY -> {
            if (value >= 1000.0) {
                String.format(Locale.US, "%.2f GeV", value / 1000.0)
            } else {
                String.format(Locale.US, "%.2f MeV", value)
            }
        }
        MetricChannel.LORENTZ_FORCE -> {
            if (value < 1e-12) {
                String.format(Locale.US, "%.2e N", value)
            } else {
                String.format(Locale.US, "%.3e N", value)
            }
        }
    }
}
