package com.example.mobiletrust.data.model

enum class DeviceSecurityStatus(val displayName: String, val penalty: Int) {
    SECURE("Secure", 0),
    COMPROMISED("Compromised", 25)
}
