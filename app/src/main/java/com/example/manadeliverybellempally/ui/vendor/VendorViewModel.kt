package com.example.manadeliverybellempally.ui.vendor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manadeliverybellempally.data.model.Order
import com.example.manadeliverybellempally.data.model.Product
import com.example.manadeliverybellempally.data.model.Review
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

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentVendorId: String = ""

    fun initialize(vendorId: String) {
        if (currentVendorId == vendorId && _vendor.value != null) return
        currentVendorId = vendorId

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // BUG-01 FIX: Load REAL vendor data from Firestore instead of hardcoded test data
                launch {
                    repository.getVendorFlow(vendorId).collect {
                        _vendor.value = it
                    }
                }
                // BUG-03 FIX: Load products for this vendor
                launch {
                    repository.getProductsForVendorFlow(vendorId).collect {
                        _products.value = it
                    }
                }
                launch {
                    repository.getVendorOrdersFlow(vendorId).collect {
                        _orders.value = it
                    }
                }
                launch {
                    repository.getVendorReviewsFlow(vendorId).collect {
                        _reviews.value = it
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            val result = repository.updateOrderStatus(orderId, "PREPARING")
            if (result.isFailure) _error.value = "Failed to accept order"
        }
    }

    // BUG-07 FIX: Use rejectOrder() which records the rejection reason properly
    fun rejectOrder(orderId: String, reason: String = "Vendor rejected") {
        viewModelScope.launch {
            val result = repository.rejectOrder(orderId, reason)
            if (result.isFailure) _error.value = "Failed to reject order"
        }
    }

    // BUG-08 FIX: Use "READY" to match getAvailableDeliveriesFlow() query
    fun markOrderReady(orderId: String) {
        viewModelScope.launch {
            val result = repository.updateOrderStatus(orderId, "READY")
            if (result.isFailure) _error.value = "Failed to mark order ready"
        }
    }

    fun markReadyForPickup(orderId: String) {
        markOrderReady(orderId)
    }

    fun loadOrders(vendorId: String) {
        // Orders are loaded via real-time flow in initialize()
    }

    // BUG-04 FIX: Persist store open/close to Firestore
    fun toggleStoreOpen(isOpen: Boolean) {
        val vendorId = _vendor.value?.id ?: return
        viewModelScope.launch {
            val result = repository.updateVendorOpenStatus(vendorId, isOpen)
            if (result.isFailure) {
                _error.value = "Failed to update store status"
            }
            // Firestore flow will automatically update _vendor state
        }
    }

    // BUG-05 FIX: Persist product availability to Firestore
    fun toggleProductAvailability(productId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            val result = repository.updateProductAvailability(productId, isAvailable)
            if (result.isFailure) {
                _error.value = "Failed to update product availability"
            }
        }
    }

    // BUG-11 FIX: Persist vendor profile updates to Firestore
    fun updateVendorProfile(vendor: Vendor) {
        viewModelScope.launch {
            val result = repository.updateVendor(vendor)
            if (result.isFailure) {
                _error.value = "Failed to update profile"
            }
            // Firestore flow will automatically update _vendor state
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            val result = repository.addProduct(product.copy(vendorId = currentVendorId))
            if (result.isFailure) _error.value = "Failed to add product"
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            val result = repository.deleteProduct(productId)
            if (result.isFailure) _error.value = "Failed to delete product"
        }
    }

    fun clearError() {
        _error.value = null
    }
}

