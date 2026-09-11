package com.example.model

/**
 * Snapshot of real-time telemetry variables for analytical plotting.
 */
data class TelemetryPoint(
    val timeNs: Double,
    val beta: Double,              // v / c
    val gamma: Double,             // Lorentz factor
    val kineticEnergyMeV: Double,  // Kinetic energy (MeV)
    val momentumMagnitude: Double, // |p| (kg * m/s)
    val forceMagnitude: Double,    // |F_Lorentz| (N)
    val posX: Double,              // Position X (m)
    val posY: Double,              // Position Y (m)
    val posZ: Double               // Position Z (m)
)
