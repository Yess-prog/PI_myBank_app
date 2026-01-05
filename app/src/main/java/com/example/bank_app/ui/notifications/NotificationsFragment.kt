package com.example.bank_app.ui.notifications

import android.os.Bundle
import androidx.navigation.fragment.findNavController
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

class NotificationsFragment : Fragment() {

    private lateinit var requestsRecyclerView: RecyclerView
    private lateinit var adapter: TransferRequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        requestsRecyclerView = view.findViewById(R.id.requestsRecyclerView)
        requestsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Back button
        view.findViewById<android.widget.ImageView>(R.id.backButton)?.setOnClickListener {
            findNavController().popBackStack()
        }

        // Load transfer requests
        loadTransferRequests()
    }

    private fun loadTransferRequests() {
        val token = PreferencesHelper.getToken(requireContext())
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token not found", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance
                val response = apiService.getTransferRequests("Bearer $token")

                if (response.isSuccessful) {
                    val requests = response.body()
                    if (requests != null && requests.isNotEmpty()) {
                        adapter = TransferRequestAdapter(
                            requests.toMutableList(),
                            ::onAcceptRequest,
                            ::onRejectRequest,
                            requireContext()
                        )
                        requestsRecyclerView.adapter = adapter
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "No transfer requests",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load requests",
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

    private fun onAcceptRequest(request: com.example.bank_app.api.TransferRequestItem, position: Int) {
        // This will be handled by the adapter
        adapter.notifyItemRemoved(position)
        loadTransferRequests() // Refresh list
    }

    private fun onRejectRequest(request: com.example.bank_app.api.TransferRequestItem, position: Int) {
        // This will be handled by the adapter
        adapter.notifyItemRemoved(position)
        loadTransferRequests() // Refresh list
    }

    override fun onResume() {
        super.onResume()
        loadTransferRequests()
    }
}
