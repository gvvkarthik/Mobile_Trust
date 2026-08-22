package com.example.mobiletrust.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiletrust.data.model.AuditLogEntry
import com.example.mobiletrust.data.model.AuditLogType
import com.example.mobiletrust.data.model.BehaviourStatus
import com.example.mobiletrust.data.model.DeviceSecurityStatus
import com.example.mobiletrust.data.model.FederatedReport
import com.example.mobiletrust.data.model.ModelMetrics
import com.example.mobiletrust.data.model.NetworkType
import com.example.mobiletrust.data.model.SessionStatus
import com.example.mobiletrust.data.model.TrustAlert
import com.example.mobiletrust.data.model.TrustInput
import com.example.mobiletrust.data.model.TrustPolicyConfig
import com.example.mobiletrust.data.model.TrustResult
import com.example.mobiletrust.data.model.UserRole
import com.example.mobiletrust.domain.engine.TrustEngine
import com.example.mobiletrust.domain.ml.FederatedTrainer
import com.example.mobiletrust.domain.ml.ModelEvaluator
import com.example.mobiletrust.domain.ml.SyntheticDataset
import com.example.mobiletrust.domain.predictor.HybridTrustPredictor
import com.example.mobiletrust.security.AlertDispatcher
import com.example.mobiletrust.security.AuditLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MobileTrustUiState(
    val input: TrustInput,
    val result: TrustResult,
    val config: TrustPolicyConfig,
    val logs: List<AuditLogEntry> = emptyList(),
    val adminAlerts: List<TrustAlert> = emptyList(),
    val activeAlert: TrustAlert? = null,
    val modelMetrics: ModelMetrics = ModelMetrics.EMPTY,
    val federatedReport: FederatedReport? = null,
    val isFederatedRunning: Boolean = false,
    val isDemoRunning: Boolean = false,
    val demoCurrentStep: Int = 0
)

class MobileTrustViewModel(
    private val engine: TrustEngine = TrustEngine(),
    private val auditLogger: AuditLogger = AuditLogger(),
    private val alertDispatcher: AlertDispatcher = AlertDispatcher(auditLogger),
    private val externalScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope get() = externalScope ?: viewModelScope

    private val _uiState: MutableStateFlow<MobileTrustUiState>
    val uiState: StateFlow<MobileTrustUiState>

    private var demoJob: Job? = null

    init {
        val config = TrustPolicyConfig()
        val input = TrustInput()
        _uiState = MutableStateFlow(
            MobileTrustUiState(
                input = input,
                result = engine.evaluate(input, config),
                config = config,
                logs = auditLogger.logs.value
            )
        )
        uiState = _uiState.asStateFlow()

        scope.launch {
            auditLogger.logs.collect { logs -> _uiState.update { it.copy(logs = logs) } }
        }
        scope.launch {
            alertDispatcher.adminAlerts.collect { alerts ->
                _uiState.update { it.copy(adminAlerts = alerts) }
            }
        }
        scope.launch {
            alertDispatcher.userAlert.collect { alert ->
                _uiState.update { it.copy(activeAlert = alert) }
            }
        }

        evaluateModelMetrics()
    }

    fun onNetworkSelected(network: NetworkType) {
        val current = _uiState.value.input
        if (current.networkType == network) return
        applyInput(
            current.copy(
                networkType = network,
                networkTransitions = current.networkTransitions + 1
            )
        )
    }

    fun onDeviceSecurityChanged(status: DeviceSecurityStatus) {
        val current = _uiState.value.input
        if (current.deviceSecurity == status) return
        applyInput(current.copy(deviceSecurity = status))
    }

    fun onFailedLoginAttemptsChanged(attempts: Int) {
        val current = _uiState.value.input
        if (current.failedLoginAttempts == attempts) return
        applyInput(current.copy(failedLoginAttempts = attempts))
    }

    fun onBehaviourChanged(behaviour: BehaviourStatus) {
        val current = _uiState.value.input
        if (current.behaviour == behaviour) return
        applyInput(current.copy(behaviour = behaviour))
    }

    fun onUserRoleChanged(role: UserRole) {
        val current = _uiState.value.input
        if (current.userRole == role) return
        auditLogger.logPolicyConfigChange("Active role changed to " + role.displayName)
        applyInput(current.copy(userRole = role))
    }

    fun onRuleToggled(ruleId: String, enabled: Boolean) {
        val config = _uiState.value.config
        val rule = config.rules.firstOrNull { it.id == ruleId } ?: return
        val state = if (enabled) "enabled" else "disabled"
        auditLogger.logPolicyConfigChange("Policy rule " + rule.name + " " + state)
        applyConfig(config.withRuleEnabled(ruleId, enabled))
    }

    fun onMlWeightChanged(weight: Double) {
        val config = _uiState.value.config
        if (config.mlWeight == weight) return
        applyConfig(config.withMlWeight(weight))
    }

    fun onMlWeightCommitted() {
        val weight = (_uiState.value.config.mlWeight * 100).toInt()
        auditLogger.logPolicyConfigChange(
            "Scoring blend set to $weight% ML / ${100 - weight}% rules"
        )
    }

    fun dismissAlert() {
        alertDispatcher.dismissUserAlert()
    }

    fun acknowledgeAdminAlerts() {
        alertDispatcher.acknowledgeAdminAlerts()
        auditLogger.log("Admin acknowledged all open alerts", AuditLogType.ALERT)
    }

    fun reauthenticateUser() {
        auditLogger.log(
            "Re-authentication challenge passed for role " + _uiState.value.input.userRole.displayName,
            AuditLogType.SECURITY_POLICY
        )
        dismissAlert()
        applyInput(_uiState.value.input.copy(failedLoginAttempts = 0))
    }

    fun recoverTerminatedSession() {
        val role = _uiState.value.input.userRole.displayName
        resetState()
        auditLogger.log(
            "Session recovery: identity re-verified for role $role, " +
                "trust context re-established from baseline",
            AuditLogType.SECURITY_POLICY
        )
    }

    fun runFederatedRound() {
        if (_uiState.value.isFederatedRunning) return
        _uiState.update { it.copy(isFederatedRunning = true) }

        scope.launch {
            auditLogger.logFederated("Federated training started across simulated field clients")
            val outcome = withContext(Dispatchers.Default) { FederatedTrainer().run() }
            val report = outcome.report
            val accuracy = (report.finalAccuracy * 100).toInt()
            auditLogger.logFederated(
                "Federated aggregation complete: ${report.rounds.size} rounds, " +
                    "global accuracy $accuracy%, no raw samples exchanged"
            )

            val predictor = engine.predictor
            if (predictor is HybridTrustPredictor) {
                predictor.updateModel(outcome.globalModel)
                auditLogger.logFederated("Aggregated model promoted to the live trust engine")
            }

            _uiState.update { it.copy(federatedReport = report, isFederatedRunning = false) }
            evaluateModelMetrics()
            applyInput(_uiState.value.input)
        }
    }

    fun runDemoScenario() {
        if (_uiState.value.isDemoRunning) return

        demoJob?.cancel()
        demoJob = scope.launch {
            _uiState.update { it.copy(isDemoRunning = true, demoCurrentStep = 1) }

            auditLogger.logDemoEvent("Step 1", "Baseline on Secure Wi-Fi with a secure device")
            applyInput(
                TrustInput(
                    networkType = NetworkType.SECURE_WIFI,
                    deviceSecurity = DeviceSecurityStatus.SECURE,
                    failedLoginAttempts = 0,
                    behaviour = BehaviourStatus.NORMAL,
                    userRole = _uiState.value.input.userRole,
                    networkTransitions = 0
                )
            )
            delay(DEMO_STEP_DELAY_MS)

            _uiState.update { it.copy(demoCurrentStep = 2) }
            auditLogger.logDemoEvent("Step 2", "Transition to Mobile 4G")
            onNetworkSelected(NetworkType.MOBILE_4G)
            delay(DEMO_STEP_DELAY_MS)

            _uiState.update { it.copy(demoCurrentStep = 3) }
            auditLogger.logDemoEvent(
                "Step 3",
                "Transition to Public Wi-Fi with 3 failed logins and suspicious behaviour"
            )
            applyInput(
                _uiState.value.input.copy(
                    networkType = NetworkType.PUBLIC_WIFI,
                    failedLoginAttempts = 3,
                    behaviour = BehaviourStatus.SUSPICIOUS,
                    networkTransitions = _uiState.value.input.networkTransitions + 1
                )
            )
            delay(DEMO_STEP_DELAY_MS)

            _uiState.update { it.copy(isDemoRunning = false, demoCurrentStep = 0) }
        }
    }

    fun resetState() {
        demoJob?.cancel()
        demoJob = null
        alertDispatcher.dismissUserAlert()

        val config = TrustPolicyConfig()
        val input = TrustInput()
        _uiState.update {
            it.copy(
                input = input,
                result = engine.evaluate(input, config),
                config = config,
                activeAlert = null,
                isDemoRunning = false,
                demoCurrentStep = 0
            )
        }
        auditLogger.log(
            "Trust context reset to baseline; audit history retained",
            AuditLogType.SYSTEM
        )
    }

    private fun applyInput(input: TrustInput) {
        if (isSessionLocked()) {
            auditLogger.log(
                "Blocked trust context change: session is terminated and requires re-authentication",
                AuditLogType.SECURITY_POLICY
            )
            return
        }
        evaluate(input, _uiState.value.config)
    }

    private fun applyConfig(config: TrustPolicyConfig) {
        if (isSessionLocked()) {
            auditLogger.log(
                "Blocked policy change: session is terminated and requires re-authentication",
                AuditLogType.SECURITY_POLICY
            )
            return
        }
        evaluate(_uiState.value.input, config)
    }

    private fun isSessionLocked(): Boolean =
        _uiState.value.result.sessionStatus == SessionStatus.TERMINATED

    private fun evaluate(input: TrustInput, config: TrustPolicyConfig) {
        val previousState = _uiState.value
        val previousResult = previousState.result
        val result = engine.evaluate(input, config)

        if (previousState.input.networkType != input.networkType) {
            auditLogger.logNetworkChange(
                oldNetwork = previousState.input.networkType.displayName,
                newNetwork = input.networkType.displayName,
                transitionCount = input.networkTransitions
            )
        }
        if (previousResult.trustScore != result.trustScore) {
            auditLogger.logTrustScoreChange(
                oldScore = previousResult.trustScore,
                newScore = result.trustScore,
                ruleScore = result.ruleScore,
                mlScore = result.mlScore
            )
            auditLogger.logInference(result.degradationProbability, result.inferenceMillis)
        }
        if (previousResult.riskLevel != result.riskLevel) {
            auditLogger.logRiskLevelChange(result.riskLevel.displayName)
        }
        if (previousResult.securityAction != result.securityAction ||
            previousResult.matchedRules != result.matchedRules
        ) {
            auditLogger.logSecurityPolicyTriggered(
                result.securityAction.displayName,
                result.matchedRules
            )
        }

        _uiState.update { it.copy(input = input, result = result, config = config) }
        alertDispatcher.dispatch(previousResult, result, config)
    }

    private fun evaluateModelMetrics() {
        val predictor = engine.predictor
        if (predictor !is HybridTrustPredictor) return

        scope.launch {
            val model = predictor.model
            val metrics = withContext(Dispatchers.Default) {
                ModelEvaluator.evaluate(model, SyntheticDataset.testSet())
            }
            val accuracy = (metrics.accuracy * 100).toInt()
            auditLogger.log(
                "Model validated on ${metrics.sampleCount} synthetic samples: $accuracy% accuracy",
                AuditLogType.ML_INFERENCE
            )
            _uiState.update { it.copy(modelMetrics = metrics) }
        }
    }

    private companion object {
        const val DEMO_STEP_DELAY_MS = 2000L
    }
}
