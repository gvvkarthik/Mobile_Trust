package com.example.mobiletrust.domain.ml

data class LabelledSample(val features: DoubleArray, val label: Int) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LabelledSample) return false
        return label == other.label && features.contentEquals(other.features)
    }

    override fun hashCode(): Int = 31 * features.contentHashCode() + label
}

object SyntheticDataset {

    const val TRAIN_SEED = 20260822L
    const val TEST_SEED = 987654321L
    const val DEGRADATION_CUTOFF = 55.0

    private val NETWORK_RISK_LEVELS = doubleArrayOf(0.0, 0.5, 1.0)

    fun generate(seed: Long, size: Int): List<LabelledSample> {
        val random = DeterministicRandom(seed)
        return List(size) { nextSample(random) }
    }

    fun trainingSet(size: Int = PretrainedTrustModel.TRAINING_SAMPLES): List<LabelledSample> =
        generate(TRAIN_SEED, size)

    fun testSet(size: Int = 800): List<LabelledSample> = generate(TEST_SEED, size)

    private fun nextSample(random: DeterministicRandom): LabelledSample {
        val networkRisk = NETWORK_RISK_LEVELS[random.nextInt(NETWORK_RISK_LEVELS.size)]
        val compromised = if (random.nextBoolean(0.32)) 1.0 else 0.0
        val failedLoginRatio =
            (random.nextInt(TrustFeatures.MAX_TRACKED_LOGIN_ATTEMPTS + 1).toDouble() /
                TrustFeatures.MAX_TRACKED_LOGIN_ATTEMPTS).coerceAtMost(1.0)
        val suspicious = if (random.nextBoolean(0.28)) 1.0 else 0.0
        val transitionRate =
            random.nextInt(TrustFeatures.MAX_TRACKED_TRANSITIONS + 1).toDouble() /
                TrustFeatures.MAX_TRACKED_TRANSITIONS

        val latentTrust = 100.0 -
            40.0 * networkRisk -
            25.0 * compromised -
            25.0 * failedLoginRatio -
            20.0 * suspicious -
            15.0 * transitionRate -
            15.0 * networkRisk * compromised +
            random.nextGaussian() * 6.0

        val features = doubleArrayOf(
            networkRisk,
            compromised,
            failedLoginRatio,
            suspicious,
            transitionRate
        )
        return LabelledSample(features, if (latentTrust < DEGRADATION_CUTOFF) 1 else 0)
    }
}
