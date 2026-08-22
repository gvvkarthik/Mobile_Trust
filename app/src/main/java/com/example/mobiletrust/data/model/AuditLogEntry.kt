package com.example.mobiletrust.data.model

import java.util.UUID

enum class AuditLogType {
    SYSTEM,
    NETWORK_CHANGE,
    TRUST_UPDATE,
    RISK_CHANGE,
    SECURITY_POLICY,
    POLICY_CONFIG,
    ML_INFERENCE,
    ALERT,
    FEDERATED,
    DEMO_EVENT
}

data class AuditLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: String,
    val message: String,
    val type: AuditLogType = AuditLogType.SYSTEM,
    val epochMillis: Long = System.currentTimeMillis()
)
