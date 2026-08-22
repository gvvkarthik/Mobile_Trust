package com.example.mobiletrust.data.model

enum class NetworkType(
    val displayName: String,
    val penalty: Int,
    val riskFactor: Double,
    val description: String
) {
    SECURE_WIFI("Secure Wi-Fi", 5, 0.0, "Trusted enterprise network"),
    MOBILE_4G("Mobile 4G", 15, 0.5, "Carrier cellular network"),
    PUBLIC_WIFI("Public Wi-Fi", 35, 1.0, "Untrusted open network")
}
