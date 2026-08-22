package com.example.mobiletrust

import com.example.mobiletrust.data.model.BehaviourStatus
import com.example.mobiletrust.data.model.DeviceSecurityStatus
import com.example.mobiletrust.data.model.NetworkType
import com.example.mobiletrust.data.model.RiskLevel
import com.example.mobiletrust.data.model.SecurityAction
import com.example.mobiletrust.data.model.SessionStatus
import com.example.mobiletrust.data.model.TrustInput
import com.example.mobiletrust.data.model.TrustPolicyConfig
import com.example.mobiletrust.data.model.UserRole
import com.example.mobiletrust.domain.engine.TrustEngine
import com.example.mobiletrust.domain.predictor.PenaltyCalculator
import com.example.mobiletrust.domain.predictor.RuleBasedTrustPredictor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrustScoringTest {

    private val config = TrustPolicyConfig()
    private val rulePredictor = RuleBasedTrustPredictor()

    private fun ruleScore(input: TrustInput) = rulePredictor.predict(input, config).ruleScore

    @Test
    fun secureBaselineScoresNinetyFive() {
        assertEquals(95, ruleScore(TrustInput()))
    }

    @Test
    fun mobileNetworkWithOneFailedLoginScoresEighty() {
        val input = TrustInput(
            networkType = NetworkType.MOBILE_4G,
            failedLoginAttempts = 1
        )
        assertEquals(80, ruleScore(input))
    }

    @Test
    fun fullyDegradedContextScoresFive() {
        val input = TrustInput(
            networkType = NetworkType.PUBLIC_WIFI,
            deviceSecurity = DeviceSecurityStatus.COMPROMISED,
            failedLoginAttempts = 3,
            behaviour = BehaviourStatus.SUSPICIOUS
        )
        assertEquals(5, ruleScore(input))
    }

    @Test
    fun scoreIsClampedToZero() {
        val input = TrustInput(
            networkType = NetworkType.PUBLIC_WIFI,
            deviceSecurity = DeviceSecurityStatus.COMPROMISED,
            failedLoginAttempts = 5,
            behaviour = BehaviourStatus.SUSPICIOUS,
            networkTransitions = 6
        )
        assertEquals(0, ruleScore(input))
    }

    @Test
    fun transitionPenaltyIsCapped() {
        val breakdown = PenaltyCalculator.breakdown(TrustInput(networkTransitions = 20))
        assertEquals(PenaltyCalculator.MAX_TRANSITION_PENALTY, breakdown.transitions)
    }

    @Test
    fun riskLevelsFollowConfiguredThresholds() {
        val thresholds = config.thresholds
        assertEquals(RiskLevel.LOW, RiskLevel.fromScore(100, thresholds))
        assertEquals(RiskLevel.LOW, RiskLevel.fromScore(71, thresholds))
        assertEquals(RiskLevel.MEDIUM, RiskLevel.fromScore(70, thresholds))
        assertEquals(RiskLevel.MEDIUM, RiskLevel.fromScore(41, thresholds))
        assertEquals(RiskLevel.HIGH, RiskLevel.fromScore(40, thresholds))
        assertEquals(RiskLevel.HIGH, RiskLevel.fromScore(21, thresholds))
        assertEquals(RiskLevel.CRITICAL, RiskLevel.fromScore(20, thresholds))
        assertEquals(RiskLevel.CRITICAL, RiskLevel.fromScore(0, thresholds))
    }

    @Test
    fun hybridScoreSitsBetweenRuleAndMlScores() {
        val engine = TrustEngine()
        val input = TrustInput(
            networkType = NetworkType.PUBLIC_WIFI,
            failedLoginAttempts = 3,
            behaviour = BehaviourStatus.SUSPICIOUS
        )
        val result = engine.evaluate(input, config)

        val lower = minOf(result.ruleScore, result.mlScore)
        val upper = maxOf(result.ruleScore, result.mlScore)
        assertTrue(
            "Blended score ${result.trustScore} outside [$lower, $upper]",
            result.trustScore in lower..upper
        )
    }

    @Test
    fun scoringBlendHonoursConfiguredWeights() {
        val engine = TrustEngine()
        val input = TrustInput(networkType = NetworkType.MOBILE_4G)

        val ruleOnly = engine.evaluate(input, config.withMlWeight(0.0))
        val mlOnly = engine.evaluate(input, config.withMlWeight(1.0))

        assertEquals(ruleOnly.ruleScore, ruleOnly.trustScore)
        assertEquals(mlOnly.mlScore, mlOnly.trustScore)
    }

    @Test
    fun trustedBaselineKeepsSessionActive() {
        val result = TrustEngine().evaluate(TrustInput(), config)
        assertEquals(RiskLevel.LOW, result.riskLevel)
        assertEquals(SessionStatus.ACTIVE, result.sessionStatus)
        assertEquals(SecurityAction.ALLOW_ACCESS, result.securityAction)
        assertTrue(result.matchedRules.isEmpty())
    }

    @Test
    fun publicNetworkDegradesTrustWithinOneEvaluation() {
        val engine = TrustEngine()
        val baseline = engine.evaluate(TrustInput(), config)
        val degraded = engine.evaluate(
            TrustInput(
                networkType = NetworkType.PUBLIC_WIFI,
                userRole = UserRole.COMMANDER,
                networkTransitions = 1
            ),
            config
        )

        assertTrue(degraded.trustScore < baseline.trustScore)
        assertTrue(degraded.riskLevel.severity > baseline.riskLevel.severity)
    }
}
