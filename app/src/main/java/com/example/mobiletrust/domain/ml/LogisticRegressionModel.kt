package com.example.mobiletrust.domain.ml

import kotlin.math.exp

class LogisticRegressionModel(
    val weights: DoubleArray,
    val bias: Double
) {

    init {
        require(weights.size == TrustFeatures.SIZE) {
            "Model expects ${TrustFeatures.SIZE} weights but received ${weights.size}"
        }
    }

    fun degradationProbability(features: DoubleArray): Double {
        var z = bias
        for (i in weights.indices) {
            z += weights[i] * features[i]
        }
        return sigmoid(z)
    }

    fun classify(features: DoubleArray, threshold: Double = 0.5): Int =
        if (degradationProbability(features) >= threshold) 1 else 0

    fun copy(): LogisticRegressionModel = LogisticRegressionModel(weights.copyOf(), bias)

    companion object {

        fun zeros(): LogisticRegressionModel =
            LogisticRegressionModel(DoubleArray(TrustFeatures.SIZE), 0.0)

        fun average(models: List<LogisticRegressionModel>): LogisticRegressionModel {
            require(models.isNotEmpty()) { "Cannot average an empty model list" }
            val weights = DoubleArray(TrustFeatures.SIZE)
            for (model in models) {
                for (i in weights.indices) {
                    weights[i] += model.weights[i]
                }
            }
            for (i in weights.indices) {
                weights[i] /= models.size
            }
            return LogisticRegressionModel(weights, models.sumOf { it.bias } / models.size)
        }

        fun sigmoid(z: Double): Double = if (z >= 0.0) {
            1.0 / (1.0 + exp(-z))
        } else {
            val e = exp(z)
            e / (1.0 + e)
        }
    }
}
