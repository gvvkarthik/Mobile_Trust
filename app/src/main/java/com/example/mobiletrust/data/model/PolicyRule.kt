package com.example.mobiletrust.data.model

sealed interface RuleCondition {
    data class OnNetwork(val networks: Set<NetworkType>) : RuleCondition
    data class RoleNotIn(val roles: Set<UserRole>) : RuleCondition
    data class DeviceIs(val status: DeviceSecurityStatus) : RuleCondition
    data class BehaviourIs(val status: BehaviourStatus) : RuleCondition
    data class FailedLoginsAtLeast(val count: Int) : RuleCondition
    data class TransitionsAtLeast(val count: Int) : RuleCondition
    data class ScoreBelow(val score: Int) : RuleCondition
    data class All(val conditions: List<RuleCondition>) : RuleCondition
    data class AnyOf(val conditions: List<RuleCondition>) : RuleCondition
}

fun RuleCondition.matches(input: TrustInput, trustScore: Int): Boolean = when (this) {
    is RuleCondition.OnNetwork -> input.networkType in networks
    is RuleCondition.RoleNotIn -> input.userRole !in roles
    is RuleCondition.DeviceIs -> input.deviceSecurity == status
    is RuleCondition.BehaviourIs -> input.behaviour == status
    is RuleCondition.FailedLoginsAtLeast -> input.failedLoginAttempts >= count
    is RuleCondition.TransitionsAtLeast -> input.networkTransitions >= count
    is RuleCondition.ScoreBelow -> trustScore < score
    is RuleCondition.All -> conditions.all { it.matches(input, trustScore) }
    is RuleCondition.AnyOf -> conditions.any { it.matches(input, trustScore) }
}

data class PolicyRule(
    val id: String,
    val name: String,
    val description: String,
    val condition: RuleCondition,
    val action: SecurityAction,
    val enabled: Boolean = true
) {
    fun matches(input: TrustInput, trustScore: Int): Boolean =
        enabled && condition.matches(input, trustScore)

    companion object {
        fun defaults(): List<PolicyRule> = listOf(
            PolicyRule(
                id = "public-network-role-gate",
                name = "Public Network Role Gate",
                description = "Block access on public networks when the user is not in an approved role",
                condition = RuleCondition.All(
                    listOf(
                        RuleCondition.OnNetwork(setOf(NetworkType.PUBLIC_WIFI)),
                        RuleCondition.RoleNotIn(UserRole.PUBLIC_NETWORK_APPROVED)
                    )
                ),
                action = SecurityAction.TERMINATE_SESSION
            ),
            PolicyRule(
                id = "compromised-device-reauth",
                name = "Compromised Device Re-Auth",
                description = "Force re-authentication whenever device integrity is compromised",
                condition = RuleCondition.DeviceIs(DeviceSecurityStatus.COMPROMISED),
                action = SecurityAction.REQUIRE_REAUTHENTICATION
            ),
            PolicyRule(
                id = "brute-force-lockout",
                name = "Brute Force Lockout",
                description = "Terminate the session after 5 or more failed login attempts",
                condition = RuleCondition.FailedLoginsAtLeast(5),
                action = SecurityAction.TERMINATE_SESSION
            ),
            PolicyRule(
                id = "rapid-transition-warning",
                name = "Rapid Transition Watch",
                description = "Warn when 4 or more network transitions occur in one session",
                condition = RuleCondition.TransitionsAtLeast(4),
                action = SecurityAction.SHOW_SECURITY_WARNING
            ),
            PolicyRule(
                id = "untrusted-suspicious-combo",
                name = "Untrusted Network Anomaly",
                description = "Force re-authentication on suspicious behaviour outside trusted Wi-Fi",
                condition = RuleCondition.All(
                    listOf(
                        RuleCondition.BehaviourIs(BehaviourStatus.SUSPICIOUS),
                        RuleCondition.OnNetwork(setOf(NetworkType.MOBILE_4G, NetworkType.PUBLIC_WIFI))
                    )
                ),
                action = SecurityAction.REQUIRE_REAUTHENTICATION,
                enabled = false
            )
        )
    }
}
