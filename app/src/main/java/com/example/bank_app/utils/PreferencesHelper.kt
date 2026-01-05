package com.example.bank_app.utils

import android.content.Context
import android.content.SharedPreferences

object PreferencesHelper {
    private const val PREF_NAME = "banking_app_pref"
    private const val TOKEN_KEY = "token"
    private const val USER_ID_KEY = "user_id"
    private const val USER_EMAIL_KEY = "user_email"
    private const val USER_NAME_KEY = "user_name"
    private const val USER_FIRST_NAME_KEY = "user_first_name"
    private const val USER_LAST_NAME_KEY = "user_last_name"
    private const val IS_LOGGED_IN_KEY = "is_logged_in"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // ============ SAVE FUNCTIONS ============

    /**
     * Save all login data after successful login
     * Call this right after login is successful
     */
    fun saveLoginData(
        context: Context,
        token: String,
        userId: Int,
        email: String,
        name: String
    ) {
        val pref = getSharedPreferences(context)

        // Split name into first and last name
        val nameParts = name.split(" ")
        val firstName = nameParts.getOrNull(0) ?: "User"
        val lastName = nameParts.getOrNull(1) ?: ""

        pref.edit().apply {
            putString(TOKEN_KEY, token)
            putInt(USER_ID_KEY, userId)
            putString(USER_EMAIL_KEY, email)
            putString(USER_NAME_KEY, name)
            putString(USER_FIRST_NAME_KEY, firstName)
            putString(USER_LAST_NAME_KEY, lastName)
            putBoolean(IS_LOGGED_IN_KEY, true)
            apply()
        }
    }

    // ============ GET FUNCTIONS ============

    /**
     * Get JWT token for API requests
     * Usage: val token = PreferencesHelper.getToken(context)
     */
    fun getToken(context: Context): String? {
        return getSharedPreferences(context).getString(TOKEN_KEY, null)
    }

    /**
     * Get user ID
     * Usage: val userId = PreferencesHelper.getUserId(context)
     */
    fun getUserId(context: Context): Int {
        return getSharedPreferences(context).getInt(USER_ID_KEY, 0)
    }

    /**
     * Get user email
     * Usage: val email = PreferencesHelper.getUserEmail(context)
     */
    fun getUserEmail(context: Context): String? {
        return getSharedPreferences(context).getString(USER_EMAIL_KEY, null)
    }

    /**
     * Get user full name
     * Usage: val name = PreferencesHelper.getUserName(context)
     */
    fun getUserName(context: Context): String? {
        return getSharedPreferences(context).getString(USER_NAME_KEY, null)
    }

    /**
     * Get user first name
     * Usage: val firstName = PreferencesHelper.getUserFirstName(context)
     */
    fun getUserFirstName(context: Context): String? {
        return getSharedPreferences(context).getString(USER_FIRST_NAME_KEY, null)
    }

    /**
     * Get user last name
     * Usage: val lastName = PreferencesHelper.getUserLastName(context)
     */
    fun getUserLastName(context: Context): String? {
        return getSharedPreferences(context).getString(USER_LAST_NAME_KEY, null)
    }

    /**
     * Check if user is logged in
     * Usage: if (PreferencesHelper.isLoggedIn(context)) { ... }
     */
    fun isLoggedIn(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(IS_LOGGED_IN_KEY, false)
    }

    // ============ UPDATE FUNCTIONS ============

    /**
     * Update user name after profile change
     * Usage: PreferencesHelper.updateUserName(context, "John Doe")
     */
    fun updateUserName(context: Context, name: String) {
        val pref = getSharedPreferences(context)
        val nameParts = name.split(" ")
        val firstName = nameParts.getOrNull(0) ?: "User"
        val lastName = nameParts.getOrNull(1) ?: ""

        pref.edit().apply {
            putString(USER_NAME_KEY, name)
            putString(USER_FIRST_NAME_KEY, firstName)
            putString(USER_LAST_NAME_KEY, lastName)
            apply()
        }
    }

    /**
     * Update user email after profile change
     * Usage: PreferencesHelper.updateUserEmail(context, "newemail@example.com")
     */
    fun updateUserEmail(context: Context, email: String) {
        getSharedPreferences(context).edit().putString(USER_EMAIL_KEY, email).apply()
    }

    /**
     * Update first name
     * Usage: PreferencesHelper.updateUserFirstName(context, "John")
     */
    fun updateUserFirstName(context: Context, firstName: String) {
        getSharedPreferences(context).edit().putString(USER_FIRST_NAME_KEY, firstName).apply()
    }

    /**
     * Update last name
     * Usage: PreferencesHelper.updateUserLastName(context, "Doe")
     */
    fun updateUserLastName(context: Context, lastName: String) {
        getSharedPreferences(context).edit().putString(USER_LAST_NAME_KEY, lastName).apply()
    }

    /**
     * Update token (in case it expires and is refreshed)
     * Usage: PreferencesHelper.updateToken(context, newToken)
     */
    fun updateToken(context: Context, token: String) {
        getSharedPreferences(context).edit().putString(TOKEN_KEY, token).apply()
    }

    /**
     * Update user ID
     * Usage: PreferencesHelper.updateUserId(context, 123)
     */
    fun updateUserId(context: Context, userId: Int) {
        getSharedPreferences(context).edit().putInt(USER_ID_KEY, userId).apply()
    }

    // ============ CLEAR FUNCTIONS ============

    /**
     * Clear all data when user logs out
     * Usage: PreferencesHelper.clearLoginData(context)
     */
    fun clearLoginData(context: Context) {
        getSharedPreferences(context).edit().clear().apply()
    }

    /**
     * Clear only token (for token refresh scenarios)
     * Usage: PreferencesHelper.clearToken(context)
     */
    fun clearToken(context: Context) {
        getSharedPreferences(context).edit().remove(TOKEN_KEY).apply()
    }

    /**
     * Check if token exists
     * Usage: if (PreferencesHelper.hasToken(context)) { ... }
     */
    fun hasToken(context: Context): Boolean {
        return getToken(context) != null
    }

    /**
     * Get all stored preferences (for debugging)
     * Usage: val allData = PreferencesHelper.getAllData(context)
     */
    fun getAllData(context: Context): Map<String, *> {
        return getSharedPreferences(context).all
    }
}