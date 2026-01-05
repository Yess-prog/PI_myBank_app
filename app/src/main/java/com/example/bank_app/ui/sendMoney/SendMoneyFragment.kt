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

class SendMoneyFragment : Fragment() {

    private lateinit var recipientRibInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var sendButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_send_money, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recipientRibInput = view.findViewById(R.id.recipientRibInput)
        amountInput = view.findViewById(R.id.amountInput)
        descriptionInput = view.findViewById(R.id.descriptionInput)
        sendButton = view.findViewById(R.id.sendButton)
        cancelButton = view.findViewById(R.id.cancelButton)

        // Setup button listeners
        sendButton.setOnClickListener {
            validateAndProceed()
        }

        cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun validateAndProceed() {
        val recipientRib = recipientRibInput.text.toString().trim()
        val amountStr = amountInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()

        // Validation
        if (recipientRib.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter recipient RIB", Toast.LENGTH_SHORT).show()
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
        showFirstConfirmation(recipientRib, amount, description)
    }

    private fun showFirstConfirmation(recipientRib: String, amount: Double, description: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Transfer")
            .setMessage("""
                Transfer Amount: $${"%.2f".format(amount)}
                To RIB: $recipientRib
                Description: ${description.ifEmpty { "No description" }}
                
                Do you want to proceed?
            """.trimIndent())
            .setPositiveButton("Yes, Continue") { _, _ ->
                showSecondConfirmation(recipientRib, amount, description)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showSecondConfirmation(recipientRib: String, amount: Double, description: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Final Confirmation")
            .setMessage("""
                This action cannot be undone!
                
                Are you absolutely sure you want to send $${"%.2f".format(amount)} to $recipientRib?
            """.trimIndent())
            .setPositiveButton("Yes, Send Now") { _, _ ->
                performTransfer(recipientRib, amount, description)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performTransfer(recipientRib: String, amount: Double, description: String) {
        val token = PreferencesHelper.getToken(requireContext())
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the user's first account
        lifecycleScope.launch {
            try {
                // First, get user's accounts to find the from account
                val apiService = RetrofitClient.instance
                val accountsResponse = apiService.getAccounts("Bearer $token")

                if (!accountsResponse.isSuccessful || accountsResponse.body().isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Failed to load your accounts", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val fromAccountId = accountsResponse.body()!![0].id

                // Now perform the transfer
                val transferRequest = com.example.bank_app.api.TransferRequest(
                    fromAccountId = fromAccountId,
                    toRib = recipientRib,
                    amount = amount,
                    description = description
                )

                val transferResponse = apiService.transfer(
                    "Bearer $token",
                    transferRequest
                )

                if (transferResponse.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Transfer successful!",
                        Toast.LENGTH_LONG
                    ).show()

                    // Show success dialog and navigate back
                    AlertDialog.Builder(requireContext())
                        .setTitle("Success")
                        .setMessage("Your transfer of $${"%.2f".format(amount)} has been sent!")
                        .setPositiveButton("Done") { _, _ ->
                            findNavController().popBackStack()
                        }
                        .show()
                } else {
                    val errorMessage = transferResponse.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(
                        requireContext(),
                        "Transfer failed: $errorMessage",
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