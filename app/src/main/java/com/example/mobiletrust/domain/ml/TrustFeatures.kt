package com.example.mobiletrust.domain.ml

import com.example.mobiletrust.data.model.BehaviourStatus
import com.example.mobiletrust.data.model.DeviceSecurityStatus
import com.example.mobiletrust.data.model.TrustInput

object TrustFeatures {

    const val SIZE = 5

    const val MAX_TRACKED_LOGIN_ATTEMPTS = 5
    const val MAX_TRACKED_TRANSITIONS = 6

    val NAMES = listOf(
        "Network risk",
        "Device compromised",
        "Failed login ratio",
        "Suspicious behaviour",
        "Transition rate"
    )

    fun extract(input: TrustInput): DoubleArray = doubleArrayOf(
        input.networkType.riskFactor,
        if (input.deviceSecurity == DeviceSecurityStatus.COMPROMISED) 1.0 else 0.0,
        (input.failedLoginAttempts.toDouble() / MAX_TRACKED_LOGIN_ATTEMPTS).coerceIn(0.0, 1.0),
        if (input.behaviour == BehaviourStatus.SUSPICIOUS) 1.0 else 0.0,
        (input.networkTransitions.toDouble() / MAX_TRACKED_TRANSITIONS).coerceIn(0.0, 1.0)
    )
}
