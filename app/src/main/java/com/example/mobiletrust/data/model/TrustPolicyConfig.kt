package com.example.mobiletrust.data.model

data class TrustThresholds(
    val lowMin: Int = 71,
    val mediumMin: Int = 41,
    val highMin: Int = 21
)

data class TrustPolicyConfig(
    val thresholds: TrustThresholds = TrustThresholds(),
    val mlWeight: Double = 0.5,
    val alertThreshold: Int = TrustThresholds().lowMin,
    val rules: List<PolicyRule> = PolicyRule.defaults()
) {
    val ruleWeight: Double get() = 1.0 - mlWeight

    fun withRuleEnabled(id: String, enabled: Boolean): TrustPolicyConfig =
        copy(rules = rules.map { if (it.id == id) it.copy(enabled = enabled) else it })

    fun withMlWeight(weight: Double): TrustPolicyConfig =
        copy(mlWeight = weight.coerceIn(0.0, 1.0))
}
