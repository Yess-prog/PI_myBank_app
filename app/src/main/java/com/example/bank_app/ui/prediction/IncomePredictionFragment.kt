package com.example.bank_app.ui.prediction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bank_app.R
import com.example.bank_app.api.ApiService
import com.example.bank_app.api.RetrofitClient
import com.example.bank_app.utils.PreferencesHelper
import kotlinx.coroutines.launch

class IncomePredictionFragment : Fragment() {

    private lateinit var backButton: ImageView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var contentContainer: LinearLayout

    private lateinit var currentIncomeText: TextView
    private lateinit var transactionCountText: TextView

    private lateinit var prediction7DaysText: TextView
    private lateinit var prediction7DaysConfidence: TextView

    private lateinit var prediction14DaysText: TextView
    private lateinit var prediction14DaysConfidence: TextView

    private lateinit var prediction30DaysText: TextView
    private lateinit var prediction30DaysConfidence: TextView

    private lateinit var aiInsightsText: TextView
    private lateinit var refreshButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_income_prediction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        loadPrediction()
    }

    private fun initializeViews(view: View) {
        backButton = view.findViewById(R.id.backButton)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)
        contentContainer = view.findViewById(R.id.contentContainer)

        currentIncomeText = view.findViewById(R.id.currentIncomeText)
        transactionCountText = view.findViewById(R.id.transactionCountText)

        prediction7DaysText = view.findViewById(R.id.prediction7DaysText)
        prediction7DaysConfidence = view.findViewById(R.id.prediction7DaysConfidence)

        prediction14DaysText = view.findViewById(R.id.prediction14DaysText)
        prediction14DaysConfidence = view.findViewById(R.id.prediction14DaysConfidence)

        prediction30DaysText = view.findViewById(R.id.prediction30DaysText)
        prediction30DaysConfidence = view.findViewById(R.id.prediction30DaysConfidence)

        aiInsightsText = view.findViewById(R.id.aiInsightsText)
        refreshButton = view.findViewById(R.id.refreshButton)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        refreshButton.setOnClickListener {
            loadPrediction()
        }
    }

    private fun loadPrediction() {
        val token = PreferencesHelper.getToken(requireContext())
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please login again", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance
                val response = apiService.getIncomePrediction("Bearer $token")

                if (response.isSuccessful) {
                    val prediction = response.body()
                    if (prediction != null) {
                        displayPrediction(prediction)
                    } else {
                        showError("No prediction data available")
                    }
                } else {
                    showError("Failed to load prediction")
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun displayPrediction(prediction: ApiService.IncomePrediction) {
        // Current income
        currentIncomeText.text = "DT ${String.format("%.2f", prediction.currentIncome)}"
        transactionCountText.text = "Based on ${prediction.transactionCount} incoming transactions"

        // 7 days prediction
        prediction7DaysText.text = "DT ${String.format("%.2f", prediction.next7Days)}"
        prediction7DaysConfidence.text = "Confidence: ${prediction.confidence}%"

        // 14 days prediction
        prediction14DaysText.text = "DT ${String.format("%.2f", prediction.next14Days)}"
        prediction14DaysConfidence.text = "Confidence: ${prediction.confidence}%"

        // 30 days prediction
        prediction30DaysText.text = "DT ${String.format("%.2f", prediction.next30Days)}"
        prediction30DaysConfidence.text = "Confidence: ${prediction.confidence}%"

        // AI Insights
        aiInsightsText.text = generateInsights(prediction)
    }

    private fun generateInsights(prediction: ApiService.IncomePrediction): String {
        val insights = mutableListOf<String>()

        // Pattern detection
        when (prediction.pattern) {
            "stable" -> insights.add("• Your income pattern is stable and predictable")
            "increasing" -> insights.add("• Your income is trending upward 📈")
            "decreasing" -> insights.add("• Your income is trending downward 📉")
            "irregular" -> insights.add("• Your income pattern is irregular")
        }

        // Confidence feedback
        val confidenceValue = prediction.confidence
        when {
            confidenceValue >= 80 -> insights.add("• High confidence prediction based on consistent data")
            confidenceValue >= 60 -> insights.add("• Moderate confidence prediction")
            else -> insights.add("• Low confidence - more data needed for accurate prediction")
        }

        // Growth analysis
        val currentIncome = prediction.currentIncome
        val futureIncome = prediction.next30Days

        if (currentIncome > 0) {
            val growth30Days = ((futureIncome - currentIncome) / currentIncome * 100)
            when {
                growth30Days > 10 -> {
                    insights.add("• Expected ${String.format("%.1f", growth30Days)}% growth in next 30 days")
                }
                growth30Days < -10 -> {
                    insights.add("• Expected ${String.format("%.1f", kotlin.math.abs(growth30Days))}% decrease in next 30 days")
                }
            }
        }

        return insights.joinToString("\n")
    }

    private fun showLoading(show: Boolean) {
        loadingIndicator.visibility = if (show) View.VISIBLE else View.GONE
        contentContainer.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}