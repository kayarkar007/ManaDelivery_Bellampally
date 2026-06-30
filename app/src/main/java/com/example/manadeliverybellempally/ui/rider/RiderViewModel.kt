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

    fun initialize(riderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _rider.value = User(id = riderId, role = "rider", name = "Test Rider", isOnline = true, vehicleNumber = "TS 00 0000")
            
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
            _isLoading.value = false
        }
    }

    fun toggleDuty(isOnline: Boolean) {
        _rider.value = _rider.value?.copy(isOnline = isOnline)
    }

    fun acceptOrder(orderId: String) {
        val r = _rider.value ?: return
        viewModelScope.launch {
            repository.assignRiderToOrder(orderId, r.id, r.name, r.phone)
        }
    }

    fun updateStatus(orderId: String, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun updateVehicle(vehicleType: String, vehicleNumber: String, vehicleModel: String) {
        val curr = _rider.value ?: return
        _rider.value = curr.copy(
            vehicleType = vehicleType,
            vehicleNumber = vehicleNumber,
            vehicleModel = vehicleModel
        )
    }

    fun pingLocation(orderId: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.updateOrderRiderLocation(orderId, lat, lng)
        }
    }
}
