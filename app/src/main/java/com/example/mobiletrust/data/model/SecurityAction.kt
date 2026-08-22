package com.example.mobiletrust.data.model

enum class SecurityAction(
    val displayName: String,
    val description: String,
    val severity: Int,
    val sessionStatus: SessionStatus
) {
    ALLOW_ACCESS(
        "ALLOW ACCESS",
        "Trust level is optimal. Full application access granted.",
        0,
        SessionStatus.ACTIVE
    ),
    SHOW_SECURITY_WARNING(
        "SHOW SECURITY WARNING",
        "Your device trust level has decreased.",
        1,
        SessionStatus.WARNING
    ),
    REQUIRE_REAUTHENTICATION(
        "REQUIRE RE-AUTHENTICATION",
        "Your current device or network environment is considered risky.",
        2,
        SessionStatus.REAUTH_REQUIRED
    ),
    TERMINATE_SESSION(
        "TERMINATE SESSION",
        "Your trust score is critically low. Access has been blocked.",
        3,
        SessionStatus.TERMINATED
    );

    companion object {
        fun mostSevere(actions: Collection<SecurityAction>): SecurityAction =
            actions.maxByOrNull { it.severity } ?: ALLOW_ACCESS
    }
}
