package com.example.manadeliverybellempally.ui.vendor

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manadeliverybellempally.data.model.*
import com.example.manadeliverybellempally.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VendorViewModel(private val repository: FirestoreRepository = FirestoreRepository()) : ViewModel() {

    private val _vendor = MutableStateFlow<Vendor?>(null)
    val vendor: StateFlow<Vendor?> = _vendor.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _payouts = MutableStateFlow<List<Payout>>(emptyList())
    val payouts: StateFlow<List<Payout>> = _payouts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private var currentVendorId = ""

    fun initialize(vendorId: String) {
        if (currentVendorId == vendorId) return
        currentVendorId = vendorId
        
        viewModelScope.launch {
            repository.getVendorFlow(vendorId).collect { _vendor.value = it }
        }
        
        viewModelScope.launch {
            repository.getProductsForVendorFlow(vendorId).collect { _products.value = it }
        }
        
        viewModelScope.launch {
            repository.getCategoriesFlow().collect { _categories.value = it }
        }
        
        viewModelScope.launch {
            repository.getOrdersForVendorFlow(vendorId).collect { _orders.value = it }
        }

        viewModelScope.launch {
            repository.getPayoutsFlow(vendorId).collect { _payouts.value = it }
        }
    }

    fun toggleStoreOpen(isOpen: Boolean) {
        viewModelScope.launch {
            repository.updateVendorOpenStatus(currentVendorId, isOpen)
        }
    }

    fun toggleBusyMode(isBusy: Boolean) {
        viewModelScope.launch {
            val current = _vendor.value ?: return@launch
            repository.updateVendor(current.copy(isBusy = isBusy))
        }
    }

    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "ACCEPTED")
        }
    }

    fun markOrderPreparing(orderId: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "PREPARING")
        }
    }

    fun markOrderReady(orderId: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "READY")
        }
    }

    fun rejectOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            repository.rejectOrder(orderId, reason)
        }
    }

    fun saveProduct(product: Product, imageUrl: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            val finalProduct = product.copy(vendorId = currentVendorId, imageUrl = imageUrl.ifEmpty { product.imageUrl })
            val result = repository.addProduct(finalProduct)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to save product"
            }
            _isLoading.value = false
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    fun toggleProductAvailability(productId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            repository.updateProductAvailability(productId, isAvailable)
        }
    }

    fun updateVendorProfile(updatedVendor: Vendor) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.updateVendor(updatedVendor)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to update profile"
            }
            _isLoading.value = false
        }
    }

    fun clearError() { _errorMessage.value = null }
}
