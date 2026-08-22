package com.example.mobiletrust.data.model

enum class NetworkType(val displayName: String, val penalty: Int) {
    SECURE_WIFI("Secure Wi-Fi", 5),
    MOBILE_4G("Mobile 4G", 15),
    PUBLIC_WIFI("Public Wi-Fi", 35)
}
