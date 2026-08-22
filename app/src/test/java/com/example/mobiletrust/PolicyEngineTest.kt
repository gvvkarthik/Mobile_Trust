package com.example.mobiletrust

import com.example.mobiletrust.data.model.BehaviourStatus
import com.example.mobiletrust.data.model.DeviceSecurityStatus
import com.example.mobiletrust.data.model.NetworkType
import com.example.mobiletrust.data.model.RiskLevel
import com.example.mobiletrust.data.model.SecurityAction
import com.example.mobiletrust.data.model.SessionStatus
import com.example.mobiletrust.data.model.TrustInput
import com.example.mobiletrust.data.model.TrustPolicyConfig
import com.example.mobiletrust.data.model.TrustThresholds
import com.example.mobiletrust.data.model.UserRole
import com.example.mobiletrust.security.PolicyRuleEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PolicyEngineTest {

    private val config = TrustPolicyConfig()
    private val trustedInput = TrustInput()

    @Test
    fun thresholdBoundariesMapToSessionPolicies() {
        assertEquals(SessionStatus.ACTIVE, evaluate(trustedInput, 100).sessionStatus)
        assertEquals(SessionStatus.ACTIVE, evaluate(trustedInput, 71).sessionStatus)
        assertEquals(SessionStatus.WARNING, evaluate(trustedInput, 70).sessionStatus)
        assertEquals(SessionStatus.WARNING, evaluate(trustedInput, 41).sessionStatus)
        assertEquals(SessionStatus.REAUTH_REQUIRED, evaluate(trustedInput, 40).sessionStatus)
        assertEquals(SessionStatus.REAUTH_REQUIRED, evaluate(trustedInput, 21).sessionStatus)
        assertEquals(SessionStatus.TERMINATED, evaluate(trustedInput, 20).sessionStatus)
        assertEquals(SessionStatus.TERMINATED, evaluate(trustedInput, 0).sessionStatus)
    }

    @Test
    fun customThresholdsChangeRiskBanding() {
        val strict = config.copy(thresholds = TrustThresholds(lowMin = 90, mediumMin = 75, highMin = 50))
        val decision = PolicyRuleEngine.evaluate(trustedInput, 80, strict)

        assertEquals(RiskLevel.MEDIUM, decision.riskLevel)
        assertEquals(SecurityAction.SHOW_SECURITY_WARNING, decision.securityAction)
    }

    @Test
    fun publicNetworkRoleGateBlocksUnapprovedRoles() {
        val input = TrustInput(
            networkType = NetworkType.PUBLIC_WIFI,
            userRole = UserRole.GUEST
        )
        val decision = PolicyRuleEngine.evaluate(input, 95, config)

        assertEquals(SecurityAction.TERMINATE_SESSION, decision.securityAction)
        assertEquals(SessionStatus.TERMINATED, decision.sessionStatus)
        assertTrue(decision.matchedRules.contains("Public Network Role Gate"))
    }

    @Test
    fun publicNetworkRoleGateAllowsApprovedRoles() {
        val input = TrustInput(
            networkType = NetworkType.PUBLIC_WIFI,
            userRole = UserRole.COMMANDER
        )
        val decision = PolicyRuleEngine.evaluate(input, 95, config)

        assertEquals(SecurityAction.ALLOW_ACCESS, decision.securityAction)
        assertTrue(decision.matchedRules.isEmpty())
    }

    @Test
    fun disablingARuleRemovesItsEnforcement() {
        val input = TrustInput(
            networkType = NetworkType.PUBLIC_WIFI,
            userRole = UserRole.GUEST
        )
        val relaxed = config.withRuleEnabled("public-network-role-gate", false)
        val decision = PolicyRuleEngine.evaluate(input, 95, relaxed)

        assertEquals(SecurityAction.ALLOW_ACCESS, decision.securityAction)
        assertFalse(decision.matchedRules.contains("Public Network Role Gate"))
    }

    @Test
    fun rulesEscalateButNeverRelaxThresholdDecisions() {
        val input = TrustInput(
            networkType = NetworkType.SECURE_WIFI,
            networkTransitions = 5
        )
        val decision = PolicyRuleEngine.evaluate(input, 10, config)

        assertTrue(decision.matchedRules.contains("Rapid Transition Watch"))
        assertEquals(SecurityAction.TERMINATE_SESSION, decision.securityAction)
    }

    @Test
    fun compromisedDeviceAlwaysForcesReauthentication() {
        val input = TrustInput(deviceSecurity = DeviceSecurityStatus.COMPROMISED)
        val decision = PolicyRuleEngine.evaluate(input, 100, config)

        assertEquals(SecurityAction.REQUIRE_REAUTHENTICATION, decision.securityAction)
        assertTrue(decision.matchedRules.contains("Compromised Device Re-Auth"))
    }

    @Test
    fun bruteForceLockoutTerminatesSession() {
        val input = TrustInput(failedLoginAttempts = 5)
        val decision = PolicyRuleEngine.evaluate(input, 100, config)

        assertEquals(SecurityAction.TERMINATE_SESSION, decision.securityAction)
        assertTrue(decision.matchedRules.contains("Brute Force Lockout"))
    }

    @Test
    fun disabledRulesDoNotMatchUntilEnabled() {
        val input = TrustInput(
            networkType = NetworkType.MOBILE_4G,
            behaviour = BehaviourStatus.SUSPICIOUS
        )

        val defaultDecision = PolicyRuleEngine.evaluate(input, 100, config)
        assertFalse(defaultDecision.matchedRules.contains("Untrusted Network Anomaly"))

        val enabled = config.withRuleEnabled("untrusted-suspicious-combo", true)
        val enabledDecision = PolicyRuleEngine.evaluate(input, 100, enabled)
        assertTrue(enabledDecision.matchedRules.contains("Untrusted Network Anomaly"))
        assertEquals(SecurityAction.REQUIRE_REAUTHENTICATION, enabledDecision.securityAction)
    }

    @Test
    fun mostSevereActionWins() {
        val actions = listOf(
            SecurityAction.ALLOW_ACCESS,
            SecurityAction.REQUIRE_REAUTHENTICATION,
            SecurityAction.SHOW_SECURITY_WARNING
        )
        assertEquals(SecurityAction.REQUIRE_REAUTHENTICATION, SecurityAction.mostSevere(actions))
        assertEquals(SecurityAction.ALLOW_ACCESS, SecurityAction.mostSevere(emptyList()))
    }

    private fun evaluate(input: TrustInput, score: Int) =
        PolicyRuleEngine.evaluate(input, score, config)
}
