package com.example.bank_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.bank_app.databinding.ActivityMainBinding
import com.example.bank_app.utils.PreferencesHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load user data from SharedPreferences
        loadUserData()

        // Setup Navigation - THIS MUST COME BEFORE OTHER LISTENERS
        setupNavigation()

        // Setup click listeners for top bar icons
        setupTopBarListeners()
    }

    private fun setupNavigation() {
        try {
            // Get the NavHostFragment
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                    as? NavHostFragment

            if (navHostFragment == null) {
                Log.e("MainActivity", "NavHostFragment is NULL - check if nav_host_fragment ID exists in activity_main.xml")
                return
            }

            // Get the NavController from the NavHostFragment
            navController = navHostFragment.navController
            Log.d("MainActivity", "NavController obtained successfully")

            // Get the BottomNavigationView
            val navView: BottomNavigationView = binding.navView

            // Connect the BottomNavigationView with the NavController
            // This handles all navigation automatically - don't add extra listeners!
            navView.setupWithNavController(navController)

            Log.d("MainActivity", "Navigation setup completed successfully")

        } catch (e: Exception) {
            Log.e("MainActivity", "Navigation setup error: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun loadUserData() {
        // Get user data from SharedPreferences
        val userName = PreferencesHelper.getUserName(this)
        val userEmail = PreferencesHelper.getUserEmail(this)

        // Get first name from full name
        val firstName = userName?.split(" ")?.firstOrNull() ?: "User"

        // Update UI with user data
        try {
            findViewById<android.widget.TextView>(R.id.userName)?.text = userName ?: "User"
            findViewById<android.widget.TextView>(R.id.userEmail)?.text = userEmail ?: "user@example.com"
            findViewById<android.widget.TextView>(R.id.greetingText)?.text = "Welcome back, $firstName"
        } catch (e: Exception) {
            Log.e("MainActivity", "Error loading user data: ${e.message}")
        }
    }

    private fun setupTopBarListeners() {
        try {
            // Profile Card Click Listener
            findViewById<androidx.cardview.widget.CardView>(R.id.profileCard)?.setOnClickListener {
                try {
                    navController.navigate(R.id.navigation_profile)
                    binding.navView.selectedItemId = R.id.navigation_profile
                } catch (e: Exception) {
                    Log.e("MainActivity", "Profile navigation error: ${e.message}")
                }
            }

            // Notification Card Click Listener
            findViewById<androidx.cardview.widget.CardView>(R.id.notificationCard)?.setOnClickListener {
                try {
                    navController.navigate(R.id.notificationsFragment)
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Notifications navigation error: ${e.message}")
                }
            }

            // Logout on long press
            findViewById<android.widget.TextView>(R.id.appTitle)?.setOnLongClickListener {
                logout()
                true
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up top bar listeners: ${e.message}")
        }
    }

    private fun logout() {
        // Clear user data from SharedPreferences
        PreferencesHelper.clearLoginData(this)

        // Navigate to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Function to refresh user data (call this after profile update)
    fun refreshUserData() {
        loadUserData()
    }
}