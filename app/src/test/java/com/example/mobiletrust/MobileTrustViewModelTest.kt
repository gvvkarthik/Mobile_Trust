package com.example.mobiletrust

import com.example.mobiletrust.data.model.AlertChannel
import com.example.mobiletrust.data.model.AuditLogType
import com.example.mobiletrust.data.model.BehaviourStatus
import com.example.mobiletrust.data.model.DeviceSecurityStatus
import com.example.mobiletrust.data.model.NetworkType
import com.example.mobiletrust.data.model.PolicyRule
import com.example.mobiletrust.data.model.RiskLevel
import com.example.mobiletrust.data.model.SecurityAction
import com.example.mobiletrust.data.model.SessionStatus
import com.example.mobiletrust.data.model.UserRole
import com.example.mobiletrust.domain.engine.TrustEngine
import com.example.mobiletrust.security.AlertDispatcher
import com.example.mobiletrust.security.AuditLogger
import com.example.mobiletrust.ui.viewmodel.MobileTrustViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MobileTrustViewModelTest {

    private lateinit var auditLogger: AuditLogger
    private lateinit var viewModel: MobileTrustViewModel

    @Before
    fun setUp() {
        auditLogger = AuditLogger()
        viewModel = MobileTrustViewModel(
            engine = TrustEngine(),
            auditLogger = auditLogger,
            alertDispatcher = AlertDispatcher(auditLogger),
            externalScope = CoroutineScope(Dispatchers.Unconfined)
        )
    }

    @Test
    fun startsFromATrustedBaseline() {
        val state = viewModel.uiState.value
        assertEquals(RiskLevel.LOW, state.result.riskLevel)
        assertEquals(SessionStatus.ACTIVE, state.result.sessionStatus)
        assertEquals(SecurityAction.ALLOW_ACCESS, state.result.securityAction)
        assertNull(state.activeAlert)
        assertTrue(state.adminAlerts.isEmpty())
    }

    @Test
    fun networkTransitionDegradesTrustAndIsCounted() {
        val baseline = viewModel.uiState.value.result.trustScore

        viewModel.onNetworkSelected(NetworkType.PUBLIC_WIFI)
        val state = viewModel.uiState.value

        assertEquals(NetworkType.PUBLIC_WIFI, state.input.networkType)
        assertEquals(1, state.input.networkTransitions)
        assertTrue(state.result.trustScore < baseline)
        assertTrue(
            state.logs.any { it.type == AuditLogType.NETWORK_CHANGE }
        )
        assertTrue(
            state.logs.any { it.type == AuditLogType.ML_INFERENCE }
        )
    }

    @Test
    fun trustDegradationRaisesUserAndAdminAlerts() {
        viewModel.onNetworkSelected(NetworkType.PUBLIC_WIFI)
        viewModel.onDeviceSecurityChanged(DeviceSecurityStatus.COMPROMISED)

        val state = viewModel.uiState.value
        assertNotNull(state.activeAlert)
        assertEquals(AlertChannel.USER, state.activeAlert?.channel)
        assertTrue(state.adminAlerts.isNotEmpty())
        assertTrue(state.adminAlerts.all { it.channel == AlertChannel.ADMIN })
    }

    @Test
    fun alertIsNotReRaisedWhileSeverityIsUnchanged() {
        viewModel.onNetworkSelected(NetworkType.PUBLIC_WIFI)
        assertNotNull(viewModel.uiState.value.activeAlert)

        viewModel.dismissAlert()
        assertNull(viewModel.uiState.value.activeAlert)

        viewModel.onFailedLoginAttemptsChanged(1)
        assertNull(viewModel.uiState.value.activeAlert)
    }

    @Test
    fun acknowledgingAdminAlertsMarksThemHandled() {
        viewModel.onNetworkSelected(NetworkType.PUBLIC_WIFI)
        viewModel.onBehaviourChanged(BehaviourStatus.SUSPICIOUS)
        assertTrue(viewModel.uiState.value.adminAlerts.isNotEmpty())

        viewModel.acknowledgeAdminAlerts()
        assertTrue(viewModel.uiState.value.adminAlerts.all { it.acknowledged })
    }

    @Test
    fun roleChangeTriggersConfigurableRoleGate() {
        viewModel.onNetworkSelected(NetworkType.PUBLIC_WIFI)
        viewModel.onUserRoleChanged(UserRole.GUEST)

        val state = viewModel.uiState.value
        assertEquals(SecurityAction.TERMINATE_SESSION, state.result.securityAction)
        assertTrue(state.result.matchedRules.contains("Public Network Role Gate"))

        viewModel.onRuleToggled("public-network-role-gate", false)
        assertTrue(
            "A terminated session must not be able to disable the rule that terminated it",
            viewModel.uiState.value.config.rules.first { it.id == "public-network-role-gate" }.enabled
        )

        viewModel.recoverTerminatedSession()
        viewModel.onRuleToggled("public-network-role-gate", false)
        viewModel.onNetworkSelected(NetworkType.PUBLIC_WIFI)
        viewModel.onUserRoleChanged(UserRole.GUEST)

        val relaxed = viewModel.uiState.value
        assertTrue(relaxed.result.matchedRules.isEmpty())
        assertTrue(relaxed.result.securityAction.severity < SecurityAction.TERMINATE_SESSION.severity)
    }

    @Test
    fun changingBlendWeightRescoresImmediately() {
        viewModel.onNetworkSelected(NetworkType.MOBILE_4G)

        viewModel.onMlWeightChanged(0.0)
        val ruleOnly = viewModel.uiState.value.result
        assertEquals(ruleOnly.ruleScore, ruleOnly.trustScore)

        viewModel.onMlWeightChanged(1.0)
        val mlOnly = viewModel.uiState.value.result
        assertEquals(mlOnly.mlScore, mlOnly.trustScore)
    }

    @Test
    fun reauthenticationClearsFailedLoginsAndIsAudited() {
        viewModel.onFailedLoginAttemptsChanged(3)
        viewModel.reauthenticateUser()

        val state = viewModel.uiState.value
        assertEquals(0, state.input.failedLoginAttempts)
        assertNull(state.activeAlert)
        assertTrue(
            state.logs.any {
                it.type == AuditLogType.SECURITY_POLICY && it.message.contains("Re-authentication")
            }
        )
    }

    @Test
    fun resetRestoresBaselineAndClearsAlerts() {
        viewModel.onNetworkSelected(NetworkType.PUBLIC_WIFI)
        viewModel.onDeviceSecurityChanged(DeviceSecurityStatus.COMPROMISED)
        viewModel.onFailedLoginAttemptsChanged(5)
        viewModel.onRuleToggled("brute-force-lockout", false)

        viewModel.resetState()
        val state = viewModel.uiState.value

        assertEquals(NetworkType.SECURE_WIFI, state.input.networkType)
        assertEquals(DeviceSecurityStatus.SECURE, state.input.deviceSecurity)
        assertEquals(0, state.input.failedLoginAttempts)
        assertEquals(BehaviourStatus.NORMAL, state.input.behaviour)
        assertEquals(0, state.input.networkTransitions)
        assertEquals(RiskLevel.LOW, state.result.riskLevel)
        assertNull(state.activeAlert)
        assertTrue(
            "Reset must not erase raised admin alerts",
            state.adminAlerts.isNotEmpty()
        )
        assertTrue(
            "Reset must not erase the audit trail",
            state.logs.any { it.type == AuditLogType.NETWORK_CHANGE }
        )
        assertEquals(PolicyRule.defaults(), state.config.rules)
    }

    @Test
    fun terminatedSessionRejectsAndAuditsContextChanges() {
        viewModel.onFailedLoginAttemptsChanged(5)
        assertEquals(SessionStatus.TERMINATED, viewModel.uiState.value.result.sessionStatus)

        val frozenScore = viewModel.uiState.value.result.trustScore
        viewModel.onNetworkSelected(NetworkType.PUBLIC_WIFI)
        viewModel.onDeviceSecurityChanged(DeviceSecurityStatus.COMPROMISED)
        viewModel.onRuleToggled("brute-force-lockout", false)

        val locked = viewModel.uiState.value
        assertEquals(SessionStatus.TERMINATED, locked.result.sessionStatus)
        assertEquals(frozenScore, locked.result.trustScore)
        assertEquals(NetworkType.SECURE_WIFI, locked.input.networkType)
        assertEquals(DeviceSecurityStatus.SECURE, locked.input.deviceSecurity)
        assertEquals(5, locked.input.failedLoginAttempts)
        assertTrue(locked.config.rules.first { it.id == "brute-force-lockout" }.enabled)
        assertTrue(
            locked.logs.any { it.message.contains("Blocked trust context change") }
        )
        assertTrue(
            locked.logs.any { it.message.contains("Blocked policy change") }
        )
    }

    @Test
    fun sessionRecoveryRequiresExplicitReVerification() {
        viewModel.onFailedLoginAttemptsChanged(5)
        assertEquals(SessionStatus.TERMINATED, viewModel.uiState.value.result.sessionStatus)

        viewModel.recoverTerminatedSession()
        val state = viewModel.uiState.value

        assertEquals(SessionStatus.ACTIVE, state.result.sessionStatus)
        assertEquals(0, state.input.failedLoginAttempts)
        assertTrue(
            state.logs.any {
                it.type == AuditLogType.SECURITY_POLICY && it.message.contains("Session recovery")
            }
        )
    }

    @Test
    fun auditLogRecordsPolicyEnforcementInOrder() {
        viewModel.onNetworkSelected(NetworkType.PUBLIC_WIFI)
        viewModel.onDeviceSecurityChanged(DeviceSecurityStatus.COMPROMISED)

        val logs = viewModel.uiState.value.logs
        assertTrue(logs.size > 1)
        assertTrue(logs.first().epochMillis >= logs.last().epochMillis)
        assertTrue(logs.any { it.type == AuditLogType.SECURITY_POLICY })
        assertTrue(logs.any { it.type == AuditLogType.ALERT })
        assertTrue(logs.any { it.type == AuditLogType.RISK_CHANGE })
    }
}
