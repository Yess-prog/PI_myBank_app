package com.example.bank_app.models

data class LoginResponse(
    val message: String,
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val token: String,
    val user: User? = null,  // Keep for compatibility
    val success: Boolean? = null  // Keep for compatibility
)

data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val balance: Double? = null
)