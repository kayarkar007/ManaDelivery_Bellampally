package com.example.manadeliverybellempally.ui.rider

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manadeliverybellempally.data.model.*
import com.example.manadeliverybellempally.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RiderViewModel(private val repository: FirestoreRepository = FirestoreRepository()) : ViewModel() {

    private val _rider = MutableStateFlow<User?>(null)
    val rider: StateFlow<User?> = _rider.asStateFlow()

    private val _availableOrders = MutableStateFlow<List<Order>>(emptyList())
    val availableOrders: StateFlow<List<Order>> = _availableOrders.asStateFlow()

    private val _myOrders = MutableStateFlow<List<Order>>(emptyList())
    val myOrders: StateFlow<List<Order>> = _myOrders.asStateFlow()

    private val _payouts = MutableStateFlow<List<Payout>>(emptyList())
    val payouts: StateFlow<List<Payout>> = _payouts.asStateFlow()

    private val _tickets = MutableStateFlow<List<SupportTicket>>(emptyList())
    val tickets: StateFlow<List<SupportTicket>> = _tickets.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentRiderId = ""

    fun initialize(riderId: String) {
        if (currentRiderId == riderId) return
        currentRiderId = riderId

        viewModelScope.launch {
            repository.getUserFlow(riderId).collect { _rider.value = it }
        }

        viewModelScope.launch {
            repository.getAvailableDeliveriesFlow().collect { _availableOrders.value = it }
        }

        viewModelScope.launch {
            repository.getOrdersForRiderFlow(riderId).collect { _myOrders.value = it }
        }

        viewModelScope.launch {
            repository.getPayoutsFlow(riderId).collect { _payouts.value = it }
        }

        viewModelScope.launch {
            repository.getTicketsFlow(riderId).collect { _tickets.value = it }
        }
    }

    // ─── DUTY & COMPLIANCE ───
    fun toggleDuty(isOnline: Boolean) {
        viewModelScope.launch {
            val riderObj = _rider.value
            if (isOnline && riderObj?.approvalStatus != "APPROVED") {
                _errorMessage.value = "Documents not approved yet. Access restricted."
                return@launch
            }
            repository.updateRiderOnlineStatus(currentRiderId, isOnline)
        }
    }

    fun uploadKYC(docType: String, url: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateRiderKYC(currentRiderId, docType, url)
            _isLoading.value = false
        }
    }

    fun updateVehicle(type: String, number: String, model: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateRiderVehicle(currentRiderId, type, number, model)
            _isLoading.value = false
        }
    }

    // ─── DELIVERY FLOW ───
    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val riderObj = _rider.value ?: return@launch
            val result = repository.assignRiderToOrder(orderId, currentRiderId, riderObj.name, "")
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Order already taken or unavailable"
            }
            _isLoading.value = false
        }
    }

    fun updateStatus(orderId: String, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
        }
    }

    fun confirmCOD(orderId: String, amount: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.confirmCODCollection(orderId, currentRiderId, amount)
            _isLoading.value = false
        }
    }

    // ─── SUPPORT ───
    fun createTicket(subject: String, description: String, orderId: String = "") {
        viewModelScope.launch {
            val ticket = SupportTicket(
                userId = currentRiderId,
                subject = subject,
                description = description + (if (orderId.isNotEmpty()) "\nLinked Order: #$orderId" else "")
            )
            repository.createTicket(ticket)
        }
    }

    fun clearError() { _errorMessage.value = null }
}
