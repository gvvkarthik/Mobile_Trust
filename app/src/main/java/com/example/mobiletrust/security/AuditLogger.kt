package com.example.mobiletrust.security

import com.example.mobiletrust.data.model.AuditLogEntry
import com.example.mobiletrust.data.model.AuditLogType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuditLogger {

    private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val _logs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val logs: StateFlow<List<AuditLogEntry>> = _logs.asStateFlow()

    init {
        logStartup()
    }

    private fun getCurrentTimestamp(): String {
        return timeFormatter.format(Date())
    }

    fun log(message: String, type: AuditLogType = AuditLogType.SYSTEM) {
        val entry = AuditLogEntry(
            timestamp = getCurrentTimestamp(),
            message = message,
            type = type
        )
        // Insert at index 0 so newest logs appear at the top
        _logs.value = listOf(entry) + _logs.value
    }

    fun logStartup() {
        val timestamp = getCurrentTimestamp()
        val startupEntry = AuditLogEntry(
            timestamp = timestamp,
            message = "MobileTrust security monitoring engine initialized [Default baseline active]",
            type = AuditLogType.SYSTEM
        )
        _logs.value = listOf(startupEntry)
    }

    fun logNetworkChange(oldNet: String, newNet: String) {
        log("Network changed: $oldNet → $newNet", AuditLogType.NETWORK_CHANGE)
    }

    fun logTrustScoreChange(oldScore: Int, newScore: Int) {
        log("Trust Score updated: $oldScore → $newScore", AuditLogType.TRUST_UPDATE)
    }

    fun logRiskLevelChange(newRisk: String) {
        log("Risk Level changed to $newRisk", AuditLogType.RISK_CHANGE)
    }

    fun logSecurityPolicyTriggered(action: String) {
        log("Security Policy Triggered: $action", AuditLogType.SECURITY_POLICY)
    }

    fun logDemoEvent(stepName: String, detail: String) {
        log("Demo Simulation [$stepName]: $detail", AuditLogType.DEMO_EVENT)
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun resetWithStartup() {
        _logs.value = emptyList()
        logStartup()
    }
}
