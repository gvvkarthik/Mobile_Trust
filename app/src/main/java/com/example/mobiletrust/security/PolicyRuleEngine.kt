package com.example.mobiletrust.security

import com.example.mobiletrust.data.model.PolicyRule
import com.example.mobiletrust.data.model.RiskLevel
import com.example.mobiletrust.data.model.SecurityAction
import com.example.mobiletrust.data.model.SessionStatus
import com.example.mobiletrust.data.model.TrustInput
import com.example.mobiletrust.data.model.TrustPolicyConfig

data class PolicyDecision(
    val riskLevel: RiskLevel,
    val sessionStatus: SessionStatus,
    val securityAction: SecurityAction,
    val matchedRules: List<String>
)

object PolicyRuleEngine {

    fun thresholdAction(riskLevel: RiskLevel): SecurityAction = when (riskLevel) {
        RiskLevel.LOW -> SecurityAction.ALLOW_ACCESS
        RiskLevel.MEDIUM -> SecurityAction.SHOW_SECURITY_WARNING
        RiskLevel.HIGH -> SecurityAction.REQUIRE_REAUTHENTICATION
        RiskLevel.CRITICAL -> SecurityAction.TERMINATE_SESSION
    }

    fun evaluate(
        input: TrustInput,
        trustScore: Int,
        config: TrustPolicyConfig
    ): PolicyDecision {
        val riskLevel = RiskLevel.fromScore(trustScore, config.thresholds)
        val matched = config.rules.filter { it.matches(input, trustScore) }
        val action = SecurityAction.mostSevere(
            matched.map(PolicyRule::action) + thresholdAction(riskLevel)
        )

        return PolicyDecision(
            riskLevel = riskLevel,
            sessionStatus = action.sessionStatus,
            securityAction = action,
            matchedRules = matched.map(PolicyRule::name)
        )
    }
}
