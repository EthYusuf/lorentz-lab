package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Vector3
import com.example.ui.components.AcademicTheorySheet
import com.example.ui.components.LaboratoryControlPanel
import com.example.ui.components.RealtimeOscilloscopeChart
import com.example.ui.components.TelemetryGaugeStrip
import com.example.ui.components.VectorFieldCanvas
import com.example.viewmodel.MetricChannel
import com.example.viewmodel.SimulationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelativisticLabScreen(
    viewModel: SimulationViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showTheorySheet by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060A14)),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "RELATIVISTIC LAB",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.2.sp
                                ),
                                color = Color(0xFF00E5FF)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Status Pulse Pill
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (uiState.isRunning) Color(0xFF00E676).copy(alpha = 0.15f) else Color(0xFFFF5252).copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (uiState.isRunning) Color(0xFF00E676) else Color(0xFFFF5252)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (uiState.isRunning) Color(0xFF00E676) else Color(0xFFFF5252))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (uiState.isRunning) "LIVE" else "PAUSED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = if (uiState.isRunning) Color(0xFF00E676) else Color(0xFFFF5252)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "RK-4 • 4-MOMENTUM SOLVER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp
                            ),
                            color = Color(0xFF64748B)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showReportDialog = true },
                        modifier = Modifier.testTag("report_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Laboratory Report & Snapshot",
                            tint = Color(0xFFD500F9)
                        )
                    }
                    IconButton(
                        onClick = { showTheorySheet = true },
                        modifier = Modifier.testTag("theory_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Functions,
                            contentDescription = "Mathematical Foundations",
                            tint = Color(0xFF00E5FF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF090E1A),
                    titleContentColor = Color(0xFF00E5FF)
                ),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF090E1A),
                contentColor = Color(0xFF94A3B8),
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Simulation"
                        )
                    },
                    label = {
                        Text(
                            text = "SIMULATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00E5FF),
                        selectedTextColor = Color(0xFF00E5FF),
                        indicatorColor = Color(0xFF00E5FF).copy(alpha = 0.15f),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Analytics"
                        )
                    },
                    label = {
                        Text(
                            text = "TELEMETRY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFD500F9),
                        selectedTextColor = Color(0xFFD500F9),
                        indicatorColor = Color(0xFFD500F9).copy(alpha = 0.15f),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF060A14))
        ) {
            when (selectedTab) {
                0 -> {
                    // Main Simulation View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Vector Field & Particle Arena (Custom Canvas)
                        VectorFieldCanvas(
                            particle = uiState.particle,
                            fieldEnvironment = uiState.fieldEnvironment,
                            showVectors = uiState.showVectors,
                            showFieldGrid = uiState.showFieldGrid,
                            onInjectAtCoordinates = { spawnPos ->
                                viewModel.resetSimulation()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                        )

                        // 2. Real-time Telemetry HUD Gauge Strip
                        TelemetryGaugeStrip(
                            particle = uiState.particle,
                            fieldEnvironment = uiState.fieldEnvironment,
                            simulationTimeNs = uiState.simulationTimeNs
                        )

                        // 3. Compact Live Oscilloscope Plot
                        RealtimeOscilloscopeChart(
                            telemetryHistory = uiState.telemetryHistory,
                            selectedChannel = uiState.selectedMetricChannel,
                            onSelectChannel = { viewModel.setMetricChannel(it) }
                        )

                        // 4. Full Laboratory Control Panel (Presets, Particles, Sliders)
                        LaboratoryControlPanel(
                            isRunning = uiState.isRunning,
                            selectedSpecies = uiState.selectedSpecies,
                            activePreset = uiState.activePreset,
                            fieldEnvironment = uiState.fieldEnvironment,
                            currentBeta = uiState.initialBeta,
                            showVectors = uiState.showVectors,
                            showFieldGrid = uiState.showFieldGrid,
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onStepSimulation = { viewModel.stepSimulation() },
                            onResetSimulation = { viewModel.resetSimulation() },
                            onInjectBeamBurst = { viewModel.injectBeamBurst() },
                            onSelectSpecies = { viewModel.selectSpecies(it) },
                            onSelectPreset = { viewModel.selectPreset(it) },
                            onUpdateBeta = { viewModel.updateBeta(it) },
                            onUpdateBField = { viewModel.updateBField(it) },
                            onUpdateEField = { viewModel.updateEField(it) },
                            onToggleVectors = { viewModel.toggleVectors() },
                            onToggleFieldGrid = { viewModel.toggleFieldGrid() }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                1 -> {
                    // Deep Telemetry & Diagnostics View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Expanded Primary Oscilloscope
                        RealtimeOscilloscopeChart(
                            telemetryHistory = uiState.telemetryHistory,
                            selectedChannel = uiState.selectedMetricChannel,
                            onSelectChannel = { viewModel.setMetricChannel(it) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Diagnostics & Relativistic Invariants Card
                        DiagnosticsCard(uiState = uiState)

                        // Detailed Multi-Channel Telemetry Logs Strip
                        TelemetryGaugeStrip(
                            particle = uiState.particle,
                            fieldEnvironment = uiState.fieldEnvironment,
                            simulationTimeNs = uiState.simulationTimeNs
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Modal Sheet for Academic Theory & Mathematics
    if (showTheorySheet) {
        AcademicTheorySheet(
            onDismissRequest = { showTheorySheet = false }
        )
    }

    // Laboratory Report & Telemetry Snapshot Dialog
    if (showReportDialog) {
        LaboratoryReportDialog(
            uiState = uiState,
            onDismissRequest = { showReportDialog = false }
        )
    }
}

@Composable
fun LaboratoryReportDialog(
    uiState: com.example.viewmodel.SimulationUiState,
    onDismissRequest: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = Color(0xFF090E1A),
        titleContentColor = Color(0xFF00E5FF),
        textContentColor = Color(0xFFE2E8F0),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = Color(0xFFD500F9),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RESEARCH TELEMETRY SNAPSHOT",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Instantaneous physical snapshot captured from the RK4 relativistic numerical solver:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF050811),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "PRESET: ${uiState.activePreset.title}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF00E5FF)
                        )
                        Text(
                            text = "SPECIES: ${uiState.particle.species.displayName} (${uiState.particle.species.symbol})",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFFE2E8F0)
                        )
                        Text(
                            text = "SPEED (β): ${String.format("%.4f", uiState.particle.beta)} c",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFFD500F9)
                        )
                        Text(
                            text = "LORENTZ (γ): ${String.format("%.4f", uiState.particle.gamma)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFFFFD600)
                        )
                        Text(
                            text = "KINETIC ENERGY: ${String.format("%.3f", uiState.particle.kineticEnergyMeV)} MeV",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFF00E676)
                        )
                        Text(
                            text = "MAGNETIC FLUX: ${String.format("%.3f", uiState.fieldEnvironment.baseBFieldTesla)} T",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF00E5FF).copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "MIT Licensed Open-Source Scientific Workstation\nCopyright (c) 2026 RelativisticLab",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp
                        ),
                        color = Color(0xFF00E5FF)
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismissRequest) {
                Text(
                    text = "CLOSE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
            }
        }
    )
}

@Composable
private fun DiagnosticsCard(uiState: com.example.viewmodel.SimulationUiState) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
        color = Color(0xFF090E1A)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "NUMERICAL DIAGNOSTICS & CONSERVATION LAWS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.1.sp
                ),
                color = Color(0xFF00E5FF)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF050811), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiagnosticRow("Algorithm", "Runge-Kutta 4th Order (RK4)")
                DiagnosticRow("Sub-Steps per Frame", "6 Adaptive Iterations")
                DiagnosticRow("Total Steps Computed", "${uiState.totalStepsComputed}")
                DiagnosticRow(
                    "Invariant Mass E₀",
                    String.format("%.3f MeV", uiState.particle.species.restEnergyMeV)
                )
                DiagnosticRow(
                    "Total Relativistic Energy",
                    String.format("%.3f MeV", uiState.particle.totalEnergyMeV)
                )
                DiagnosticRow(
                    "Current Momentum |p|",
                    String.format("%.4e kg·m/s", uiState.particle.momentum.magnitude())
                )
                DiagnosticRow(
                    "Coordinate Position (x, y)",
                    String.format("(%.3f, %.3f) m", uiState.particle.position.x, uiState.particle.position.y)
                )
            }
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            ),
            color = Color(0xFF64748B)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = Color(0xFFE2E8F0)
        )
    }
}
