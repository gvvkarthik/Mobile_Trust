package com.example.mobiletrust.domain.predictor

import com.example.mobiletrust.data.model.PenaltyBreakdown
import com.example.mobiletrust.data.model.TrustInput
import com.example.mobiletrust.data.model.TrustPolicyConfig

data class TrustPrediction(
    val ruleScore: Int,
    val mlScore: Int,
    val blendedScore: Int,
    val degradationProbability: Double,
    val penalties: PenaltyBreakdown,
    val inferenceNanos: Long
)

interface TrustPredictor {
    fun predict(input: TrustInput, config: TrustPolicyConfig): TrustPrediction
}
