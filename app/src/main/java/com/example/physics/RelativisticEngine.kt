package com.example.physics

import com.example.model.FieldEnvironment
import com.example.model.ParticleSpecies
import com.example.model.ParticleState
import com.example.model.Vector3
import kotlin.math.sqrt

/**
 * High-accuracy 4th-Order Runge-Kutta (RK4) Relativistic Numerical Integrator.
 * Solves the coupled relativistic equations of motion for charged particles
 * in arbitrary electromagnetic field configurations.
 */
object RelativisticEngine {

    data class StateDerivative(
        val dR: Vector3, // Velocity v = dr/dt
        val dP: Vector3  // Lorentz Force F = dp/dt
    ) {
        operator fun plus(other: StateDerivative): StateDerivative =
            StateDerivative(dR + other.dR, dP + other.dP)

        operator fun times(scalar: Double): StateDerivative =
            StateDerivative(dR * scalar, dP * scalar)
    }

    /**
     * Converts relativistic momentum p to velocity v:
     * v = p / sqrt(m0^2 + p^2 / c^2)
     */
    fun momentumToVelocity(p: Vector3, m0: Double): Vector3 {
        val cSq = ParticleState.C_LIGHT_SQ
        val pSq = p.magnitudeSquared()
        val denom = sqrt(m0 * m0 + pSq / cSq)
        return if (denom > 1e-35) p / denom else Vector3.ZERO
    }

    /**
     * Evaluates state derivatives f(t, r, p) = (dr/dt, dp/dt)
     */
    private fun evaluateDerivative(
        r: Vector3,
        p: Vector3,
        timeSec: Double,
        species: ParticleSpecies,
        fields: FieldEnvironment
    ): StateDerivative {
        val v = momentumToVelocity(p, species.restMassKg)
        val eField = fields.evaluateE(r)
        val bField = fields.evaluateB(r)
        val vCrossB = v.cross(bField)
        val lorentzForce = (eField + vCrossB) * species.chargeCoulombs
        return StateDerivative(dR = v, dP = lorentzForce)
    }

    /**
     * Executes one RK4 step of size dt.
     */
    fun rk4Step(
        state: ParticleState,
        fields: FieldEnvironment,
        dt: Double,
        timeSec: Double
    ): ParticleState {
        val r0 = state.position
        val p0 = state.momentum
        val species = state.species

        // k1
        val k1 = evaluateDerivative(r0, p0, timeSec, species, fields)

        // k2
        val rK2 = r0 + k1.dR * (0.5 * dt)
        val pK2 = p0 + k1.dP * (0.5 * dt)
        val k2 = evaluateDerivative(rK2, pK2, timeSec + 0.5 * dt, species, fields)

        // k3
        val rK3 = r0 + k2.dR * (0.5 * dt)
        val pK3 = p0 + k2.dP * (0.5 * dt)
        val k3 = evaluateDerivative(rK3, pK3, timeSec + 0.5 * dt, species, fields)

        // k4
        val rK4 = r0 + k3.dR * dt
        val pK4 = p0 + k3.dP * dt
        val k4 = evaluateDerivative(rK4, pK4, timeSec + dt, species, fields)

        // Weighted sum
        val deltaR = (k1.dR + k2.dR * 2.0 + k3.dR * 2.0 + k4.dR) * (dt / 6.0)
        val deltaP = (k1.dP + k2.dP * 2.0 + k3.dP * 2.0 + k4.dP) * (dt / 6.0)

        val newPosition = r0 + deltaR
        val newMomentum = p0 + deltaP

        // Append to trajectory with spatial pruning
        val newTrajectory = if (state.trajectory.size > 250) {
            state.trajectory.drop(1) + newPosition
        } else {
            state.trajectory + newPosition
        }

        return state.copy(
            position = newPosition,
            momentum = newMomentum,
            trajectory = newTrajectory
        )
    }

    /**
     * Factory to initialize a particle with given relativistic beta (v/c).
     */
    fun createParticle(
        species: ParticleSpecies,
        initialPosition: Vector3,
        initialDirection: Vector3,
        beta: Double
    ): ParticleState {
        val c = ParticleState.C_LIGHT
        val safeBeta = beta.coerceIn(0.001, 0.9999)
        val gamma = 1.0 / sqrt(1.0 - safeBeta * safeBeta)
        val vMag = safeBeta * c
        val velocityVector = initialDirection.normalized() * vMag
        val momentumVector = velocityVector * (gamma * species.restMassKg)

        val color = when (species) {
            ParticleSpecies.ELECTRON -> 0xFF00E5FF // Cyan
            ParticleSpecies.POSITRON -> 0xFFFF4081 // Pink
            ParticleSpecies.PROTON -> 0xFFFF9100   // Orange
            ParticleSpecies.ALPHA -> 0xFFD500F9    // Violet
            ParticleSpecies.MUON -> 0xFF00E676     // Emerald
        }

        return ParticleState(
            species = species,
            position = initialPosition,
            momentum = momentumVector,
            colorHex = color,
            trajectory = listOf(initialPosition)
        )
    }
}
