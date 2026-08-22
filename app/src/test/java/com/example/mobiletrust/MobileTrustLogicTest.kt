package com.example.mobiletrust

import com.example.mobiletrust.data.model.AuditLogType
import com.example.mobiletrust.data.model.BehaviourStatus
import com.example.mobiletrust.data.model.DeviceSecurityStatus
import com.example.mobiletrust.data.model.NetworkType
import com.example.mobiletrust.data.model.RiskLevel
import com.example.mobiletrust.data.model.SecurityAction
import com.example.mobiletrust.data.model.SessionStatus
import com.example.mobiletrust.data.model.TrustInput
import com.example.mobiletrust.domain.predictor.RuleBasedTrustPredictor
import com.example.mobiletrust.security.AuditLogger
import com.example.mobiletrust.security.SecurityPolicyEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MobileTrustLogicTest {

    private lateinit var predictor: RuleBasedTrustPredictor
    private lateinit var auditLogger: AuditLogger

    @Before
    fun setUp() {
        predictor = RuleBasedTrustPredictor()
        auditLogger = AuditLogger()
    }

    @Test
    fun testInitialDefaultBaseline() {
        val input = TrustInput(
            networkType = NetworkType.SECURE_WIFI,
            deviceSecurity = DeviceSecurityStatus.SECURE,
            failedLoginAttempts = 0,
            behaviour = BehaviourStatus.NORMAL
        )
        val result = predictor.predict(input)

        // Base 100 - 5 (Secure Wi-Fi) = 95
        assertEquals(95, result.trustScore)
        assertEquals(RiskLevel.LOW, result.riskLevel)
        assertEquals(SessionStatus.ACTIVE, result.sessionStatus)
        assertEquals(SecurityAction.ALLOW_ACCESS, result.securityAction)
    }

    @Test
    fun testScenario1_SecureWifi_Normal() {
        // SCENARIO 1: Secure Wi-Fi, Secure Device, 0 Failed, Normal Behaviour
        val input = TrustInput(
            networkType = NetworkType.SECURE_WIFI,
            deviceSecurity = DeviceSecurityStatus.SECURE,
            failedLoginAttempts = 0,
            behaviour = BehaviourStatus.NORMAL
        )
        val result = predictor.predict(input)

        assertEquals(95, result.trustScore)
        assertEquals(RiskLevel.LOW, result.riskLevel)
        assertEquals(SessionStatus.ACTIVE, result.sessionStatus)
        assertEquals(SecurityAction.ALLOW_ACCESS, result.securityAction)
    }

    @Test
    fun testScenario2_Mobile4G_OneFailedLogin() {
        // SCENARIO 2: Mobile 4G, Secure Device, 1 Failed Attempt, Normal Behaviour
        // 100 - 15 (Mobile 4G) - 0 (Secure) - 5 (1 failed login) - 0 (Normal) = 80
        val input = TrustInput(
            networkType = NetworkType.MOBILE_4G,
            deviceSecurity = DeviceSecurityStatus.SECURE,
            failedLoginAttempts = 1,
            behaviour = BehaviourStatus.NORMAL
        )
        val result = predictor.predict(input)

        assertEquals(80, result.trustScore)
        assertEquals(RiskLevel.LOW, result.riskLevel)
        assertEquals(SessionStatus.ACTIVE, result.sessionStatus)
        assertEquals(SecurityAction.ALLOW_ACCESS, result.securityAction)
    }

    @Test
    fun testScenario3_PublicWifi_Compromised_Suspicious() {
        // SCENARIO 3: Public Wi-Fi, Compromised Device, 3 Failed Attempts, Suspicious Behaviour
        // 100 - 35 (Public Wi-Fi) - 25 (Compromised) - 15 (3 * 5) - 20 (Suspicious) = 5
        val input = TrustInput(
            networkType = NetworkType.PUBLIC_WIFI,
            deviceSecurity = DeviceSecurityStatus.COMPROMISED,
            failedLoginAttempts = 3,
            behaviour = BehaviourStatus.SUSPICIOUS
        )
        val result = predictor.predict(input)

        assertEquals(5, result.trustScore)
        assertEquals(RiskLevel.CRITICAL, result.riskLevel)
        assertEquals(SessionStatus.TERMINATED, result.sessionStatus)
        assertEquals(SecurityAction.TERMINATE_SESSION, result.securityAction)
    }

    @Test
    fun testScenario4_PublicWifi_Secure_3Failed_Suspicious() {
        // SCENARIO 4: Public Wi-Fi, Secure Device, 3 Failed Attempts, Suspicious Behaviour
        // 100 - 35 (Public Wi-Fi) - 0 (Secure) - 15 (3 * 5) - 20 (Suspicious) = 30
        val input = TrustInput(
            networkType = NetworkType.PUBLIC_WIFI,
            deviceSecurity = DeviceSecurityStatus.SECURE,
            failedLoginAttempts = 3,
            behaviour = BehaviourStatus.SUSPICIOUS
        )
        val result = predictor.predict(input)

        assertEquals(30, result.trustScore)
        assertEquals(RiskLevel.HIGH, result.riskLevel)
        assertEquals(SessionStatus.REAUTH_REQUIRED, result.sessionStatus)
        assertEquals(SecurityAction.REQUIRE_REAUTHENTICATION, result.securityAction)
    }

    @Test
    fun testMediumRiskThreshold() {
        // Score between 41 and 70 -> MEDIUM Risk, WARNING Session
        // 100 - 35 (Public Wi-Fi) - 0 - 5 (1 failed) - 0 (Normal) = 60
        val input = TrustInput(
            networkType = NetworkType.PUBLIC_WIFI,
            deviceSecurity = DeviceSecurityStatus.SECURE,
            failedLoginAttempts = 1,
            behaviour = BehaviourStatus.NORMAL
        )
        val result = predictor.predict(input)

        assertEquals(60, result.trustScore)
        assertEquals(RiskLevel.MEDIUM, result.riskLevel)
        assertEquals(SessionStatus.WARNING, result.sessionStatus)
        assertEquals(SecurityAction.SHOW_SECURITY_WARNING, result.securityAction)
    }

    @Test
    fun testScoreClampingToZero() {
        // 100 - 35 - 25 - 25 - 20 = -5 -> Should clamp to 0
        val input = TrustInput(
            networkType = NetworkType.PUBLIC_WIFI,
            deviceSecurity = DeviceSecurityStatus.COMPROMISED,
            failedLoginAttempts = 5,
            behaviour = BehaviourStatus.SUSPICIOUS
        )
        val result = predictor.predict(input)

        assertEquals(0, result.trustScore)
        assertEquals(RiskLevel.CRITICAL, result.riskLevel)
        assertEquals(SessionStatus.TERMINATED, result.sessionStatus)
    }

    @Test
    fun testSecurityPolicyEngineBoundaries() {
        assertEquals(SessionStatus.ACTIVE, SecurityPolicyEngine.evaluate(100).sessionStatus)
        assertEquals(SessionStatus.ACTIVE, SecurityPolicyEngine.evaluate(71).sessionStatus)

        assertEquals(SessionStatus.WARNING, SecurityPolicyEngine.evaluate(70).sessionStatus)
        assertEquals(SessionStatus.WARNING, SecurityPolicyEngine.evaluate(41).sessionStatus)

        assertEquals(SessionStatus.REAUTH_REQUIRED, SecurityPolicyEngine.evaluate(40).sessionStatus)
        assertEquals(SessionStatus.REAUTH_REQUIRED, SecurityPolicyEngine.evaluate(21).sessionStatus)

        assertEquals(SessionStatus.TERMINATED, SecurityPolicyEngine.evaluate(20).sessionStatus)
        assertEquals(SessionStatus.TERMINATED, SecurityPolicyEngine.evaluate(0).sessionStatus)
    }

    @Test
    fun testAuditLoggerOrderingAndEvents() {
        auditLogger.logNetworkChange("Secure Wi-Fi", "Public Wi-Fi")
        auditLogger.logTrustScoreChange(95, 38)
        auditLogger.logRiskLevelChange("HIGH")
        auditLogger.logSecurityPolicyTriggered("REQUIRE RE-AUTHENTICATION")

        val currentLogs = auditLogger.logs.value
        // Initial startup + 4 events = 5 events
        assertEquals(5, currentLogs.size)

        // Newest log should be at the top
        assertEquals("Security Policy Triggered: REQUIRE RE-AUTHENTICATION", currentLogs[0].message)
        assertEquals(AuditLogType.SECURITY_POLICY, currentLogs[0].type)

        assertEquals("Risk Level changed to HIGH", currentLogs[1].message)
        assertEquals("Trust Score updated: 95 → 38", currentLogs[2].message)
        assertEquals("Network changed: Secure Wi-Fi → Public Wi-Fi", currentLogs[3].message)
        assertTrue(currentLogs[4].message.contains("initialized"))
    }

    @Test
    fun testViewModelStateMutations() {
        val testScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined)
        val viewModel = com.example.mobiletrust.ui.viewmodel.MobileTrustViewModel(predictor, auditLogger, testScope)
        
        // Initial state
        assertEquals(95, viewModel.uiState.value.result.trustScore)
        assertEquals(RiskLevel.LOW, viewModel.uiState.value.result.riskLevel)
        assertEquals(SessionStatus.ACTIVE, viewModel.uiState.value.result.sessionStatus)

        // Switch to Public Wi-Fi
        viewModel.onNetworkSelected(NetworkType.PUBLIC_WIFI)
        assertEquals(NetworkType.PUBLIC_WIFI, viewModel.uiState.value.input.networkType)
        // 100 - 35 = 65 -> MEDIUM risk, WARNING session
        assertEquals(65, viewModel.uiState.value.result.trustScore)
        assertEquals(RiskLevel.MEDIUM, viewModel.uiState.value.result.riskLevel)
        assertEquals(SessionStatus.WARNING, viewModel.uiState.value.result.sessionStatus)
        assertEquals("Security Warning", viewModel.uiState.value.activeAlert?.title)

        // Dismiss alert
        viewModel.dismissAlert()
        org.junit.Assert.assertNull(viewModel.uiState.value.activeAlert)

        // Set device compromised
        viewModel.onDeviceSecurityChanged(DeviceSecurityStatus.COMPROMISED)
        // 100 - 35 - 25 = 40 -> HIGH risk, REAUTH_REQUIRED
        assertEquals(40, viewModel.uiState.value.result.trustScore)
        assertEquals(RiskLevel.HIGH, viewModel.uiState.value.result.riskLevel)
        assertEquals(SessionStatus.REAUTH_REQUIRED, viewModel.uiState.value.result.sessionStatus)

        // Reset
        viewModel.resetState()
        assertEquals(95, viewModel.uiState.value.result.trustScore)
        assertEquals(NetworkType.SECURE_WIFI, viewModel.uiState.value.input.networkType)
        assertEquals(DeviceSecurityStatus.SECURE, viewModel.uiState.value.input.deviceSecurity)
        assertEquals(0, viewModel.uiState.value.input.failedLoginAttempts)
        assertEquals(BehaviourStatus.NORMAL, viewModel.uiState.value.input.behaviour)
    }
}
