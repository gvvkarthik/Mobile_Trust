package com.example.mobiletrust.domain.predictor

import com.example.mobiletrust.data.model.RiskLevel
import com.example.mobiletrust.data.model.TrustInput
import com.example.mobiletrust.data.model.TrustResult
import com.example.mobiletrust.security.SecurityPolicyEngine

class RuleBasedTrustPredictor : TrustPredictor {

    override fun predict(input: TrustInput): TrustResult {
        val baseScore = 100
        val networkPenalty = input.networkType.penalty
        val deviceSecurityPenalty = input.deviceSecurity.penalty
        val failedLoginsPenalty = input.failedLoginAttempts * 5
        val behaviourPenalty = input.behaviour.penalty

        val totalPenalty = networkPenalty + deviceSecurityPenalty + failedLoginsPenalty + behaviourPenalty
        val rawScore = baseScore - totalPenalty
        val clampedScore = rawScore.coerceIn(0, 100)

        val riskLevel = RiskLevel.fromScore(clampedScore)
        val policyDecision = SecurityPolicyEngine.evaluate(clampedScore)

        return TrustResult(
            trustScore = clampedScore,
            riskLevel = riskLevel,
            sessionStatus = policyDecision.sessionStatus,
            securityAction = policyDecision.securityAction,
            input = input,
            baseScore = baseScore,
            networkPenalty = networkPenalty,
            deviceSecurityPenalty = deviceSecurityPenalty,
            failedLoginsPenalty = failedLoginsPenalty,
            behaviourPenalty = behaviourPenalty
        )
    }
}
