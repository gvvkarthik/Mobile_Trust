package com.example.mobiletrust.data.model

data class ModelMetrics(
    val accuracy: Double,
    val precision: Double,
    val recall: Double,
    val f1Score: Double,
    val sampleCount: Int,
    val truePositives: Int,
    val trueNegatives: Int,
    val falsePositives: Int,
    val falseNegatives: Int
) {
    companion object {
        val EMPTY = ModelMetrics(0.0, 0.0, 0.0, 0.0, 0, 0, 0, 0, 0)
    }
}

data class FederatedRound(
    val round: Int,
    val globalAccuracy: Double,
    val clientAccuracies: List<Double>
)

data class FederatedReport(
    val clientCount: Int,
    val samplesPerClient: Int,
    val rounds: List<FederatedRound>,
    val durationMillis: Long
) {
    val finalAccuracy: Double get() = rounds.lastOrNull()?.globalAccuracy ?: 0.0
}
