package com.example.mobiletrust.domain.predictor

import com.example.mobiletrust.data.model.TrustInput
import com.example.mobiletrust.data.model.TrustPolicyConfig

class RuleBasedTrustPredictor : TrustPredictor {

    override fun predict(input: TrustInput, config: TrustPolicyConfig): TrustPrediction {
        val startedAt = System.nanoTime()
        val penalties = PenaltyCalculator.breakdown(input)
        val ruleScore = (PenaltyCalculator.BASE_SCORE - penalties.total).coerceIn(0, 100)

        return TrustPrediction(
            ruleScore = ruleScore,
            mlScore = ruleScore,
            blendedScore = ruleScore,
            degradationProbability = 1.0 - ruleScore / 100.0,
            penalties = penalties,
            inferenceNanos = System.nanoTime() - startedAt
        )
    }
}
