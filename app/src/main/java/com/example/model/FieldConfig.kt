package com.example.model

import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

enum class SimulationPreset(
    val title: String,
    val description: String,
    val recommendedSpecies: ParticleSpecies,
    val defaultBeta: Double,
    val defaultBFieldTesla: Double,
    val defaultEFieldVPerM: Double
) {
    SYNCHROTRON_DIPOLE(
        title = "Relativistic Cyclotron",
        description = "High-energy circular particle acceleration under uniform magnetic dipole field B_z.",
        recommendedSpecies = ParticleSpecies.ELECTRON,
        defaultBeta = 0.85,
        defaultBFieldTesla = 0.05,
        defaultEFieldVPerM = 0.0
    ),
    WIEN_VELOCITY_SELECTOR(
        title = "Wien Filter (Crossed E ⊥ B)",
        description = "Orthogonal electric and magnetic fields selecting particles where v = E / B with zero net deflection.",
        recommendedSpecies = ParticleSpecies.PROTON,
        defaultBeta = 0.40,
        defaultBFieldTesla = 0.02,
        defaultEFieldVPerM = 2.4e6
    ),
    MAGNETIC_MIRROR(
        title = "Magnetic Mirror (Confinement Bottle)",
        description = "Non-uniform magnetic gradient causing magnetic reflection and plasma confinement via adiabatic invariants.",
        recommendedSpecies = ParticleSpecies.ELECTRON,
        defaultBeta = 0.70,
        defaultBFieldTesla = 0.04,
        defaultEFieldVPerM = 0.0
    ),
    ELECTRIC_QUADRUPOLE(
        title = "Quadrupole Beam Focusing",
        description = "Hyperbolic field arrangement focusing the charged particle beam along one axis while defocusing along the other.",
        recommendedSpecies = ParticleSpecies.PROTON,
        defaultBeta = 0.35,
        defaultBFieldTesla = 0.01,
        defaultEFieldVPerM = 1.5e5
    )
}

/**
 * Computes the Electromagnetic Field vector (E, B) at any spatial coordinate r=(x,y,z).
 */
data class FieldEnvironment(
    val preset: SimulationPreset = SimulationPreset.SYNCHROTRON_DIPOLE,
    val baseBFieldTesla: Double = 0.05, // Up to a few Tesla
    val baseEFieldVPerM: Double = 0.0,
    val rfFrequencyHz: Double = 1e6, // RF cavity for cyclotron acceleration
    val simulationTimeSec: Double = 0.0
) {
    /**
     * Evaluates Magnetic Field B(r) in Tesla
     */
    fun evaluateB(r: Vector3): Vector3 {
        return when (preset) {
            SimulationPreset.SYNCHROTRON_DIPOLE -> {
                // Uniform magnetic field along +Z
                Vector3(0.0, 0.0, baseBFieldTesla)
            }
            SimulationPreset.WIEN_VELOCITY_SELECTOR -> {
                // B field directed along +Z
                Vector3(0.0, 0.0, baseBFieldTesla)
            }
            SimulationPreset.MAGNETIC_MIRROR -> {
                // Magnetic bottle: B_z increases away from origin z=0, div B = 0 requires radial component B_r = - (r/2) * dB_z/dz
                val z = r.z
                val x = r.x
                val y = r.y
                val b0 = baseBFieldTesla
                val mirrorRatio = 2.5
                val lengthScale = 0.2 // 20 cm bottle scale
                val bz = b0 * (1.0 + (mirrorRatio - 1.0) * (z * z) / (lengthScale * lengthScale))
                val dbzDz = b0 * (mirrorRatio - 1.0) * (2.0 * z) / (lengthScale * lengthScale)
                val bx = -0.5 * x * dbzDz
                val by = -0.5 * y * dbzDz
                Vector3(bx, by, bz)
            }
            SimulationPreset.ELECTRIC_QUADRUPOLE -> {
                // Quadrupole magnetic lens: B = G * (y*x_hat + x*y_hat)
                val gradient = baseBFieldTesla / 0.1
                Vector3(gradient * r.y, gradient * r.x, 0.0)
            }
        }
    }

    /**
     * Evaluates Electric Field E(r) in V/m
     */
    fun evaluateE(r: Vector3): Vector3 {
        return when (preset) {
            SimulationPreset.SYNCHROTRON_DIPOLE -> {
                // Optional central RF accelerating kick across the gap |x| < 0.02
                if (baseEFieldVPerM > 0.0 && kotlin.math.abs(r.x) < 0.03) {
                    val phase = 2.0 * Math.PI * rfFrequencyHz * simulationTimeSec
                    val eX = baseEFieldVPerM * sin(phase)
                    Vector3(eX, 0.0, 0.0)
                } else {
                    Vector3.ZERO
                }
            }
            SimulationPreset.WIEN_VELOCITY_SELECTOR -> {
                // E field directed along +Y perpendicular to B (along +Z) and beam (along +X)
                Vector3(0.0, baseEFieldVPerM, 0.0)
            }
            SimulationPreset.MAGNETIC_MIRROR -> {
                Vector3.ZERO
            }
            SimulationPreset.ELECTRIC_QUADRUPOLE -> {
                // Electric quadrupole component
                val gradient = baseEFieldVPerM / 0.1
                Vector3(gradient * r.x, -gradient * r.y, 0.0)
            }
        }
    }
}
