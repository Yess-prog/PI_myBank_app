package com.example.bank_app.ui.home

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
import com.example.bank_app.utils.PreferencesHelper
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController

class HomeFragment : Fragment() {

    private lateinit var transactionsRecyclerView: RecyclerView
    private var isBalanceVisible = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        transactionsRecyclerView = view.findViewById(R.id.transactionsRecyclerView)
        transactionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Load user account data
        loadAccountData()

        // Load transactions
        loadTransactions()

        // Setup eye icon to toggle balance visibility
        view.findViewById<View>(R.id.eyeIcon)?.setOnClickListener {
            toggleBalanceVisibility(view)
        }

        // Setup quick action buttons
        setupQuickActions(view)
    }

    private fun loadAccountData() {
        val token = PreferencesHelper.getToken(requireContext())
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token not found", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance
                val response = apiService.getAccounts("Bearer $token")

                if (response.isSuccessful) {
                    val accounts = response.body()
                    if (accounts != null && accounts.isNotEmpty()) {
                        val account = accounts[0] // Get first account
                        updateBalanceUI(account.balance, account.currency)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load accounts",
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

    private fun updateBalanceUI(balance: Double, currency: String) {
        view?.apply {
            val balanceAmountView = findViewById<android.widget.TextView>(R.id.balanceAmount)
            val formattedBalance = String.format("%.2f", balance)

            if (isBalanceVisible) {
                balanceAmountView?.text = "$currency $formattedBalance"
            } else {
                balanceAmountView?.text = "$currency ****"
            }
        }
    }

    private fun toggleBalanceVisibility(view: View) {
        isBalanceVisible = !isBalanceVisible
        val eyeIcon = view.findViewById<android.widget.ImageView>(R.id.eyeIcon)
        val balanceAmountView = view.findViewById<android.widget.TextView>(R.id.balanceAmount)

        // You can update the icon here if needed
        // For now, just reload the data to show/hide balance
        loadAccountData()

        // Change eye icon appearance
        if (isBalanceVisible) {
            eyeIcon?.alpha = 1f
        } else {
            eyeIcon?.alpha = 0.5f
        }
    }

    private fun loadTransactions() {
        val token = PreferencesHelper.getToken(requireContext())
        if (token.isNullOrEmpty()) {
            return
        }

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance
                val response = apiService.getTransactions("Bearer $token")

                if (response.isSuccessful) {
                    val transactions = response.body()
                    if (transactions != null) {
                        val adapter = TransactionAdapter(transactions)
                        transactionsRecyclerView.adapter = adapter
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
                    "Error loading transactions: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupQuickActions(view: View) {

        // Request Money
        view.findViewById<View>(R.id.requestAction)?.setOnClickListener {
            findNavController().navigate(R.id.requestMoneyFragment)
        }
        // Send Money
        view.findViewById<View>(R.id.sendAction)?.setOnClickListener {
            findNavController().navigate(R.id.sendMoneyFragment)
        }



        // Replace topupAction with predictAction
        view.findViewById<View>(R.id.predictAction)?.setOnClickListener {
            findNavController().navigate(R.id.incomePredictionFragment)
        }

        // More
        view.findViewById<View>(R.id.moreAction)?.setOnClickListener {
            Toast.makeText(requireContext(), "More options clicked", Toast.LENGTH_SHORT).show()
            // TODO: Show more options menu
        }
    }

    // Refresh data when fragment is visible
    override fun onResume() {
        super.onResume()
        loadAccountData()
        loadTransactions()
    }
}