package com.example.mobiletrust.domain.ml

import com.example.mobiletrust.data.model.FederatedReport
import com.example.mobiletrust.data.model.FederatedRound

data class FederatedOutcome(
    val report: FederatedReport,
    val globalModel: LogisticRegressionModel
)

class FederatedTrainer(
    private val clientCount: Int = 4,
    private val samplesPerClient: Int = 600,
    private val rounds: Int = 5,
    private val localEpochs: Int = 120,
    private val learningRate: Double = PretrainedTrustModel.LEARNING_RATE
) {

    fun run(): FederatedOutcome {
        val startedAt = System.nanoTime()
        val shards = List(clientCount) { client ->
            SyntheticDataset.generate(SyntheticDataset.TRAIN_SEED + client * 7919L, samplesPerClient)
        }
        val validation = SyntheticDataset.testSet()

        var global = LogisticRegressionModel.zeros()
        val roundReports = mutableListOf<FederatedRound>()

        for (round in 1..rounds) {
            val localModels = shards.map { shard ->
                LogisticRegressionTrainer.train(
                    samples = shard,
                    learningRate = learningRate,
                    epochs = localEpochs,
                    initial = global
                )
            }
            val clientAccuracies = localModels.mapIndexed { index, model ->
                ModelEvaluator.evaluate(model, shards[index]).accuracy
            }
            global = LogisticRegressionModel.average(localModels)
            roundReports += FederatedRound(
                round = round,
                globalAccuracy = ModelEvaluator.evaluate(global, validation).accuracy,
                clientAccuracies = clientAccuracies
            )
        }

        return FederatedOutcome(
            report = FederatedReport(
                clientCount = clientCount,
                samplesPerClient = samplesPerClient,
                rounds = roundReports,
                durationMillis = (System.nanoTime() - startedAt) / 1_000_000
            ),
            globalModel = global
        )
    }
}
