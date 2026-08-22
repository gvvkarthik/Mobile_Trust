package com.example.mobiletrust.security

import com.example.mobiletrust.data.model.RiskLevel
import com.example.mobiletrust.data.model.SecurityAction
import com.example.mobiletrust.data.model.SessionStatus

data class PolicyDecision(
    val sessionStatus: SessionStatus,
    val securityAction: SecurityAction
)

object SecurityPolicyEngine {

    /**
     * Evaluates cybersecurity policy based on the calculated Trust Score.
     *
     * Trust Score 71–100: Session Status = ACTIVE, Action = ALLOW ACCESS
     * Trust Score 41–70:  Session Status = WARNING, Action = SHOW SECURITY WARNING
     * Trust Score 21–40:  Session Status = REAUTH_REQUIRED, Action = REQUIRE RE-AUTHENTICATION
     * Trust Score 0–20:   Session Status = TERMINATED, Action = TERMINATE SESSION
     */
    fun evaluate(trustScore: Int): PolicyDecision {
        return when {
            trustScore in 71..100 -> PolicyDecision(
                sessionStatus = SessionStatus.ACTIVE,
                securityAction = SecurityAction.ALLOW_ACCESS
            )
            trustScore in 41..70 -> PolicyDecision(
                sessionStatus = SessionStatus.WARNING,
                securityAction = SecurityAction.SHOW_SECURITY_WARNING
            )
            trustScore in 21..40 -> PolicyDecision(
                sessionStatus = SessionStatus.REAUTH_REQUIRED,
                securityAction = SecurityAction.REQUIRE_REAUTHENTICATION
            )
            else -> PolicyDecision(
                sessionStatus = SessionStatus.TERMINATED,
                securityAction = SecurityAction.TERMINATE_SESSION
            )
        }
    }
}
