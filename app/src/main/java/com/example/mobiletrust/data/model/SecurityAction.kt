package com.example.mobiletrust.data.model

enum class SecurityAction(val displayName: String, val description: String) {
    ALLOW_ACCESS(
        "ALLOW ACCESS",
        "Trust level is optimal. Full application access granted."
    ),
    SHOW_SECURITY_WARNING(
        "SHOW SECURITY WARNING",
        "Your device trust level has decreased."
    ),
    REQUIRE_REAUTHENTICATION(
        "REQUIRE RE-AUTHENTICATION",
        "Your current device or network environment is considered risky."
    ),
    TERMINATE_SESSION(
        "TERMINATE SESSION",
        "Your trust score is critically low. Access has been blocked."
    )
}
