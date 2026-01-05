package com.example.bank_app.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bank_app.R
import com.example.bank_app.api.RetrofitClient
import com.example.bank_app.ui.home.TransactionAdapter
import com.example.bank_app.utils.PreferencesHelper
import kotlinx.coroutines.launch

class TransactionsFragment : Fragment() {

    private lateinit var transactionsRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        transactionsRecyclerView = view.findViewById(R.id.transactionsRecyclerView)
        transactionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Setup filter buttons
        setupFilters(view)

        // Load transactions
        loadTransactions()
    }

    private fun setupFilters(view: View) {
        // Date filter button
        view.findViewById<View>(R.id.dateFilterButton)?.setOnClickListener {
            Toast.makeText(requireContext(), "Date filter clicked", Toast.LENGTH_SHORT).show()
            // TODO: Implement date filter
        }

        // Type filter button
        view.findViewById<View>(R.id.typeFilterButton)?.setOnClickListener {
            Toast.makeText(requireContext(), "Type filter clicked", Toast.LENGTH_SHORT).show()
            // TODO: Implement type filter
        }
    }

    private fun loadTransactions() {
        val token = PreferencesHelper.getToken(requireContext())
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token not found", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance
                val response = apiService.getTransactions("Bearer $token")

                if (response.isSuccessful) {
                    val transactions = response.body()
                    if (transactions != null && transactions.isNotEmpty()) {
                        val adapter = TransactionAdapter(transactions)
                        transactionsRecyclerView.adapter = adapter
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "No transactions found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load transactions",
                        Toast.LENGTH_SHORT
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

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }
}