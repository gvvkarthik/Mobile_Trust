package com.example.mobiletrust.data.model

data class TrustResult(
    val trustScore: Int,
    val riskLevel: RiskLevel,
    val sessionStatus: SessionStatus,
    val securityAction: SecurityAction,
    val input: TrustInput,
    val baseScore: Int = 100,
    val networkPenalty: Int = input.networkType.penalty,
    val deviceSecurityPenalty: Int = input.deviceSecurity.penalty,
    val failedLoginsPenalty: Int = input.failedLoginAttempts * 5,
    val behaviourPenalty: Int = input.behaviour.penalty
) {
    val totalPenalty: Int
        get() = networkPenalty + deviceSecurityPenalty + failedLoginsPenalty + behaviourPenalty
}
