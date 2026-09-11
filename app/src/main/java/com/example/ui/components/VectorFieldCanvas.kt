package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FieldEnvironment
import com.example.model.ParticleState
import com.example.model.SimulationPreset
import com.example.model.Vector3
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * High-performance Custom Canvas rendering of relativistic trajectories,
 * dynamic electromagnetic vector fields (E & B), and instantaneous force vectors.
 */
@Composable
fun VectorFieldCanvas(
    particle: ParticleState,
    fieldEnvironment: FieldEnvironment,
    showVectors: Boolean,
    showFieldGrid: Boolean,
    onInjectAtCoordinates: (Vector3) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .background(Color(0xFF060A14))
            .testTag("simulation_canvas_arena")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Map screen pixels to simulation space [-0.2m, +0.2m]
                        val simX = ((offset.x / size.width) - 0.5) * 0.4
                        val simY = ((offset.y / size.height) - 0.5) * -0.4
                        onInjectAtCoordinates(Vector3(simX, simY, 0.0))
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val simX = ((change.position.x / size.width) - 0.5) * 0.4
                        val simY = ((change.position.y / size.height) - 0.5) * -0.4
                        onInjectAtCoordinates(Vector3(simX, simY, 0.0))
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW / 2f
            val centerY = canvasH / 2f
            val scalePxPerMeter = (canvasW.coerceAtMost(canvasH) / 0.4f)

            fun toScreenOffset(simPos: Vector3): Offset {
                return Offset(
                    x = centerX + (simPos.x * scalePxPerMeter).toFloat(),
                    y = centerY - (simPos.y * scalePxPerMeter).toFloat()
                )
            }

            // 1. Draw Lab Coordinate Grid & Crosshairs
            drawLabGrid(centerX, centerY, canvasW, canvasH, scalePxPerMeter)

            // 2. Draw Vector Field (Magnetic B & Electric E lines)
            if (showFieldGrid) {
                drawFieldVectors(
                    fieldEnvironment = fieldEnvironment,
                    centerX = centerX,
                    centerY = centerY,
                    canvasW = canvasW,
                    canvasH = canvasH,
                    scalePxPerMeter = scalePxPerMeter
                )
            }

            // 3. Draw Particle Trajectory Path with Relativistic Glow
            if (particle.trajectory.size > 1) {
                drawParticleTrajectory(
                    trajectory = particle.trajectory,
                    colorHex = particle.colorHex,
                    toScreen = ::toScreenOffset
                )
            }

            // 4. Draw Vector Overlays (Velocity v, Force F, Larmor Radius)
            val currentScreenPos = toScreenOffset(particle.position)
            if (showVectors) {
                drawPhysicalVectors(
                    particle = particle,
                    fieldEnvironment = fieldEnvironment,
                    currentScreenPos = currentScreenPos,
                    scalePxPerMeter = scalePxPerMeter
                )
            }

            // 5. Draw Particle Core & Glowing Corona
            val baseParticleColor = Color(particle.colorHex)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseParticleColor.copy(alpha = 0.8f * pulseGlow),
                        baseParticleColor.copy(alpha = 0.2f * pulseGlow),
                        Color.Transparent
                    ),
                    center = currentScreenPos,
                    radius = 24f
                ),
                radius = 24f,
                center = currentScreenPos
            )
            drawCircle(
                color = baseParticleColor,
                radius = 7f,
                center = currentScreenPos
            )
            drawCircle(
                color = Color.White,
                radius = 3.5f,
                center = currentScreenPos
            )
        }

        // Overlay Legend / Preset Badge
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            color = Color(0xFF090D18).copy(alpha = 0.85f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Text(
                text = "${fieldEnvironment.preset.title.uppercase()} | B = ${String.format("%.2f", fieldEnvironment.baseBFieldTesla)} T",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                ),
                color = Color(0xFF00E5FF)
            )
        }

        // Interactive instruction chip
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            color = Color(0xFF090D18).copy(alpha = 0.75f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = "TAP / DRAG TO INJECT BEAM",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                ),
                color = Color(0xFF64748B)
            )
        }
    }
}

private fun DrawScope.drawLabGrid(
    centerX: Float,
    centerY: Float,
    w: Float,
    h: Float,
    scale: Float
) {
    val gridSpacingPx = scale * 0.05f // 5 cm grid intervals
    val subtleGridColor = Color(0xFF0E1626)

    // Vertical grid lines
    var x = centerX % gridSpacingPx
    while (x < w) {
        drawLine(
            color = subtleGridColor,
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 1f
        )
        x += gridSpacingPx
    }

    // Horizontal grid lines
    var y = centerY % gridSpacingPx
    while (y < h) {
        drawLine(
            color = subtleGridColor,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1f
        )
        y += gridSpacingPx
    }

    // Central Axes
    val axisColor = Color(0xFF1E293B)
    drawLine(
        color = axisColor,
        start = Offset(0f, centerY),
        end = Offset(w, centerY),
        strokeWidth = 1.5f
    )
    drawLine(
        color = axisColor,
        start = Offset(centerX, 0f),
        end = Offset(centerX, h),
        strokeWidth = 1.5f
    )
}

private fun DrawScope.drawFieldVectors(
    fieldEnvironment: FieldEnvironment,
    centerX: Float,
    centerY: Float,
    canvasW: Float,
    canvasH: Float,
    scalePxPerMeter: Float
) {
    val step = 42f
    val bColor = Color(0xFF00E5FF).copy(alpha = 0.22f)
    val eColor = Color(0xFFFFD600).copy(alpha = 0.25f)

    var curX = step / 2f
    while (curX < canvasW) {
        var curY = step / 2f
        while (curY < canvasH) {
            val simX = ((curX - centerX) / scalePxPerMeter).toDouble()
            val simY = ((centerY - curY) / scalePxPerMeter).toDouble()
            val simPos = Vector3(simX, simY, 0.0)

            val bVector = fieldEnvironment.evaluateB(simPos)
            val eVector = fieldEnvironment.evaluateE(simPos)

            // Draw Magnetic Field Symbol (Out-of-plane circle with dot/cross)
            if (kotlin.math.abs(bVector.z) > 1e-6) {
                val isOutOfPlane = bVector.z > 0
                val radius = 4.5f
                drawCircle(
                    color = bColor,
                    radius = radius,
                    center = Offset(curX, curY),
                    style = Stroke(width = 1.2f)
                )
                if (isOutOfPlane) {
                    drawCircle(color = bColor, radius = 1.5f, center = Offset(curX, curY))
                } else {
                    // Cross for in-plane
                    val d = 2.5f
                    drawLine(bColor, Offset(curX - d, curY - d), Offset(curX + d, curY + d), 1f)
                    drawLine(bColor, Offset(curX - d, curY + d), Offset(curX + d, curY - d), 1f)
                }
            }

            // Draw Electric Field Vector Arrow
            if (eVector.magnitudeSquared() > 1e-6) {
                val arrowLen = 14f
                val angle = atan2(-eVector.y, eVector.x).toFloat()
                val endX = curX + arrowLen * cos(angle)
                val endY = curY + arrowLen * sin(angle)

                drawLine(
                    color = eColor,
                    start = Offset(curX, curY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.5f,
                    cap = StrokeCap.Round
                )
            }

            curY += step
        }
        curX += step
    }
}

private fun DrawScope.drawParticleTrajectory(
    trajectory: List<Vector3>,
    colorHex: Long,
    toScreen: (Vector3) -> Offset
) {
    val total = trajectory.size
    val baseColor = Color(colorHex)

    for (i in 1 until total) {
        val p1 = toScreen(trajectory[i - 1])
        val p2 = toScreen(trajectory[i])
        val alphaFraction = (i.toFloat() / total).coerceIn(0.08f, 1.0f)
        val strokeWidth = 1.0f + 2.5f * alphaFraction

        drawLine(
            color = baseColor.copy(alpha = alphaFraction * 0.9f),
            start = p1,
            end = p2,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawPhysicalVectors(
    particle: ParticleState,
    fieldEnvironment: FieldEnvironment,
    currentScreenPos: Offset,
    scalePxPerMeter: Float
) {
    val v = particle.velocity
    val b = fieldEnvironment.evaluateB(particle.position)
    val e = fieldEnvironment.evaluateE(particle.position)
    val lorentzForce = (e + v.cross(b)) * particle.species.chargeCoulombs

    // 1. Velocity Vector (Neon Cyan)
    val vMag = v.magnitude()
    if (vMag > 1e4) {
        val vScale = 45f / ParticleState.C_LIGHT.toFloat()
        val vScreenX = currentScreenPos.x + (v.x * vScale).toFloat()
        val vScreenY = currentScreenPos.y - (v.y * vScale).toFloat()

        drawArrow(
            start = currentScreenPos,
            end = Offset(vScreenX, vScreenY),
            color = Color(0xFF00E5FF),
            strokeWidth = 2.5f,
            arrowHeadSize = 8f
        )
    }

    // 2. Lorentz Force Vector (Electric Magenta)
    val fMag = lorentzForce.magnitude()
    if (fMag > 1e-18) {
        val fScale = 38f / fMag.toFloat().coerceAtLeast(1e-12f)
        val fScreenX = currentScreenPos.x + (lorentzForce.x * fScale).toFloat().coerceIn(-50f, 50f)
        val fScreenY = currentScreenPos.y - (lorentzForce.y * fScale).toFloat().coerceIn(-50f, 50f)

        drawArrow(
            start = currentScreenPos,
            end = Offset(fScreenX, fScreenY),
            color = Color(0xFFD500F9),
            strokeWidth = 2.5f,
            arrowHeadSize = 8f
        )
    }
}

private fun DrawScope.drawArrow(
    start: Offset,
    end: Offset,
    color: Color,
    strokeWidth: Float,
    arrowHeadSize: Float
) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    val angle = atan2((end.y - start.y), (end.x - start.x))
    val headAngle = Math.PI / 6.0 // 30 degrees

    val path = Path().apply {
        moveTo(end.x, end.y)
        lineTo(
            (end.x - arrowHeadSize * cos(angle - headAngle)).toFloat(),
            (end.y - arrowHeadSize * sin(angle - headAngle)).toFloat()
        )
        lineTo(
            (end.x - arrowHeadSize * cos(angle + headAngle)).toFloat(),
            (end.y - arrowHeadSize * sin(angle + headAngle)).toFloat()
        )
        close()
    }
    drawPath(path = path, color = color)
}
