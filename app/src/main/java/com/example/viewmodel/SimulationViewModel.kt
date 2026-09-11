package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.FieldEnvironment
import com.example.model.ParticleSpecies
import com.example.model.ParticleState
import com.example.model.SimulationPreset
import com.example.model.TelemetryPoint
import com.example.model.Vector3
import com.example.physics.RelativisticEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class MetricChannel(val label: String, val unit: String, val colorHex: Long) {
    LORENTZ_GAMMA("Lorentz Factor (γ)", "", 0xFF00E5FF),
    VELOCITY_BETA("Relativistic Speed (β = v/c)", "", 0xFFD500F9),
    KINETIC_ENERGY("Kinetic Energy (Ek)", "MeV", 0xFFFFD600),
    LORENTZ_FORCE("Lorentz Force (|F|)", "N", 0xFFFF5252)
}

data class SimulationUiState(
    val isRunning: Boolean = true,
    val particle: ParticleState,
    val fieldEnvironment: FieldEnvironment,
    val telemetryHistory: List<TelemetryPoint> = emptyList(),
    val activePreset: SimulationPreset = SimulationPreset.SYNCHROTRON_DIPOLE,
    val selectedSpecies: ParticleSpecies = ParticleSpecies.ELECTRON,
    val initialBeta: Double = 0.85,
    val simulationTimeNs: Double = 0.0,
    val showVectors: Boolean = true,
    val showFieldGrid: Boolean = true,
    val selectedMetricChannel: MetricChannel = MetricChannel.LORENTZ_GAMMA,
    val fps: Int = 60,
    val totalStepsComputed: Long = 0L
)

class SimulationViewModel : ViewModel() {

    private val _uiState: MutableStateFlow<SimulationUiState>
    val uiState: StateFlow<SimulationUiState>

    private var simulationJob: Job? = null
    private val maxHistorySize = 120

    init {
        val initialPreset = SimulationPreset.SYNCHROTRON_DIPOLE
        val initialSpecies = initialPreset.recommendedSpecies
        val initialBeta = initialPreset.defaultBeta

        val particle = createInitialParticle(initialPreset, initialSpecies, initialBeta)
        val field = FieldEnvironment(
            preset = initialPreset,
            baseBFieldTesla = initialPreset.defaultBFieldTesla,
            baseEFieldVPerM = initialPreset.defaultEFieldVPerM
        )

        _uiState = MutableStateFlow(
            SimulationUiState(
                particle = particle,
                fieldEnvironment = field,
                activePreset = initialPreset,
                selectedSpecies = initialSpecies,
                initialBeta = initialBeta,
                telemetryHistory = listOf(generateTelemetryPoint(particle, field, 0.0))
            )
        )
        uiState = _uiState.asStateFlow()

        startSimulationLoop()
    }

    private fun createInitialParticle(
        preset: SimulationPreset,
        species: ParticleSpecies,
        beta: Double
    ): ParticleState {
        return when (preset) {
            SimulationPreset.SYNCHROTRON_DIPOLE -> {
                RelativisticEngine.createParticle(
                    species = species,
                    initialPosition = Vector3(0.0, -0.08, 0.0),
                    initialDirection = Vector3(1.0, 0.0, 0.0),
                    beta = beta
                )
            }
            SimulationPreset.WIEN_VELOCITY_SELECTOR -> {
                RelativisticEngine.createParticle(
                    species = species,
                    initialPosition = Vector3(-0.15, 0.0, 0.0),
                    initialDirection = Vector3(1.0, 0.0, 0.0),
                    beta = beta
                )
            }
            SimulationPreset.MAGNETIC_MIRROR -> {
                RelativisticEngine.createParticle(
                    species = species,
                    initialPosition = Vector3(0.02, 0.0, -0.10),
                    initialDirection = Vector3(0.3, 0.9, 0.4).normalized(),
                    beta = beta
                )
            }
            SimulationPreset.ELECTRIC_QUADRUPOLE -> {
                RelativisticEngine.createParticle(
                    species = species,
                    initialPosition = Vector3(-0.15, 0.02, 0.0),
                    initialDirection = Vector3(1.0, 0.05, 0.0).normalized(),
                    beta = beta
                )
            }
        }
    }

    private fun generateTelemetryPoint(
        p: ParticleState,
        field: FieldEnvironment,
        timeNs: Double
    ): TelemetryPoint {
        val v = p.velocity
        val e = field.evaluateE(p.position)
        val b = field.evaluateB(p.position)
        val force = (e + v.cross(b)) * p.species.chargeCoulombs

        return TelemetryPoint(
            timeNs = timeNs,
            beta = p.beta,
            gamma = p.gamma,
            kineticEnergyMeV = p.kineticEnergyMeV,
            momentumMagnitude = p.momentum.magnitude(),
            forceMagnitude = force.magnitude(),
            posX = p.position.x,
            posY = p.position.y,
            posZ = p.position.z
        )
    }

    private fun startSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            // Adaptive sub-step size in seconds
            val dtSubStep = 2.0e-11 // 0.02 nanoseconds per numerical sub-step
            val subStepsPerFrame = 6

            while (isActive) {
                if (_uiState.value.isRunning) {
                    val currentState = _uiState.value
                    var currentParticle = currentState.particle
                    val field = currentState.fieldEnvironment
                    var currentTimeSec = currentState.simulationTimeNs * 1e-9

                    // Integrate sub-steps
                    for (i in 0 until subStepsPerFrame) {
                        currentParticle = RelativisticEngine.rk4Step(
                            state = currentParticle,
                            fields = field,
                            dt = dtSubStep,
                            timeSec = currentTimeSec
                        )
                        currentTimeSec += dtSubStep
                    }

                    // Check bounds to reset or wrap in cyclical configurations
                    if (currentParticle.position.magnitude() > 0.4) {
                        currentParticle = createInitialParticle(
                            currentState.activePreset,
                            currentState.selectedSpecies,
                            currentState.initialBeta
                        )
                    }

                    val updatedTimeNs = currentTimeSec * 1e9
                    val newPoint = generateTelemetryPoint(currentParticle, field, updatedTimeNs)

                    val updatedHistory = if (currentState.telemetryHistory.size >= maxHistorySize) {
                        currentState.telemetryHistory.drop(1) + newPoint
                    } else {
                        currentState.telemetryHistory + newPoint
                    }

                    _uiState.update { state ->
                        state.copy(
                            particle = currentParticle,
                            fieldEnvironment = field.copy(simulationTimeSec = currentTimeSec),
                            telemetryHistory = updatedHistory,
                            simulationTimeNs = updatedTimeNs,
                            totalStepsComputed = state.totalStepsComputed + subStepsPerFrame
                        )
                    }
                }
                // Target ~60 FPS update rate (16ms)
                delay(16)
            }
        }
    }

    fun togglePlayPause() {
        _uiState.update { it.copy(isRunning = !it.isRunning) }
    }

    fun stepSimulation() {
        val currentState = _uiState.value
        val dtSubStep = 5.0e-11
        var p = currentState.particle
        val field = currentState.fieldEnvironment
        val tSec = currentState.simulationTimeNs * 1e-9

        for (i in 0 until 4) {
            p = RelativisticEngine.rk4Step(p, field, dtSubStep, tSec + i * dtSubStep)
        }
        val newTimeNs = (tSec + 4 * dtSubStep) * 1e9
        val point = generateTelemetryPoint(p, field, newTimeNs)

        _uiState.update {
            it.copy(
                particle = p,
                telemetryHistory = (it.telemetryHistory + point).takeLast(maxHistorySize),
                simulationTimeNs = newTimeNs,
                totalStepsComputed = it.totalStepsComputed + 4
            )
        }
    }

    fun resetSimulation() {
        val s = _uiState.value
        val newP = createInitialParticle(s.activePreset, s.selectedSpecies, s.initialBeta)
        _uiState.update {
            it.copy(
                particle = newP,
                simulationTimeNs = 0.0,
                telemetryHistory = listOf(generateTelemetryPoint(newP, s.fieldEnvironment, 0.0))
            )
        }
    }

    fun selectPreset(preset: SimulationPreset) {
        val species = preset.recommendedSpecies
        val beta = preset.defaultBeta
        val particle = createInitialParticle(preset, species, beta)
        val field = FieldEnvironment(
            preset = preset,
            baseBFieldTesla = preset.defaultBFieldTesla,
            baseEFieldVPerM = preset.defaultEFieldVPerM
        )
        _uiState.update {
            it.copy(
                activePreset = preset,
                selectedSpecies = species,
                initialBeta = beta,
                fieldEnvironment = field,
                particle = particle,
                simulationTimeNs = 0.0,
                telemetryHistory = listOf(generateTelemetryPoint(particle, field, 0.0))
            )
        }
    }

    fun selectSpecies(species: ParticleSpecies) {
        val s = _uiState.value
        val particle = createInitialParticle(s.activePreset, species, s.initialBeta)
        _uiState.update {
            it.copy(
                selectedSpecies = species,
                particle = particle,
                telemetryHistory = listOf(generateTelemetryPoint(particle, s.fieldEnvironment, s.simulationTimeNs))
            )
        }
    }

    fun updateBeta(newBeta: Double) {
        val s = _uiState.value
        val particle = createInitialParticle(s.activePreset, s.selectedSpecies, newBeta)
        _uiState.update {
            it.copy(
                initialBeta = newBeta,
                particle = particle
            )
        }
    }

    fun updateBField(bTesla: Double) {
        _uiState.update {
            it.copy(fieldEnvironment = it.fieldEnvironment.copy(baseBFieldTesla = bTesla))
        }
    }

    fun updateEField(eVPerM: Double) {
        _uiState.update {
            it.copy(fieldEnvironment = it.fieldEnvironment.copy(baseEFieldVPerM = eVPerM))
        }
    }

    fun injectBeamBurst() {
        val s = _uiState.value
        val newParticle = createInitialParticle(s.activePreset, s.selectedSpecies, s.initialBeta)
        _uiState.update { it.copy(particle = newParticle) }
    }

    fun toggleVectors() {
        _uiState.update { it.copy(showVectors = !it.showVectors) }
    }

    fun toggleFieldGrid() {
        _uiState.update { it.copy(showFieldGrid = !it.showFieldGrid) }
    }

    fun setMetricChannel(channel: MetricChannel) {
        _uiState.update { it.copy(selectedMetricChannel = channel) }
    }
}
