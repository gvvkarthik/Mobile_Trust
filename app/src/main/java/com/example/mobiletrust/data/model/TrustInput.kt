package com.example.mobiletrust.data.model

data class TrustInput(
    val networkType: NetworkType = NetworkType.SECURE_WIFI,
    val deviceSecurity: DeviceSecurityStatus = DeviceSecurityStatus.SECURE,
    val failedLoginAttempts: Int = 0,
    val behaviour: BehaviourStatus = BehaviourStatus.NORMAL,
    val userRole: UserRole = UserRole.COMMANDER,
    val networkTransitions: Int = 0
)
