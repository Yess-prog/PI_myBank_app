package com.example.bank_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bank_app.models.LoginResponse
import com.example.bank_app.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(email: String, password: String) {
        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            _loginResult.value = LoginResult.Error("Email and password are required")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginResult.value = LoginResult.Error("Invalid email format")
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = repository.login(email, password)
                _isLoading.value = false

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null && loginResponse.token.isNotEmpty()) {
                        _loginResult.value = LoginResult.Success(loginResponse)
                    } else {
                        _loginResult.value = LoginResult.Error(
                            loginResponse?.message ?: "Login failed"
                        )
                    }
                } else {
                    _loginResult.value = LoginResult.Error(
                        "Server error: ${response.code()} - ${response.errorBody()?.string()}"
                    )
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _loginResult.value = LoginResult.Error(
                    "Network error: ${e.message}"
                )
            }
        }
    }
}

sealed class LoginResult {
    data class Success(val data: LoginResponse) : LoginResult()
    data class Error(val message: String) : LoginResult()
}