package com.example.mobiletrust.data.model

import java.util.UUID

enum class AlertChannel(val displayName: String) {
    USER("User"),
    ADMIN("Admin")
}

data class TrustAlert(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: String,
    val channel: AlertChannel,
    val title: String,
    val message: String,
    val riskLevel: RiskLevel,
    val action: SecurityAction,
    val trustScore: Int,
    val acknowledged: Boolean = false
) {
    val isBlocking: Boolean get() = action == SecurityAction.TERMINATE_SESSION

    val requiresReauthentication: Boolean
        get() = action == SecurityAction.REQUIRE_REAUTHENTICATION
}
