package com.example.bank_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.bank_app.utils.PreferencesHelper
import com.example.bank_app.viewmodel.LoginResult
import com.example.bank_app.viewmodel.LoginViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var progressBar: CircularProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if already logged in
       /* if (PreferencesHelper.isLoggedIn(this)) {
            navigateToHome()
            return
        }*/

        setContentView(R.layout.activity_login)

        initializeViews()
        setupObservers()
        setupClickListeners()
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        // Add progress bar to your XML or create programmatically
        // progressBar = findViewById(R.id.progressBar)
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is LoginResult.Success -> {
                    handleLoginSuccess(result.data)
                }
                is LoginResult.Error -> {
                    showError(result.message)
                }
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        }
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            performLogin()
        }

        findViewById<View>(R.id.tvSignup)?.setOnClickListener {
            // Navigate to signup activity
            Toast.makeText(this, "Sign up clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.tvForgot)?.setOnClickListener {
            // Navigate to forgot password activity
            Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Animate button
        btnLogin.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                btnLogin.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()

        // Perform login
        viewModel.login(email, password)
    }

    private fun handleLoginSuccess(data: com.example.bank_app.models.LoginResponse) {
        // Save login data
        data.token?.let { token ->
            PreferencesHelper.saveLoginData(
                context = this,
                token = token,
                userId = data.userId,
                email = data.email,
                name = "${data.firstName} ${data.lastName}"
            )
        }

        // Show success message
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()


        // Show success message
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

        // Navigate to home
        navigateToHome()
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showLoading() {
        btnLogin.isEnabled = false
        btnLogin.text = "Signing in..."
        // progressBar?.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        btnLogin.isEnabled = true
        btnLogin.text = "Sign In"
        // progressBar?.visibility = View.GONE
    }
}