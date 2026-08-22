package com.example.mobiletrust.data.model

enum class UserRole(val displayName: String, val clearance: Int) {
    ADMIN("Admin", 3),
    COMMANDER("Commander", 2),
    FIELD_OPERATOR("Field Operator", 1),
    GUEST("Guest", 0);

    companion object {
        val PUBLIC_NETWORK_APPROVED: Set<UserRole> = setOf(ADMIN, COMMANDER)
    }
}
