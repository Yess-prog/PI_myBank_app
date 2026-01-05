
package com.example.bank_app.ui.cards

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

class CardsFragment : Fragment() {

    private lateinit var cardsRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_cards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load cards
        loadCards(view)

        // Setup add card button
        view.findViewById<View>(R.id.addCardButton)?.setOnClickListener {
            Toast.makeText(requireContext(), "Add new card clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to add card screen
        }
    }

    private fun loadCards(view: View) {
        val token = PreferencesHelper.getToken(requireContext())
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token not found", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance
                val response = apiService.getCards("Bearer $token")

                if (response.isSuccessful) {
                    val cards = response.body()
                    if (cards != null && cards.isNotEmpty()) {
                        // Display first card details
                        val firstCard = cards[0]
                        displayCardDetails(view, firstCard)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "No cards found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load cards",
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

    private fun displayCardDetails(view: View, card: com.example.bank_app.api.Card) {
        view.apply {
            // Set card type
            findViewById<android.widget.TextView>(R.id.cardType)?.text = card.cardType

            // Set card number (masked)
            findViewById<android.widget.TextView>(R.id.cardNumber)?.text =
                "**** **** **** ${card.cardLast4}"

            // Set card holder name
            findViewById<android.widget.TextView>(R.id.cardHolder)?.text =
                card.cardHolderName.uppercase()

            // Set expiry date
            val expiryText = String.format("%02d/%02d", card.expiryMonth, card.expiryYear % 100)
            findViewById<android.widget.TextView>(R.id.expiryDate)?.text = expiryText
        }
    }

    override fun onResume() {
        super.onResume()
        view?.let { loadCards(it) }
    }
}