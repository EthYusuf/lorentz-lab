package com.example.model

import kotlin.math.sqrt

/**
 * High-performance 3D vector for relativistic mechanics.
 * All units in standard SI or normalized simulation units.
 */
data class Vector3(
    val x: Double = 0.0,
    val y: Double = 0.0,
    val z: Double = 0.0
) {
    operator fun plus(other: Vector3): Vector3 =
        Vector3(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: Vector3): Vector3 =
        Vector3(x - other.x, y - other.y, z - other.z)

    operator fun times(scalar: Double): Vector3 =
        Vector3(x * scalar, y * scalar, z * scalar)

    operator fun div(scalar: Double): Vector3 =
        Vector3(x / scalar, y / scalar, z / scalar)

    operator fun unaryMinus(): Vector3 =
        Vector3(-x, -y, -z)

    fun dot(other: Vector3): Double =
        x * other.x + y * other.y + z * other.z

    /**
     * Vector cross product: A x B = (Ay*Bz - Az*By, Az*Bx - Ax*Bz, Ax*By - Ay*Bx)
     */
    fun cross(other: Vector3): Vector3 = Vector3(
        x = y * other.z - z * other.y,
        y = z * other.x - x * other.z,
        z = x * other.y - y * other.x
    )

    fun magnitudeSquared(): Double = x * x + y * y + z * z

    fun magnitude(): Double = sqrt(magnitudeSquared())

    fun normalized(): Vector3 {
        val mag = magnitude()
        return if (mag > 1e-18) this / mag else ZERO
    }

    companion object {
        val ZERO = Vector3(0.0, 0.0, 0.0)
        val UNIT_X = Vector3(1.0, 0.0, 0.0)
        val UNIT_Y = Vector3(0.0, 1.0, 0.0)
        val UNIT_Z = Vector3(0.0, 0.0, 1.0)
    }
}
