package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.model.ParticleSpecies
import com.example.model.SimulationPreset
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LaboratoryControlPanel(
    isRunning: Boolean,
    selectedSpecies: ParticleSpecies,
    activePreset: SimulationPreset,
    fieldEnvironment: FieldEnvironment,
    currentBeta: Double,
    showVectors: Boolean,
    showFieldGrid: Boolean,
    onTogglePlayPause: () -> Unit,
    onStepSimulation: () -> Unit,
    onResetSimulation: () -> Unit,
    onInjectBeamBurst: () -> Unit,
    onSelectSpecies: (ParticleSpecies) -> Unit,
    onSelectPreset: (SimulationPreset) -> Unit,
    onUpdateBeta: (Double) -> Unit,
    onUpdateBField: (Double) -> Unit,
    onUpdateEField: (Double) -> Unit,
    onToggleVectors: () -> Unit,
    onToggleFieldGrid: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .testTag("laboratory_control_panel"),
        color = Color(0xFF090E1A)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Simulation Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Play / Pause Primary Button
                    ElevatedButton(
                        onClick = onTogglePlayPause,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = if (isRunning) Color(0xFFD500F9) else Color(0xFF00E5FF),
                            contentColor = Color(0xFF060A14)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Play",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRunning) "PAUSE" else "RESUME",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }

                    // Step Frame
                    FilledTonalButton(
                        onClick = onStepSimulation,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color(0xFF94A3B8)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("step_frame_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Step",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "STEP",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }

                    // Inject Beam Burst
                    FilledTonalButton(
                        onClick = onInjectBeamBurst,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF0F2634),
                            contentColor = Color(0xFF00E5FF)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("burst_inject_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Inject Beam",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "BURST",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }

                // Reset & Quick Toggles
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onToggleVectors,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (showVectors) Color(0xFF1E293B) else Color(0xFF0F172A),
                            contentColor = if (showVectors) Color(0xFF00E5FF) else Color(0xFF64748B)
                        ),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Toggle Vectors",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleFieldGrid,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (showFieldGrid) Color(0xFF1E293B) else Color(0xFF0F172A),
                            contentColor = if (showFieldGrid) Color(0xFFFFD600) else Color(0xFF64748B)
                        ),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Toggle Grid",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onResetSimulation,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Simulation",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Presets Selector
            Text(
                text = "PHYSICAL CONFIGURATION PRESETS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                ),
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SimulationPreset.values().forEach { preset ->
                    val isSelected = preset == activePreset
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectPreset(preset) },
                        label = {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF0F172A),
                            labelColor = Color(0xFF94A3B8),
                            selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFF00E5FF)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Particle Species Selector
            Text(
                text = "PARTICLE SPECIES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                ),
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ParticleSpecies.values().forEach { species ->
                    val isSelected = species == selectedSpecies
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectSpecies(species) },
                        label = {
                            Text(
                                text = "${species.symbol} ${species.displayName}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF0F172A),
                            labelColor = Color(0xFF94A3B8),
                            selectedContainerColor = Color(0xFFD500F9).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFFD500F9)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color(0xFFD500F9) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Parameter Sliders
            // 1. Relativistic Velocity Beta Slider
            SliderControlRow(
                label = "INJECTION SPEED β (v/c)",
                valueText = String.format(Locale.US, "%.3f c", currentBeta),
                sliderValue = currentBeta.toFloat(),
                valueRange = 0.05f..0.98f,
                accentColor = Color(0xFF00E5FF),
                onValueChange = { onUpdateBeta(it.toDouble()) }
            )

            // 2. Magnetic Field B Slider
            SliderControlRow(
                label = "MAGNETIC FLUX DENSITY (B)",
                valueText = String.format(Locale.US, "%.3f T", fieldEnvironment.baseBFieldTesla),
                sliderValue = fieldEnvironment.baseBFieldTesla.toFloat(),
                valueRange = -0.10f..0.10f,
                accentColor = Color(0xFFD500F9),
                onValueChange = { onUpdateBField(it.toDouble()) }
            )

            // 3. Electric Field E Slider
            SliderControlRow(
                label = "ELECTRIC FIELD (E)",
                valueText = String.format(Locale.US, "%.1f kV/m", fieldEnvironment.baseEFieldVPerM / 1000.0),
                sliderValue = (fieldEnvironment.baseEFieldVPerM / 1000.0).toFloat(),
                valueRange = -5000f..5000f,
                accentColor = Color(0xFFFFD600),
                onValueChange = { onUpdateEField(it.toDouble() * 1000.0) }
            )
        }
    }
}

@Composable
private fun SliderControlRow(
    label: String,
    valueText: String,
    sliderValue: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    accentColor: Color,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                ),
                color = Color(0xFF94A3B8)
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = accentColor
            )
        }
        Slider(
            value = sliderValue,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color(0xFF1E293B)
            ),
            modifier = Modifier.height(26.dp)
        )
    }
}
