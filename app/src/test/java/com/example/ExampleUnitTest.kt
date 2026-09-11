package com.example

import com.example.model.FieldEnvironment
import com.example.model.ParticleSpecies
import com.example.model.SimulationPreset
import com.example.model.Vector3
import com.example.physics.RelativisticEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sqrt

class RelativisticPhysicsUnitTest {

    @Test
    fun testLorentzFactorAnalyticalConsistency() {
        val beta = 0.8660254 // 0.866c corresponds to gamma ~ 2.0
        val particle = RelativisticEngine.createParticle(
            species = ParticleSpecies.ELECTRON,
            initialPosition = Vector3.ZERO,
            initialDirection = Vector3.UNIT_X,
            beta = beta
        )

        val expectedGamma = 1.0 / sqrt(1.0 - beta * beta)
        assertEquals(expectedGamma, particle.gamma, 1e-4)
        assertTrue(particle.gamma > 1.99 && particle.gamma < 2.01)
    }

    @Test
    fun testEnergyConservationInUniformMagneticField() {
        // In a static magnetic field with E = 0, magnetic force does no work: W = F . v = q(v x B) . v = 0
        // Hence, kinetic energy and speed must be strictly conserved
        val initialBeta = 0.75
        var particle = RelativisticEngine.createParticle(
            species = ParticleSpecies.PROTON,
            initialPosition = Vector3(0.0, 0.0, 0.0),
            initialDirection = Vector3(1.0, 0.0, 0.0),
            beta = initialBeta
        )

        val initialEk = particle.kineticEnergyMeV
        val fields = FieldEnvironment(
            preset = SimulationPreset.SYNCHROTRON_DIPOLE,
            baseBFieldTesla = 0.05,
            baseEFieldVPerM = 0.0
        )

        val dt = 1e-10 // 0.1 ns
        var time = 0.0
        for (i in 0 until 50) {
            particle = RelativisticEngine.rk4Step(particle, fields, dt, time)
            time += dt
        }

        val finalEk = particle.kineticEnergyMeV
        val relativeError = abs((finalEk - initialEk) / initialEk)
        assertTrue("Energy drift must be under 0.01% with RK-4", relativeError < 1e-4)
    }

    @Test
    fun testWienFilterZeroDeflectionCondition() {
        // In a Wien filter, if v = E / B, the electric force qE and magnetic force q(v x B) cancel exactly
        val bTesla = 0.05
        val beta = 0.20
        val v = beta * com.example.model.ParticleState.C_LIGHT
        val requiredE = v * bTesla

        val fields = FieldEnvironment(
            preset = SimulationPreset.WIEN_VELOCITY_SELECTOR,
            baseBFieldTesla = bTesla,
            baseEFieldVPerM = requiredE
        )

        val particle = RelativisticEngine.createParticle(
            species = ParticleSpecies.PROTON,
            initialPosition = Vector3(0.0, 0.0, 0.0),
            initialDirection = Vector3(1.0, 0.0, 0.0), // Along +X
            beta = beta
        )

        // E is along +Y, v is along +X, B is along +Z:
        // F_E = q * E_y * y_hat
        // F_B = q * (v_x x_hat cross B_z z_hat) = q * v_x * B_z * (-y_hat)
        // Total F_y = q (E - vB) = 0
        val vVec = particle.velocity
        val eVec = fields.evaluateE(particle.position)
        val bVec = fields.evaluateB(particle.position)
        val lorentzForce = (eVec + vVec.cross(bVec)) * particle.species.chargeCoulombs

        assertEquals(0.0, lorentzForce.y, 1e-12)
    }
}
