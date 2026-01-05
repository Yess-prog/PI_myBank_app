
package com.example.bank_app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bank_app.R
import com.example.bank_app.api.RetrofitClient
import com.example.bank_app.utils.PreferencesHelper
import kotlinx.coroutines.launch

class RequestMoneyFragment : Fragment() {

    private lateinit var recipientEmailInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var requestButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_request_money, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recipientEmailInput = view.findViewById(R.id.recipientEmailInput)
        amountInput = view.findViewById(R.id.amountInput)
        descriptionInput = view.findViewById(R.id.descriptionInput)
        requestButton = view.findViewById(R.id.requestButton)
        cancelButton = view.findViewById(R.id.cancelButton)

        // Setup button listeners
        requestButton.setOnClickListener {
            validateAndProceed()
        }

        cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun validateAndProceed() {
        val recipientEmail = recipientEmailInput.text.toString().trim()
        val amountStr = amountInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()

        // Validation
        if (recipientEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter recipient email", Toast.LENGTH_SHORT).show()
            return
        }

        if (amountStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = try {
            amountStr.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (amount <= 0) {
            Toast.makeText(requireContext(), "Amount must be greater than 0", Toast.LENGTH_SHORT).show()
            return
        }

        // Show first confirmation dialog
        showFirstConfirmation(recipientEmail, amount, description)
    }

    private fun showFirstConfirmation(recipientEmail: String, amount: Double, description: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Request")
            .setMessage("""
                Request Amount: $${"%.2f".format(amount)}
                From: $recipientEmail
                Description: ${description.ifEmpty { "No description" }}
                
                Do you want to proceed?
            """.trimIndent())
            .setPositiveButton("Yes, Continue") { _, _ ->
                showSecondConfirmation(recipientEmail, amount, description)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showSecondConfirmation(recipientEmail: String, amount: Double, description: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Final Confirmation")
            .setMessage("""
                This will send a transfer request to $recipientEmail.
                
                Are you absolutely sure you want to request $${"%.2f".format(amount)}?
            """.trimIndent())
            .setPositiveButton("Yes, Send Request") { _, _ ->
                performRequest(recipientEmail, amount, description)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performRequest(recipientEmail: String, amount: Double, description: String) {
        val token = PreferencesHelper.getToken(requireContext())
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the user's first account
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance

                // First, get user's accounts to find the from account
                val accountsResponse = apiService.getAccounts("Bearer $token")

                if (!accountsResponse.isSuccessful || accountsResponse.body().isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Failed to load your accounts", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val fromAccountId = accountsResponse.body()!![0].id

                // Get recipient user ID by email
                val recipientResponse = apiService.getUserByEmail("Bearer $token", recipientEmail)

                if (!recipientResponse.isSuccessful || recipientResponse.body() == null) {
                    Toast.makeText(requireContext(), "Recipient not found", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val recipientUser = recipientResponse.body()!!

                // Get recipient's first account
                val recipientAccountsResponse = apiService.getAccountsByUserId("Bearer $token", recipientUser.id)

                if (!recipientAccountsResponse.isSuccessful || recipientAccountsResponse.body().isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Recipient has no accounts", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val toAccountId = recipientAccountsResponse.body()!![0].id

                // Now create the transfer request
                val requestBody = com.example.bank_app.api.TransferRequestBody(
                    toUserId = recipientUser.id,
                    fromAccountId = fromAccountId,
                    toAccountId = toAccountId,
                    amount = amount,
                    description = description
                )

                val requestResponse = apiService.createTransferRequest(
                    "Bearer $token",
                    requestBody
                )
                
                if (requestResponse.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Request sent to $recipientEmail!",
                        Toast.LENGTH_LONG
                    ).show()

                    // Show success dialog and navigate back
                    AlertDialog.Builder(requireContext())
                        .setTitle("Success")
                        .setMessage("Your request for $${"%.2f".format(amount)} has been sent to $recipientEmail!")
                        .setPositiveButton("Done") { _, _ ->
                            findNavController().popBackStack()
                        }
                        .show()
                } else {
                    val errorMessage = requestResponse.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(
                        requireContext(),
                        "Request failed: $errorMessage",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}