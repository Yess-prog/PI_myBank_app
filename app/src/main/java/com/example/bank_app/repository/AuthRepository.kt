package com.example.bank_app.repository

import com.example.bank_app.api.RetrofitClient
import com.example.bank_app.models.LoginRequest
import com.example.bank_app.models.LoginResponse
import retrofit2.Response

class AuthRepository {
    private val apiService = RetrofitClient.instance

    suspend fun login(email: String, password: String): Response<LoginResponse> {
        return apiService.login(LoginRequest(email, password))
    }
}