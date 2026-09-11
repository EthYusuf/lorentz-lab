package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FieldEnvironment
import com.example.model.ParticleState
import java.util.Locale

@Composable
fun TelemetryGaugeStrip(
    particle: ParticleState,
    fieldEnvironment: FieldEnvironment,
    simulationTimeNs: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
            .testTag("telemetry_gauge_strip"),
        color = Color(0xFF090E1A)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top Bar: Particle tag and Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(particle.colorHex).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(particle.colorHex))
                    ) {
                        Text(
                            text = particle.species.symbol,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color(particle.colorHex)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = particle.species.displayName.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color(0xFFE2E8F0)
                    )
                }

                Text(
                    text = String.format(Locale.US, "t = %.2f ns", simulationTimeNs),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF64748B)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Grid Telemetry Cells
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryCell(
                    label = "BETA (v/c)",
                    value = String.format(Locale.US, "%.4f", particle.beta),
                    sublabel = String.format(Locale.US, "%.1f%% c", particle.beta * 100),
                    accentColor = Color(0xFF00E5FF),
                    modifier = Modifier.weight(1f)
                )

                TelemetryCell(
                    label = "LORENTZ γ",
                    value = String.format(Locale.US, "%.3f", particle.gamma),
                    sublabel = String.format(Locale.US, "Δt'=%.2fΔt", particle.gamma),
                    accentColor = Color(0xFFD500F9),
                    modifier = Modifier.weight(1f)
                )

                TelemetryCell(
                    label = "KINETIC Ek",
                    value = if (particle.kineticEnergyMeV >= 1000.0) {
                        String.format(Locale.US, "%.2f", particle.kineticEnergyMeV / 1000.0)
                    } else {
                        String.format(Locale.US, "%.2f", particle.kineticEnergyMeV)
                    },
                    sublabel = if (particle.kineticEnergyMeV >= 1000.0) "GeV" else "MeV",
                    accentColor = Color(0xFFFFD600),
                    modifier = Modifier.weight(1f)
                )

                val gyroR = particle.gyroradius(fieldEnvironment.baseBFieldTesla)
                TelemetryCell(
                    label = "GYRORADIUS",
                    value = if (gyroR.isInfinite()) "∞" else String.format(Locale.US, "%.1f", gyroR * 100.0),
                    sublabel = if (gyroR.isInfinite()) "Zero B" else "cm",
                    accentColor = Color(0xFF00E676),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TelemetryCell(
    label: String,
    value: String,
    sublabel: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF050811), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            ),
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace
            ),
            color = accentColor
        )
        Text(
            text = sublabel,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            ),
            color = Color(0xFF94A3B8)
        )
    }
}
