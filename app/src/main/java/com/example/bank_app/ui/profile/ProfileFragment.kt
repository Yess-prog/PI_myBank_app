package com.example.bank_app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.bank_app.LoginActivity
import com.example.bank_app.R
import com.example.bank_app.utils.PreferencesHelper

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load user data
        loadUserData(view)

        // Setup click listeners
        setupClickListeners(view)
    }

    private fun loadUserData(view: View) {
        val userName = PreferencesHelper.getUserName(requireContext())
        val userEmail = PreferencesHelper.getUserEmail(requireContext())

        // Update profile name
        view.findViewById<android.widget.TextView>(R.id.profileName)?.apply {
            text = userName ?: "User"
        }

        // Update profile email
        view.findViewById<android.widget.TextView>(R.id.profileEmail)?.apply {
            text = userEmail ?: "user@example.com"
        }
    }

    private fun setupClickListeners(view: View) {
        // Personal Information Click
        view.findViewById<CardView>(R.id.personalInfoCard)?.setOnClickListener {
            Toast.makeText(requireContext(), "Personal Information clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to personal info screen
        }

        // Settings Click
        view.findViewById<CardView>(R.id.settingsCard)?.setOnClickListener {
            Toast.makeText(requireContext(), "Settings clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to settings screen
        }

        // Logout Click
        view.findViewById<CardView>(R.id.logoutCard)?.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        // Clear user data from SharedPreferences
        PreferencesHelper.clearLoginData(requireContext())

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate to login screen
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        // Close current activity
        requireActivity().finish()
    }

    // Refresh data when fragment is visible
    override fun onResume() {
        super.onResume()
        view?.let { loadUserData(it) }
    }
}