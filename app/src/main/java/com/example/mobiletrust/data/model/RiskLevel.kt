package com.example.mobiletrust.data.model

enum class RiskLevel(val displayName: String, val severity: Int) {
    LOW("LOW", 0),
    MEDIUM("MEDIUM", 1),
    HIGH("HIGH", 2),
    CRITICAL("CRITICAL", 3);

    companion object {
        fun fromScore(score: Int, thresholds: TrustThresholds = TrustThresholds()): RiskLevel = when {
            score >= thresholds.lowMin -> LOW
            score >= thresholds.mediumMin -> MEDIUM
            score >= thresholds.highMin -> HIGH
            else -> CRITICAL
        }
    }
}
