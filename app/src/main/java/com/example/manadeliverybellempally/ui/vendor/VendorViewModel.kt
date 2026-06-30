package com.example.manadeliverybellempally.ui.vendor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manadeliverybellempally.data.model.Order
import com.example.manadeliverybellempally.data.model.Product
import com.example.manadeliverybellempally.data.model.Vendor
import com.example.manadeliverybellempally.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VendorViewModel(private val repository: FirestoreRepository = FirestoreRepository()) : ViewModel() {

    private val _vendor = MutableStateFlow<Vendor?>(null)
    val vendor: StateFlow<Vendor?> = _vendor

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun initialize(vendorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _vendor.value = Vendor(id = vendorId, storeName = "Test Vendor Store", isStoreOpen = true)
            
            launch {
                repository.getVendorOrdersFlow(vendorId).collect {
                    _orders.value = it
                }
            }
            _isLoading.value = false
        }
    }

    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "PREPARING")
        }
    }

    fun rejectOrder(orderId: String, reason: String = "") {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "CANCELLED")
        }
    }

    fun markOrderReady(orderId: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "READY_FOR_PICKUP")
        }
    }
    
    fun markReadyForPickup(orderId: String) {
        markOrderReady(orderId)
    }
    
    fun loadOrders(vendorId: String) {
        // dummy for compile
    }

    fun toggleStoreOpen(isOpen: Boolean) {
        _vendor.value = _vendor.value?.copy(isStoreOpen = isOpen)
    }

    fun toggleProductAvailability(productId: String, isAvailable: Boolean) {
        // Not implemented yet in repository
    }

    fun updateVendorProfile(user: Vendor) {
        _vendor.value = user
    }

    fun clearError() {
        _error.value = null
    }
}
