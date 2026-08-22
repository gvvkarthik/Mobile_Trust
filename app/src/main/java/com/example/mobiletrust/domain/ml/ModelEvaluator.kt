package com.example.mobiletrust.domain.ml

import com.example.mobiletrust.data.model.ModelMetrics

object ModelEvaluator {

    fun evaluate(
        model: LogisticRegressionModel,
        samples: List<LabelledSample>,
        threshold: Double = 0.5
    ): ModelMetrics {
        if (samples.isEmpty()) return ModelMetrics.EMPTY

        var truePositives = 0
        var trueNegatives = 0
        var falsePositives = 0
        var falseNegatives = 0

        for (sample in samples) {
            val predicted = model.classify(sample.features, threshold)
            when {
                predicted == 1 && sample.label == 1 -> truePositives++
                predicted == 0 && sample.label == 0 -> trueNegatives++
                predicted == 1 && sample.label == 0 -> falsePositives++
                else -> falseNegatives++
            }
        }

        val precision = ratio(truePositives, truePositives + falsePositives)
        val recall = ratio(truePositives, truePositives + falseNegatives)
        val f1 = if (precision + recall > 0.0) {
            2.0 * precision * recall / (precision + recall)
        } else {
            0.0
        }

        return ModelMetrics(
            accuracy = (truePositives + trueNegatives).toDouble() / samples.size,
            precision = precision,
            recall = recall,
            f1Score = f1,
            sampleCount = samples.size,
            truePositives = truePositives,
            trueNegatives = trueNegatives,
            falsePositives = falsePositives,
            falseNegatives = falseNegatives
        )
    }

    private fun ratio(numerator: Int, denominator: Int): Double =
        if (denominator == 0) 0.0 else numerator.toDouble() / denominator
}
