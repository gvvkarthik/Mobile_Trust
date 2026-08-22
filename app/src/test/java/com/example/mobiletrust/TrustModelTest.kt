package com.example.mobiletrust

import com.example.mobiletrust.data.model.BehaviourStatus
import com.example.mobiletrust.data.model.DeviceSecurityStatus
import com.example.mobiletrust.data.model.NetworkType
import com.example.mobiletrust.data.model.TrustInput
import com.example.mobiletrust.data.model.TrustPolicyConfig
import com.example.mobiletrust.domain.ml.FederatedTrainer
import com.example.mobiletrust.domain.ml.LogisticRegressionModel
import com.example.mobiletrust.domain.ml.LogisticRegressionTrainer
import com.example.mobiletrust.domain.ml.ModelEvaluator
import com.example.mobiletrust.domain.ml.PretrainedTrustModel
import com.example.mobiletrust.domain.ml.SyntheticDataset
import com.example.mobiletrust.domain.ml.TrustFeatures
import com.example.mobiletrust.domain.predictor.HybridTrustPredictor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrustModelTest {

    private val config = TrustPolicyConfig()
    private val testSet = SyntheticDataset.testSet()

    @Test
    fun pretrainedModelMeetsRequiredAccuracy() {
        val metrics = ModelEvaluator.evaluate(PretrainedTrustModel.model(), testSet)
        assertTrue(
            "Accuracy ${metrics.accuracy} is below the required 0.70",
            metrics.accuracy >= 0.70
        )
        assertTrue("Precision ${metrics.precision} is too low", metrics.precision >= 0.70)
        assertTrue("Recall ${metrics.recall} is too low", metrics.recall >= 0.70)
        assertEquals(testSet.size, metrics.sampleCount)
    }

    @Test
    fun modelBeatsMajorityClassBaseline() {
        val positives = testSet.count { it.label == 1 }
        val majorityAccuracy =
            maxOf(positives, testSet.size - positives).toDouble() / testSet.size
        val metrics = ModelEvaluator.evaluate(PretrainedTrustModel.model(), testSet)

        assertTrue(
            "Model accuracy ${metrics.accuracy} does not beat baseline $majorityAccuracy",
            metrics.accuracy > majorityAccuracy
        )
    }

    @Test
    fun retrainingReproducesComparableAccuracy() {
        val retrained = LogisticRegressionTrainer.train(SyntheticDataset.trainingSet())
        val retrainedAccuracy = ModelEvaluator.evaluate(retrained, testSet).accuracy
        val pretrainedAccuracy = ModelEvaluator.evaluate(PretrainedTrustModel.model(), testSet).accuracy

        assertTrue("Retrained accuracy $retrainedAccuracy below 0.70", retrainedAccuracy >= 0.70)
        assertEquals(pretrainedAccuracy, retrainedAccuracy, 0.02)
    }

    @Test
    fun syntheticDatasetIsDeterministic() {
        val first = SyntheticDataset.generate(4242L, 200)
        val second = SyntheticDataset.generate(4242L, 200)
        assertEquals(first, second)
        assertNotEquals(first, SyntheticDataset.generate(4243L, 200))
    }

    @Test
    fun inferenceCompletesWellUnderLatencyBudget() {
        val predictor = HybridTrustPredictor()
        val input = TrustInput(
            networkType = NetworkType.PUBLIC_WIFI,
            deviceSecurity = DeviceSecurityStatus.COMPROMISED,
            failedLoginAttempts = 3,
            behaviour = BehaviourStatus.SUSPICIOUS,
            networkTransitions = 2
        )

        repeat(100) { predictor.predict(input, config) }

        var worstMillis = 0.0
        repeat(1000) {
            val prediction = predictor.predict(input, config)
            worstMillis = maxOf(worstMillis, prediction.inferenceNanos / 1_000_000.0)
        }

        assertTrue("Worst inference took $worstMillis ms", worstMillis < 500.0)
    }

    @Test
    fun degradationProbabilityRisesWithRisk() {
        val model = PretrainedTrustModel.model()
        val safe = model.degradationProbability(TrustFeatures.extract(TrustInput()))
        val risky = model.degradationProbability(
            TrustFeatures.extract(
                TrustInput(
                    networkType = NetworkType.PUBLIC_WIFI,
                    deviceSecurity = DeviceSecurityStatus.COMPROMISED,
                    failedLoginAttempts = 5,
                    behaviour = BehaviourStatus.SUSPICIOUS,
                    networkTransitions = 6
                )
            )
        )

        assertTrue("Safe probability $safe should be low", safe < 0.2)
        assertTrue("Risky probability $risky should be high", risky > 0.9)
    }

    @Test
    fun featureVectorIsNormalised() {
        val features = TrustFeatures.extract(
            TrustInput(
                networkType = NetworkType.PUBLIC_WIFI,
                deviceSecurity = DeviceSecurityStatus.COMPROMISED,
                failedLoginAttempts = 99,
                behaviour = BehaviourStatus.SUSPICIOUS,
                networkTransitions = 99
            )
        )
        assertEquals(TrustFeatures.SIZE, features.size)
        assertTrue(features.all { it in 0.0..1.0 })
    }

    @Test
    fun federatedAveragingProducesAccurateGlobalModel() {
        val report = FederatedTrainer(rounds = 2, localEpochs = 60).run().report

        assertEquals(2, report.rounds.size)
        assertTrue(
            "Federated accuracy ${report.finalAccuracy} below 0.70",
            report.finalAccuracy >= 0.70
        )
        report.rounds.forEach { round ->
            assertEquals(report.clientCount, round.clientAccuracies.size)
        }
    }

    @Test
    fun federatedTrainingReturnsAPromotableGlobalModel() {
        val outcome = FederatedTrainer(rounds = 2, localEpochs = 60).run()
        val accuracy = ModelEvaluator.evaluate(outcome.globalModel, testSet).accuracy

        assertEquals(outcome.report.finalAccuracy, accuracy, 1e-9)
        assertTrue("Promoted model accuracy $accuracy below 0.70", accuracy >= 0.70)
    }

    @Test
    fun federatedAverageCombinesClientWeights() {
        val a = LogisticRegressionModel(DoubleArray(TrustFeatures.SIZE) { 2.0 }, 4.0)
        val b = LogisticRegressionModel(DoubleArray(TrustFeatures.SIZE) { 4.0 }, 8.0)
        val averaged = LogisticRegressionModel.average(listOf(a, b))

        assertTrue(averaged.weights.all { it == 3.0 })
        assertEquals(6.0, averaged.bias, 1e-9)
    }
}
