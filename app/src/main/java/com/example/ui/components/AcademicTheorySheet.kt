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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicTheorySheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFF090E1A),
        contentColor = Color(0xFFE2E8F0),
        dragHandle = null
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Functions,
                        contentDescription = "Theory",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "MATHEMATICAL FOUNDATIONS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.1.sp
                        ),
                        color = Color(0xFF00E5FF)
                    )
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Text(
                text = "Relativistic Electrodynamics & 4th-Order Runge-Kutta Integration",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Relativistic Equations of Motion
            EquationCard(
                title = "1. RELATIVISTIC LORENTZ EQUATIONS OF MOTION",
                description = "For a particle of rest mass m₀ and elementary charge q in electromagnetic fields E and B, Newton's second law is generalized in four-momentum space:",
                formulaLines = listOf(
                    "dp / dt = q ( E(r, t) + v × B(r) )",
                    "p = γ m₀ v   ⟹   v = p / √(m₀² + |p|² / c²)",
                    "γ = 1 / √(1 - v² / c²) = √(1 + |p|² / (m₀² c²))"
                ),
                accentColor = Color(0xFF00E5FF)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Section 2: Numerical Integration (RK-4 Butcher Tableau)
            EquationCard(
                title = "2. 4TH-ORDER RUNGE-KUTTA (RK-4) NUMERICAL SCHEME",
                description = "The state vector S = (r, p)ᵀ evolves according to dS/dt = f(t, S). RK4 evaluates four intermediate test slopes to achieve local truncation error O(Δt⁵):",
                formulaLines = listOf(
                    "k₁ = f(tₙ, Sₙ)",
                    "k₂ = f(tₙ + ½Δt,  Sₙ + ½Δt k₁)",
                    "k₃ = f(tₙ + ½Δt,  Sₙ + ½Δt k₂)",
                    "k₄ = f(tₙ + Δt,   Sₙ + Δt k₃)",
                    "Sₙ₊₁ = Sₙ + (Δt / 6) [ k₁ + 2k₂ + 2k₃ + k₄ ]"
                ),
                accentColor = Color(0xFFD500F9)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Section 3: Energy Conservation & Larmor Gyroradius
            EquationCard(
                title = "3. RELATIVISTIC KINEMATICS & LARMOR RADIUS",
                description = "Kinetic energy (Ek), total invariant mass-energy (E), and relativistic cyclotron gyroradius (r_L):",
                formulaLines = listOf(
                    "E² = (p c)² + (m₀ c²)²",
                    "Ek = (γ - 1) m₀ c²",
                    "r_L = p_⟂ / (|q| B) = γ m₀ v_⟂ / (|q| B)",
                    "ω_c = |q| B / (γ m₀)   [Relativistic Cyclotron Frequency]"
                ),
                accentColor = Color(0xFFFFD600)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Section 4: Physical Configurations
            EquationCard(
                title = "4. LABORATORY PRESET PRINCIPLES",
                description = "Specialized configurations evaluated dynamically:",
                formulaLines = listOf(
                    "• Wien Filter: F_net = q(E - vB) = 0  ⟹  v_select = E / B",
                    "• Magnetic Mirror: μ = p_⟂² / (2 m₀ B) = const (Reflection condition)",
                    "• Quadrupole Lens: B = G(y x̂ + x ŷ), div B = 0, curl B = 0"
                ),
                accentColor = Color(0xFF00E676)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EquationCard(
    title: String,
    description: String,
    formulaLines: List<String>,
    accentColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
        color = Color(0xFF050811)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = accentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B1220), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                formulaLines.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = Color(0xFFE2E8F0)
                    )
                }
            }
        }
    }
}
