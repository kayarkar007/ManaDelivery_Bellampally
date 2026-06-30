package com.example.manadeliverybellempally.ui.rider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manadeliverybellempally.data.model.Order
import com.example.manadeliverybellempally.data.model.User
import com.example.manadeliverybellempally.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RiderViewModel(private val repository: FirestoreRepository = FirestoreRepository()) : ViewModel() {

    private val _rider = MutableStateFlow<User?>(null)
    val rider: StateFlow<User?> = _rider

    private val _myOrders = MutableStateFlow<List<Order>>(emptyList())
    val myOrders: StateFlow<List<Order>> = _myOrders

    // Phase 8: Dynamic Earnings (20 Rs Base + 5 Rs per km. Assuming 3km avg for now -> 35 Rs per delivery)
    val totalEarnings: StateFlow<Double> = _myOrders.map { orders ->
        orders.filter { it.status == "DELIVERED" }.size * 35.0
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val completedDeliveriesCount: StateFlow<Int> = _myOrders.map { orders ->
        orders.count { it.status == "DELIVERED" }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    
    private val _availableOrders = MutableStateFlow<List<Order>>(emptyList())
    val availableOrders: StateFlow<List<Order>> = _availableOrders

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentRiderId: String = ""

    fun initialize(riderId: String) {
        if (currentRiderId == riderId && _rider.value != null) return
        currentRiderId = riderId

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // BUG-02 FIX: Load REAL rider data from Firestore instead of hardcoded test data
                launch {
                    repository.getUserFlow(riderId).collect {
                        _rider.value = it
                    }
                }
                launch {
                    repository.getRiderOrdersFlow(riderId).collect {
                        _myOrders.value = it
                    }
                }
                launch {
                    repository.getAvailableDeliveriesFlow().collect {
                        _availableOrders.value = it
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // BUG-06 FIX: Persist duty toggle to Firestore
    fun toggleDuty(isOnline: Boolean) {
        viewModelScope.launch {
            val result = repository.updateRiderOnlineStatus(currentRiderId, isOnline)
            if (result.isFailure) {
                _error.value = "Failed to update duty status"
            }
            // Firestore flow will automatically update _rider state
        }
    }

    fun acceptOrder(orderId: String) {
        val r = _rider.value ?: return
        viewModelScope.launch {
            val result = repository.assignRiderToOrder(orderId, r.id, r.name, r.phone)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to accept order"
            }
        }
    }

    fun updateStatus(orderId: String, status: String) {
        viewModelScope.launch {
            val result = repository.updateOrderStatus(orderId, status)
            if (result.isFailure) {
                _error.value = "Failed to update order status"
            }
        }
    }

    // Phase 9: Real-time Location Updates
    fun updateLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            // Update rider's own location in their user document
            repository.updateRiderLocation(currentRiderId, lat, lng)
            // Update location for all active orders for this rider
            val activeOrders = _myOrders.value.filter { it.status == "OUT_FOR_DELIVERY" || it.status == "ACCEPTED" || it.status == "READY" }
            activeOrders.forEach { order ->
                repository.updateOrderRiderLocation(order.id, lat, lng)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun updateVehicle(vehicleType: String, vehicleNumber: String, vehicleModel: String) {
        viewModelScope.launch {
            val result = repository.updateRiderVehicle(currentRiderId, vehicleType, vehicleNumber, vehicleModel)
            if (result.isFailure) {
                _error.value = "Failed to update vehicle info"
            }
            // Firestore flow will automatically update _rider state
        }
    }

    fun pingLocation(orderId: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.updateOrderRiderLocation(orderId, lat, lng)
        }
    }
}

