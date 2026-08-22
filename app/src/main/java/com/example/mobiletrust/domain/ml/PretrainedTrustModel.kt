package com.example.mobiletrust.domain.ml

object PretrainedTrustModel {

    const val TRAINING_SAMPLES = 2400
    const val TRAINING_EPOCHS = 900
    const val LEARNING_RATE = 0.85

    private val WEIGHTS = doubleArrayOf(
        7.289788,
        4.676805,
        4.299938,
        3.583500,
        2.378874
    )

    private const val BIAS = -7.859698

    fun model(): LogisticRegressionModel = LogisticRegressionModel(WEIGHTS.copyOf(), BIAS)
}
