package com.example.mobiletrust.domain.predictor

import com.example.mobiletrust.data.model.TrustInput
import com.example.mobiletrust.data.model.TrustPolicyConfig
import com.example.mobiletrust.domain.ml.LogisticRegressionModel
import com.example.mobiletrust.domain.ml.PretrainedTrustModel
import com.example.mobiletrust.domain.ml.TrustFeatures
import kotlin.math.roundToInt

class HybridTrustPredictor(
    initialModel: LogisticRegressionModel = PretrainedTrustModel.model()
) : TrustPredictor {

    @Volatile
    var model: LogisticRegressionModel = initialModel
        private set

    fun updateModel(updated: LogisticRegressionModel) {
        model = updated
    }

    override fun predict(input: TrustInput, config: TrustPolicyConfig): TrustPrediction {
        val startedAt = System.nanoTime()

        val penalties = PenaltyCalculator.breakdown(input)
        val ruleScore = (PenaltyCalculator.BASE_SCORE - penalties.total).coerceIn(0, 100)

        val probability = model.degradationProbability(TrustFeatures.extract(input))
        val mlScore = ((1.0 - probability) * 100.0).roundToInt().coerceIn(0, 100)

        val blended = (config.ruleWeight * ruleScore + config.mlWeight * mlScore)
            .roundToInt()
            .coerceIn(0, 100)

        return TrustPrediction(
            ruleScore = ruleScore,
            mlScore = mlScore,
            blendedScore = blended,
            degradationProbability = probability,
            penalties = penalties,
            inferenceNanos = System.nanoTime() - startedAt
        )
    }
}
