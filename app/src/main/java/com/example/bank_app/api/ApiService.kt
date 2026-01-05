package com.example.bank_app.api

import com.example.bank_app.models.LoginRequest
import com.example.bank_app.models.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Auth endpoints
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<LoginResponse>


    // User endpoints
    @GET("users/profile")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<User>

    // Account endpoints
    @GET("accounts")
    suspend fun getAccounts(@Header("Authorization") token: String): Response<List<Account>>

    @GET("accounts/{accountId}")
    suspend fun getAccountDetails(
        @Path("accountId") accountId: Int,
        @Header("Authorization") token: String
    ): Response<Account>

    // Card endpoints
    @GET("cards")
    suspend fun getCards(@Header("Authorization") token: String): Response<List<Card>>

    // Transaction endpoints
    @GET("transactions")
    suspend fun getTransactions(@Header("Authorization") token: String): Response<List<Transaction>>

    @POST("transactions/transfer")
    suspend fun transfer(
        @Header("Authorization") token: String,
        @Body transferRequest: TransferRequest
    ): Response<TransferResponse>

    @GET("notifications/transfer-requests")
    suspend fun getTransferRequests(
        @Header("Authorization") token: String
    ): Response<List<TransferRequestItem>>

    @POST("notifications/transfer-requests/{requestId}/accept")
    suspend fun acceptTransferRequest(
        @Header("Authorization") token: String,
        @Path("requestId") requestId: Int
    ): Response<AcceptResponse>

    @POST("notifications/transfer-requests/{requestId}/reject")
    suspend fun rejectTransferRequest(
        @Header("Authorization") token: String,
        @Path("requestId") requestId: Int
    ): Response<RejectResponse>
    @GET("users/email/{email}")
    suspend fun getUserByEmail(
        @Header("Authorization") token: String,
        @Path("email") email: String
    ): Response<User>

    @GET("accounts/user/{userId}")
    suspend fun getAccountsByUserId(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Response<List<Account>>

    @POST("transactions/request")
    suspend fun createTransferRequest(
        @Header("Authorization") token: String,
        @Body transferRequestBody: TransferRequestBody
    ): Response<RequestResponse>

    // Add this data class
    data class IncomePrediction(
        val currentIncome: Double,
        val transactionCount: Int,
        val next7Days: Double,
        val next14Days: Double,
        val next30Days: Double,
        val confidence: Int,
        val pattern: String, // "stable", "increasing", "decreasing", "irregular"
        val averageMonthlyIncome: Double
    )

    // Add this endpoint to ApiService interface
    // Prediction endpoint
    @GET("predictions/income")
    suspend fun getIncomePrediction(
        @Header("Authorization") token: String
    ): Response<IncomePrediction>




}
// ============ PREDICTION DATA CLASSES ============
data class IncomePrediction(
    val currentIncome: Double,
    val transactionCount: Int,
    val next7Days: Double,
    val next14Days: Double,
    val next30Days: Double,
    val confidence: Int,
    val pattern: String,
    val averageMonthlyIncome: Double
)

// ============ REQUEST DATA CLASSES ============
data class TransferRequestBody(
    val toUserId: Int,
    val fromAccountId: Int,
    val toAccountId: Int,
    val amount: Double,
    val description: String
)


data class RequestResponse(
    val message: String,
    val requestId: Int
)

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?
)

// Update the existing TransferRequest to be for sending
data class TransferRequest(
    val fromAccountId: Int,
    val toRib: String,
    val amount: Double,
    val description: String
)

// Add this NEW data class for transfer requests (receiving)
data class TransferRequestItem(
    val id: Int,
    val senderName: String,
    val senderEmail: String,
    val amount: Double,
    val description: String,
    val createdAt: String
)

data class TransferResponse(
    val message: String,
    val transactionId: Int,
    val amount: Double,
    val toRib: String
)

data class Account(
    val id: Int,
    val userId: Int,
    val type: String,
    val balance: Double,
    val currency: String,
    val rib: String
)

data class Card(
    val id: Int,
    val cardHolderName: String,
    val cardLast4: String,
    val cardMask: String,
    val cardType: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val status: String
)

data class Transaction(
    val id: Int,
    val fromAccountId: Int,
    val toAccountId: Int,
    val amount: Double,
    val description: String?,
    val createdAt: String
)

data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val status: String,
    val createdAt: String
)

data class AcceptResponse(
    val message: String,
    val amount: Double
)

data class RejectResponse(
    val message: String
)