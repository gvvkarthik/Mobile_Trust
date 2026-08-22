package com.example.mobiletrust.security

import com.example.mobiletrust.data.model.AuditLogEntry
import com.example.mobiletrust.data.model.AuditLogType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuditLogger(private val maxEntries: Int = 200) {

    private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val _logs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val logs: StateFlow<List<AuditLogEntry>> = _logs.asStateFlow()

    init {
        resetWithStartup()
    }

    fun currentTimestamp(): String = synchronized(timeFormatter) {
        timeFormatter.format(Date())
    }

    fun log(message: String, type: AuditLogType = AuditLogType.SYSTEM) {
        val entry = AuditLogEntry(
            timestamp = currentTimestamp(),
            message = message,
            type = type
        )
        _logs.update { existing -> retain(listOf(entry) + existing) }
    }

    fun logNetworkChange(oldNetwork: String, newNetwork: String, transitionCount: Int) {
        log(
            "Network transition #$transitionCount: $oldNetwork -> $newNetwork",
            AuditLogType.NETWORK_CHANGE
        )
    }

    fun logTrustScoreChange(oldScore: Int, newScore: Int, ruleScore: Int, mlScore: Int) {
        log(
            "Trust Score updated: $oldScore -> $newScore (rule $ruleScore / ml $mlScore)",
            AuditLogType.TRUST_UPDATE
        )
    }

    fun logRiskLevelChange(newRisk: String) {
        log("Risk Level changed to $newRisk", AuditLogType.RISK_CHANGE)
    }

    fun logSecurityPolicyTriggered(action: String, matchedRules: List<String>) {
        val source = if (matchedRules.isEmpty()) "trust thresholds" else matchedRules.joinToString()
        log("Security Policy Triggered: $action [source: $source]", AuditLogType.SECURITY_POLICY)
    }

    fun logInference(probability: Double, millis: Double) {
        val percent = (probability * 100).toInt()
        val latency = String.format(Locale.US, "%.3f", millis)
        log(
            "ML inference: degradation probability $percent% in $latency ms",
            AuditLogType.ML_INFERENCE
        )
    }

    fun logPolicyConfigChange(message: String) {
        log(message, AuditLogType.POLICY_CONFIG)
    }

    fun logAlert(channel: String, title: String) {
        log("Alert dispatched to $channel channel: $title", AuditLogType.ALERT)
    }

    fun logFederated(message: String) {
        log(message, AuditLogType.FEDERATED)
    }

    fun logDemoEvent(stepName: String, detail: String) {
        log("Demo Simulation [$stepName]: $detail", AuditLogType.DEMO_EVENT)
    }

    fun resetWithStartup() {
        _logs.value = listOf(
            AuditLogEntry(
                timestamp = currentTimestamp(),
                message = "MobileTrust engine initialized [hybrid rule + ML scoring active]",
                type = AuditLogType.SYSTEM
            )
        )
    }

    private fun retain(entries: List<AuditLogEntry>): List<AuditLogEntry> {
        if (entries.size <= maxEntries) return entries

        val dropped = entries.size - maxEntries + 1
        val notice = AuditLogEntry(
            timestamp = currentTimestamp(),
            message = "Audit buffer full: $dropped oldest entries discarded (in-memory limit $maxEntries)",
            type = AuditLogType.SYSTEM
        )
        return entries.take(maxEntries - 1) + notice
    }
}
