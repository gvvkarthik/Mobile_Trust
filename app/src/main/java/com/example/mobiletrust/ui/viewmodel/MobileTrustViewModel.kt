package com.example.mobiletrust.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiletrust.data.model.AuditLogEntry
import com.example.mobiletrust.data.model.BehaviourStatus
import com.example.mobiletrust.data.model.DeviceSecurityStatus
import com.example.mobiletrust.data.model.NetworkType
import com.example.mobiletrust.data.model.RiskLevel
import com.example.mobiletrust.data.model.SecurityAction
import com.example.mobiletrust.data.model.SessionStatus
import com.example.mobiletrust.data.model.TrustInput
import com.example.mobiletrust.data.model.TrustResult
import com.example.mobiletrust.domain.predictor.RuleBasedTrustPredictor
import com.example.mobiletrust.domain.predictor.TrustPredictor
import com.example.mobiletrust.security.AuditLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SecurityAlert(
    val title: String,
    val message: String,
    val riskLevel: RiskLevel,
    val action: SecurityAction
)

data class MobileTrustUiState(
    val input: TrustInput = TrustInput(),
    val result: TrustResult = RuleBasedTrustPredictor().predict(TrustInput()),
    val logs: List<AuditLogEntry> = emptyList(),
    val isDemoRunning: Boolean = false,
    val demoCurrentStep: Int = 0,
    val activeAlert: SecurityAlert? = null
)

class MobileTrustViewModel(
    private val predictor: TrustPredictor = RuleBasedTrustPredictor(),
    private val auditLogger: AuditLogger = AuditLogger(),
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val effectiveScope: CoroutineScope by lazy {
        coroutineScope ?: viewModelScope
    }

    private val _uiState = MutableStateFlow(
        MobileTrustUiState(
            input = TrustInput(),
            result = predictor.predict(TrustInput()),
            logs = auditLogger.logs.value
        )
    )
    val uiState: StateFlow<MobileTrustUiState> = _uiState.asStateFlow()

    private var demoJob: Job? = null

    init {
        // Collect audit logs into the UI state
        if (coroutineScope != null) {
            coroutineScope.launch {
                auditLogger.logs.collect { newLogs ->
                    _uiState.update { it.copy(logs = newLogs) }
                }
            }
        } else {
            viewModelScope.launch {
                auditLogger.logs.collect { newLogs ->
                    _uiState.update { it.copy(logs = newLogs) }
                }
            }
        }
    }

    fun onNetworkSelected(network: NetworkType) {
        if (_uiState.value.input.networkType == network) return
        updateState(_uiState.value.input.copy(networkType = network))
    }

    fun onDeviceSecurityChanged(status: DeviceSecurityStatus) {
        if (_uiState.value.input.deviceSecurity == status) return
        updateState(_uiState.value.input.copy(deviceSecurity = status))
    }

    fun onFailedLoginAttemptsChanged(attempts: Int) {
        if (_uiState.value.input.failedLoginAttempts == attempts) return
        updateState(_uiState.value.input.copy(failedLoginAttempts = attempts))
    }

    fun onBehaviourChanged(behaviour: BehaviourStatus) {
        if (_uiState.value.input.behaviour == behaviour) return
        updateState(_uiState.value.input.copy(behaviour = behaviour))
    }

    private fun updateState(newInput: TrustInput) {
        val previousState = _uiState.value
        val previousResult = previousState.result
        val newResult = predictor.predict(newInput)

        // Log specific state transitions
        if (previousState.input.networkType != newInput.networkType) {
            auditLogger.logNetworkChange(
                oldNet = previousState.input.networkType.displayName,
                newNet = newInput.networkType.displayName
            )
        }

        if (previousResult.trustScore != newResult.trustScore) {
            auditLogger.logTrustScoreChange(
                oldScore = previousResult.trustScore,
                newScore = newResult.trustScore
            )
        }

        if (previousResult.riskLevel != newResult.riskLevel) {
            auditLogger.logRiskLevelChange(newResult.riskLevel.displayName)
        }

        if (previousResult.securityAction != newResult.securityAction) {
            auditLogger.logSecurityPolicyTriggered(newResult.securityAction.displayName)
        }

        // Determine alert popup based on Risk Level
        val alert = when (newResult.riskLevel) {
            RiskLevel.LOW -> null
            RiskLevel.MEDIUM -> SecurityAlert(
                title = "Security Warning",
                message = "Your device trust level has decreased.",
                riskLevel = RiskLevel.MEDIUM,
                action = newResult.securityAction
            )
            RiskLevel.HIGH -> SecurityAlert(
                title = "Re-authentication Required",
                message = "Your current device or network environment is considered risky.",
                riskLevel = RiskLevel.HIGH,
                action = newResult.securityAction
            )
            RiskLevel.CRITICAL -> SecurityAlert(
                title = "Session Terminated",
                message = "Your trust score is critically low. Access has been blocked.",
                riskLevel = RiskLevel.CRITICAL,
                action = newResult.securityAction
            )
        }

        _uiState.update {
            it.copy(
                input = newInput,
                result = newResult,
                activeAlert = alert
            )
        }
    }

    fun dismissAlert() {
        _uiState.update { it.copy(activeAlert = null) }
    }

    fun runDemoScenario() {
        if (_uiState.value.isDemoRunning) return

        demoJob?.cancel()
        demoJob = effectiveScope.launch {
            _uiState.update { it.copy(isDemoRunning = true, demoCurrentStep = 1) }

            // STEP 1: Secure baseline
            auditLogger.logDemoEvent("Step 1", "Initializing baseline: Secure Wi-Fi, Device Secure, 0 Failed, Normal Behaviour")
            updateState(
                TrustInput(
                    networkType = NetworkType.SECURE_WIFI,
                    deviceSecurity = DeviceSecurityStatus.SECURE,
                    failedLoginAttempts = 0,
                    behaviour = BehaviourStatus.NORMAL
                )
            )
            delay(2000)

            // STEP 2: Switch to Mobile 4G
            _uiState.update { it.copy(demoCurrentStep = 2) }
            auditLogger.logDemoEvent("Step 2", "Simulating transition to Mobile 4G")
            updateState(
                _uiState.value.input.copy(
                    networkType = NetworkType.MOBILE_4G
                )
            )
            delay(2000)

            // STEP 3: Switch to Public Wi-Fi + Security Anomaly
            _uiState.update { it.copy(demoCurrentStep = 3) }
            auditLogger.logDemoEvent("Step 3", "Simulating Public Wi-Fi connection with 3 failed logins and suspicious behaviour")
            updateState(
                _uiState.value.input.copy(
                    networkType = NetworkType.PUBLIC_WIFI,
                    failedLoginAttempts = 3,
                    behaviour = BehaviourStatus.SUSPICIOUS
                )
            )

            _uiState.update { it.copy(isDemoRunning = false, demoCurrentStep = 0) }
        }
    }

    fun resetState() {
        demoJob?.cancel()
        demoJob = null
        val initialInput = TrustInput(
            networkType = NetworkType.SECURE_WIFI,
            deviceSecurity = DeviceSecurityStatus.SECURE,
            failedLoginAttempts = 0,
            behaviour = BehaviourStatus.NORMAL
        )
        val initialResult = predictor.predict(initialInput)
        auditLogger.resetWithStartup()
        auditLogger.log("Application state reset to initial baseline", com.example.mobiletrust.data.model.AuditLogType.SYSTEM)

        _uiState.value = MobileTrustUiState(
            input = initialInput,
            result = initialResult,
            logs = auditLogger.logs.value,
            isDemoRunning = false,
            demoCurrentStep = 0,
            activeAlert = null
        )
    }

    fun reauthenticateUser() {
        // Simulates a user successfully completing re-authentication
        auditLogger.log("User successfully verified credentials via biometric / MFA re-authentication", com.example.mobiletrust.data.model.AuditLogType.SECURITY_POLICY)
        dismissAlert()
    }
}
