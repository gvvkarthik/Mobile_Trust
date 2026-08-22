package com.example.mobiletrust.domain.engine

import com.example.mobiletrust.data.model.TrustInput
import com.example.mobiletrust.data.model.TrustPolicyConfig
import com.example.mobiletrust.data.model.TrustResult
import com.example.mobiletrust.domain.predictor.HybridTrustPredictor
import com.example.mobiletrust.domain.predictor.TrustPredictor
import com.example.mobiletrust.security.PolicyRuleEngine

class TrustEngine(
    val predictor: TrustPredictor = HybridTrustPredictor()
) {

    fun evaluate(input: TrustInput, config: TrustPolicyConfig): TrustResult {
        val prediction = predictor.predict(input, config)
        val decision = PolicyRuleEngine.evaluate(input, prediction.blendedScore, config)

        return TrustResult(
            input = input,
            trustScore = prediction.blendedScore,
            ruleScore = prediction.ruleScore,
            mlScore = prediction.mlScore,
            degradationProbability = prediction.degradationProbability,
            riskLevel = decision.riskLevel,
            sessionStatus = decision.sessionStatus,
            securityAction = decision.securityAction,
            matchedRules = decision.matchedRules,
            penalties = prediction.penalties,
            inferenceNanos = prediction.inferenceNanos
        )
    }
}
