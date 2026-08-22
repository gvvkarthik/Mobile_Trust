package com.example.mobiletrust.domain.predictor

import com.example.mobiletrust.data.model.TrustInput
import com.example.mobiletrust.data.model.TrustResult

/**
 * Interface for Trust Prediction engines.
 * Easily replaceable with a TensorFlow Lite model in future iterations.
 */
interface TrustPredictor {
    fun predict(input: TrustInput): TrustResult
}
