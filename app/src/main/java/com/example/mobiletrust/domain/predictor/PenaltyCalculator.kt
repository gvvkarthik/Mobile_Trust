package com.example.mobiletrust.domain.predictor

import com.example.mobiletrust.data.model.PenaltyBreakdown
import com.example.mobiletrust.data.model.TrustInput

object PenaltyCalculator {

    const val BASE_SCORE = 100
    const val PENALTY_PER_FAILED_LOGIN = 5
    const val PENALTY_PER_TRANSITION = 3
    const val MAX_TRANSITION_PENALTY = 18

    fun breakdown(input: TrustInput): PenaltyBreakdown = PenaltyBreakdown(
        network = input.networkType.penalty,
        device = input.deviceSecurity.penalty,
        failedLogins = input.failedLoginAttempts * PENALTY_PER_FAILED_LOGIN,
        behaviour = input.behaviour.penalty,
        transitions = (input.networkTransitions * PENALTY_PER_TRANSITION)
            .coerceAtMost(MAX_TRANSITION_PENALTY)
    )

    fun score(input: TrustInput): Int =
        (BASE_SCORE - breakdown(input).total).coerceIn(0, 100)
}
