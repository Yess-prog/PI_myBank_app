package com.example.bank_app.ui.notifications

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.bank_app.R
import com.example.bank_app.api.RetrofitClient
import com.example.bank_app.api.TransferRequestItem
import com.example.bank_app.utils.PreferencesHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TransferRequestAdapter(
    private val requests: MutableList<TransferRequestItem>,
    private val onAccept: (TransferRequestItem, Int) -> Unit,
    private val onReject: (TransferRequestItem, Int) -> Unit,
    private val context: Context
) : RecyclerView.Adapter<TransferRequestAdapter.RequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transfer_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        holder.bind(request, position)
    }

    override fun getItemCount(): Int = requests.size

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderName: TextView = itemView.findViewById(R.id.senderName)
        private val senderEmail: TextView = itemView.findViewById(R.id.senderEmail)
        private val amount: TextView = itemView.findViewById(R.id.amount)
        private val description: TextView = itemView.findViewById(R.id.description)
        private val date: TextView = itemView.findViewById(R.id.date)
        private val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        private val rejectButton: Button = itemView.findViewById(R.id.rejectButton)

        fun bind(request: TransferRequestItem, position: Int) {
            senderName.text = request.senderName
            senderEmail.text = request.senderEmail
            amount.text = String.format("%.2f", request.amount)
            description.text = request.description.ifEmpty { "No description" }

            // Format date
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                val parsedDate = inputFormat.parse(request.createdAt)
                date.text = if (parsedDate != null) outputFormat.format(parsedDate) else request.createdAt
            } catch (e: Exception) {
                date.text = request.createdAt
            }

            acceptButton.setOnClickListener {
                showFirstConfirmation(request, position, true)
            }

            rejectButton.setOnClickListener {
                showFirstConfirmation(request, position, false)
            }
        }

        private fun showFirstConfirmation(request: TransferRequestItem, position: Int, isAccept: Boolean) {
            val action = if (isAccept) "Accept" else "Reject"
            val message = if (isAccept) {
                "Accept transfer of $${String.format("%.2f", request.amount)} from ${request.senderName}?"
            } else {
                "Reject transfer request from ${request.senderName}?"
            }

            AlertDialog.Builder(context)
                .setTitle("$action Transfer Request")
                .setMessage(message)
                .setPositiveButton("Yes, $action") { _, _ ->
                    showSecondConfirmation(request, position, isAccept)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        private fun showSecondConfirmation(request: TransferRequestItem, position: Int, isAccept: Boolean) {
            val action = if (isAccept) "Accept" else "Reject"
            val message = if (isAccept) {
                "This action cannot be undone!\n\nAre you absolutely sure you want to accept $${String.format("%.2f", request.amount)} from ${request.senderName}?"
            } else {
                "Are you absolutely sure you want to reject this transfer request?"
            }

            AlertDialog.Builder(context)
                .setTitle("Final Confirmation")
                .setMessage(message)
                .setPositiveButton("Yes, Confirm $action") { _, _ ->
                    if (isAccept) {
                        acceptTransfer(request, position)
                    } else {
                        rejectTransfer(request, position)
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        private fun acceptTransfer(request: TransferRequestItem, position: Int) {
            val token = PreferencesHelper.getToken(context)
            if (token.isNullOrEmpty()) {
                Toast.makeText(context, "Token not found", Toast.LENGTH_SHORT).show()
                return
            }

            (context as LifecycleOwner).lifecycleScope.launch {
                try {
                    val apiService = RetrofitClient.instance
                    val response = apiService.acceptTransferRequest(
                        "Bearer $token",
                        request.id
                    )

                    if (response.isSuccessful) {
                        Toast.makeText(
                            context,
                            "Transfer accepted! $${String.format("%.2f", request.amount)} received",
                            Toast.LENGTH_LONG
                        ).show()
                        requests.removeAt(position)
                        notifyItemRemoved(position)
                        onAccept(request, position)
                    } else {
                        Toast.makeText(context, "Failed to accept transfer", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun rejectTransfer(request: TransferRequestItem, position: Int) {
            val token = PreferencesHelper.getToken(context)
            if (token.isNullOrEmpty()) {
                Toast.makeText(context, "Token not found", Toast.LENGTH_SHORT).show()
                return
            }

            (context as LifecycleOwner).lifecycleScope.launch {
                try {
                    val apiService = RetrofitClient.instance
                    val response = apiService.rejectTransferRequest(
                        "Bearer $token",
                        request.id
                    )

                    if (response.isSuccessful) {
                        Toast.makeText(context, "Transfer request rejected", Toast.LENGTH_LONG).show()
                        requests.removeAt(position)
                        notifyItemRemoved(position)
                        onReject(request, position)
                    } else {
                        Toast.makeText(context, "Failed to reject transfer", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}