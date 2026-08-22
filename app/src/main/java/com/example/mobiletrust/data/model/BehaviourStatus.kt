package com.example.mobiletrust.data.model

enum class BehaviourStatus(val displayName: String, val penalty: Int) {
    NORMAL("Normal", 0),
    SUSPICIOUS("Suspicious", 20)
}
