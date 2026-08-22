package com.example.mobiletrust.security

import com.example.mobiletrust.data.model.AlertChannel
import com.example.mobiletrust.data.model.SecurityAction
import com.example.mobiletrust.data.model.TrustAlert
import com.example.mobiletrust.data.model.TrustPolicyConfig
import com.example.mobiletrust.data.model.TrustResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AlertDispatcher(
    private val auditLogger: AuditLogger,
    private val maxAlerts: Int = 50
) {

    private val _adminAlerts = MutableStateFlow<List<TrustAlert>>(emptyList())
    val adminAlerts: StateFlow<List<TrustAlert>> = _adminAlerts.asStateFlow()

    private val _userAlert = MutableStateFlow<TrustAlert?>(null)
    val userAlert: StateFlow<TrustAlert?> = _userAlert.asStateFlow()

    fun dispatch(previous: TrustResult?, current: TrustResult, config: TrustPolicyConfig) {
        val escalated = previous == null ||
            current.securityAction.severity > previous.securityAction.severity

        if (escalated && current.securityAction.severity > 0) {
            val alert = buildAlert(current, AlertChannel.USER)
            _userAlert.value = alert
            auditLogger.logAlert(AlertChannel.USER.displayName, alert.title)
        }

        val crossedThreshold = current.trustScore < config.alertThreshold &&
            (previous == null || previous.trustScore >= config.alertThreshold)

        if (escalated || crossedThreshold) {
            val alert = buildAlert(current, AlertChannel.ADMIN)
            _adminAlerts.update { existing -> (listOf(alert) + existing).take(maxAlerts) }
            auditLogger.logAlert(AlertChannel.ADMIN.displayName, alert.title)
        }
    }

    fun dismissUserAlert() {
        _userAlert.value = null
    }

    fun acknowledgeAdminAlerts() {
        _adminAlerts.update { existing -> existing.map { it.copy(acknowledged = true) } }
    }

    private fun buildAlert(result: TrustResult, channel: AlertChannel): TrustAlert {
        val title = when (channel) {
            AlertChannel.USER -> userTitle(result)
            AlertChannel.ADMIN -> "Trust degradation on operator device"
        }
        val message = when (channel) {
            AlertChannel.USER -> result.securityAction.description
            AlertChannel.ADMIN -> buildString {
                append("Score ${result.trustScore}/100 on ${result.input.networkType.displayName}")
                append(" for role ${result.input.userRole.displayName}. ")
                append("Enforced ${result.securityAction.displayName}.")
                if (result.matchedRules.isNotEmpty()) {
                    append(" Rules: ${result.matchedRules.joinToString()}.")
                }
            }
        }

        return TrustAlert(
            timestamp = auditLogger.currentTimestamp(),
            channel = channel,
            title = title,
            message = message,
            riskLevel = result.riskLevel,
            action = result.securityAction,
            trustScore = result.trustScore
        )
    }

    private fun userTitle(result: TrustResult): String = when (result.securityAction) {
        SecurityAction.SHOW_SECURITY_WARNING -> "Security Warning"
        SecurityAction.REQUIRE_REAUTHENTICATION -> "Re-authentication Required"
        SecurityAction.TERMINATE_SESSION -> "Session Terminated"
        else -> "Trust Status Update"
    }
}
