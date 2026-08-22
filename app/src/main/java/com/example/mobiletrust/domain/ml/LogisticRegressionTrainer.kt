package com.example.mobiletrust.domain.ml

object LogisticRegressionTrainer {

    fun train(
        samples: List<LabelledSample>,
        learningRate: Double = PretrainedTrustModel.LEARNING_RATE,
        epochs: Int = PretrainedTrustModel.TRAINING_EPOCHS,
        initial: LogisticRegressionModel = LogisticRegressionModel.zeros()
    ): LogisticRegressionModel {
        require(samples.isNotEmpty()) { "Cannot train on an empty sample set" }

        val weights = initial.weights.copyOf()
        var bias = initial.bias
        val size = samples.size

        repeat(epochs) {
            val weightGradients = DoubleArray(TrustFeatures.SIZE)
            var biasGradient = 0.0

            for (sample in samples) {
                var z = bias
                for (i in weights.indices) {
                    z += weights[i] * sample.features[i]
                }
                val error = LogisticRegressionModel.sigmoid(z) - sample.label
                for (i in weights.indices) {
                    weightGradients[i] += error * sample.features[i]
                }
                biasGradient += error
            }

            for (i in weights.indices) {
                weights[i] -= learningRate * weightGradients[i] / size
            }
            bias -= learningRate * biasGradient / size
        }

        return LogisticRegressionModel(weights, bias)
    }
}
