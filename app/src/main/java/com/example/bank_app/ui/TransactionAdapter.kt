package com.example.bank_app.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bank_app.R
import com.example.bank_app.api.Transaction
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    override fun getItemCount(): Int = transactions.size

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val transactionIcon: ImageView = itemView.findViewById(R.id.transactionIcon)
        private val transactionTitle: TextView = itemView.findViewById(R.id.transactionTitle)
        private val transactionSubtitle: TextView = itemView.findViewById(R.id.transactionSubtitle)
        private val transactionAmount: TextView = itemView.findViewById(R.id.transactionAmount)
        private val transactionDate: TextView = itemView.findViewById(R.id.transactionDate)

        fun bind(transaction: Transaction) {
            // Set transaction type
            val isDebit = transaction.fromAccountId.toString().isNotEmpty()
            transactionTitle.text = if (isDebit) "Money Sent" else "Money Received"
            transaction.description?.let { transactionSubtitle.text = it?.ifEmpty { "Transfer" } }

            // Set amount
            val amountText = String.format("%.2f", transaction.amount)
            transactionAmount.text = if (isDebit) "-$amountText" else "+$amountText"
            transactionAmount.setTextColor(
                if (isDebit) itemView.context.getColor(R.color.error)
                else itemView.context.getColor(R.color.success)
            )

            // Set date
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                val date = inputFormat.parse(transaction.createdAt)
                transactionDate.text = if (date != null) outputFormat.format(date) else transaction.createdAt
            } catch (e: Exception) {
                transactionDate.text = transaction.createdAt
            }

            // Set icon
            transactionIcon.setImageResource(
                if (isDebit) R.drawable.ic_send else R.drawable.ic_request
            )
        }
    }
}