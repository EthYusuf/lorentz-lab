package com.example.model

import kotlin.math.sqrt

enum class ParticleSpecies(
    val displayName: String,
    val symbol: String,
    val restMassKg: Double,
    val restEnergyMeV: Double,
    val chargeCoulombs: Double,
    val chargeSign: Int
) {
    ELECTRON(
        displayName = "Electron",
        symbol = "e⁻",
        restMassKg = 9.1093837e-31,
        restEnergyMeV = 0.51099895,
        chargeCoulombs = -1.60217663e-19,
        chargeSign = -1
    ),
    POSITRON(
        displayName = "Positron",
        symbol = "e⁺",
        restMassKg = 9.1093837e-31,
        restEnergyMeV = 0.51099895,
        chargeCoulombs = 1.60217663e-19,
        chargeSign = 1
    ),
    PROTON(
        displayName = "Proton",
        symbol = "p⁺",
        restMassKg = 1.67262192e-27,
        restEnergyMeV = 938.272,
        chargeCoulombs = 1.60217663e-19,
        chargeSign = 1
    ),
    ALPHA(
        displayName = "Alpha Particle",
        symbol = "α²⁺",
        restMassKg = 6.64465723e-27,
        restEnergyMeV = 3727.379,
        chargeCoulombs = 3.20435326e-19,
        chargeSign = 2
    ),
    MUON(
        displayName = "Muon",
        symbol = "μ⁻",
        restMassKg = 1.883531627e-28,
        restEnergyMeV = 105.658,
        chargeCoulombs = -1.60217663e-19,
        chargeSign = -1
    )
}

/**
 * State of a charged relativistic particle at simulation time t.
 */
data class ParticleState(
    val species: ParticleSpecies,
    val position: Vector3,
    val momentum: Vector3, // Relativistic momentum p = gamma * m0 * v
    val colorHex: Long = 0xFF00E5FF,
    val trajectory: List<Vector3> = emptyList()
) {
    companion object {
        // Physical Constants
        const val C_LIGHT: Double = 299792458.0 // m/s
        const val C_LIGHT_SQ: Double = C_LIGHT * C_LIGHT
        const val ELEMENTARY_CHARGE: Double = 1.60217663e-19 // C
    }

    /**
     * Lorentz Factor: gamma = sqrt(1 + (p / (m0 * c))^2)
     */
    val gamma: Double
        get() {
            val pSquared = momentum.magnitudeSquared()
            val m0 = species.restMassKg
            val pOverM0C = sqrt(pSquared) / (m0 * C_LIGHT)
            return sqrt(1.0 + pOverM0C * pOverM0C)
        }

    /**
     * Relativistic Velocity vector: v = p / (gamma * m0) = p / sqrt(m0^2 + p^2/c^2)
     */
    val velocity: Vector3
        get() {
            val m0 = species.restMassKg
            val pSquared = momentum.magnitudeSquared()
            val denom = sqrt(m0 * m0 + pSquared / C_LIGHT_SQ)
            return if (denom > 1e-35) momentum / denom else Vector3.ZERO
        }

    /**
     * Relativistic velocity ratio beta = v / c
     */
    val beta: Double
        get() {
            val vMag = velocity.magnitude()
            return (vMag / C_LIGHT).coerceIn(0.0, 0.9999999)
        }

    /**
     * Kinetic Energy in MeV: E_k = (gamma - 1) * m0 * c^2
     */
    val kineticEnergyMeV: Double
        get() = (gamma - 1.0) * species.restEnergyMeV

    /**
     * Total Energy in MeV: E = gamma * m0 * c^2
     */
    val totalEnergyMeV: Double
        get() = gamma * species.restEnergyMeV

    /**
     * Relativistic Gyroradius (Larmor radius) in meters for given B field magnitude:
     * r_L = p_perp / (|q| * B) = gamma * m0 * v_perp / (|q| * B)
     */
    fun gyroradius(bMag: Double): Double {
        val q = kotlin.math.abs(species.chargeCoulombs)
        return if (bMag > 1e-12 && q > 1e-25) {
            momentum.magnitude() / (q * bMag)
        } else {
            Double.POSITIVE_INFINITY
        }
    }
}
