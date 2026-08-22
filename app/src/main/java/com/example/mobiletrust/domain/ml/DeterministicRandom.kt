package com.example.mobiletrust.domain.ml

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sqrt

class DeterministicRandom(seed: Long) {

    private var state: Long = seed

    fun nextDouble(): Double {
        state = state * MULTIPLIER + INCREMENT
        val bits = (state ushr 11) and MANTISSA_MASK
        return bits.toDouble() / MANTISSA_SCALE
    }

    fun nextInt(bound: Int): Int = (nextDouble() * bound).toInt().coerceIn(0, bound - 1)

    fun nextBoolean(probability: Double): Boolean = nextDouble() < probability

    fun nextGaussian(): Double {
        val u1 = nextDouble().coerceAtLeast(1e-12)
        val u2 = nextDouble()
        return sqrt(-2.0 * ln(u1)) * cos(2.0 * PI * u2)
    }

    private companion object {
        const val MULTIPLIER = 6364136223846793005L
        const val INCREMENT = 1442695040888963407L
        const val MANTISSA_MASK = (1L shl 53) - 1
        const val MANTISSA_SCALE = 9007199254740992.0
    }
}
