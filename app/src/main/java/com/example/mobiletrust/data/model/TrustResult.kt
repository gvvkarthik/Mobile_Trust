package com.example.mobiletrust.data.model

data class PenaltyBreakdown(
    val network: Int,
    val device: Int,
    val failedLogins: Int,
    val behaviour: Int,
    val transitions: Int
) {
    val total: Int get() = network + device + failedLogins + behaviour + transitions
}

data class TrustResult(
    val input: TrustInput,
    val trustScore: Int,
    val ruleScore: Int,
    val mlScore: Int,
    val degradationProbability: Double,
    val riskLevel: RiskLevel,
    val sessionStatus: SessionStatus,
    val securityAction: SecurityAction,
    val matchedRules: List<String>,
    val penalties: PenaltyBreakdown,
    val inferenceNanos: Long
) {
    val inferenceMillis: Double get() = inferenceNanos / 1_000_000.0
}
