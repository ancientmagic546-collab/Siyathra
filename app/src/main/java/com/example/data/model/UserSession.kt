package com.example.data.model

data class UserSession(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val isAdmin: Boolean = true,
    val isDemoMode: Boolean = false
)
