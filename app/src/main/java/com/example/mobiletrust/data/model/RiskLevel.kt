package com.example.mobiletrust.data.model

enum class RiskLevel(val displayName: String, val minScore: Int, val maxScore: Int) {
    LOW("LOW", 71, 100),
    MEDIUM("MEDIUM", 41, 70),
    HIGH("HIGH", 21, 40),
    CRITICAL("CRITICAL", 0, 20);

    companion object {
        fun fromScore(score: Int): RiskLevel {
            return when {
                score in 71..100 -> LOW
                score in 41..70 -> MEDIUM
                score in 21..40 -> HIGH
                else -> CRITICAL
            }
        }
    }
}
