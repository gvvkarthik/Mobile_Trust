package com.example.mobiletrust.data.model

enum class SessionStatus(val displayName: String) {
    ACTIVE("ACTIVE"),
    WARNING("WARNING"),
    REAUTH_REQUIRED("REAUTH_REQUIRED"),
    TERMINATED("TERMINATED")
}
