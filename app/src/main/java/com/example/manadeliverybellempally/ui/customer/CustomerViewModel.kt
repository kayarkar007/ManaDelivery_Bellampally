package com.example.manadeliverybellempally.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manadeliverybellempally.data.model.*
import com.example.manadeliverybellempally.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class CustomerViewModel(private val repository: FirestoreRepository = FirestoreRepository()) : ViewModel() {

    private val _vendors = MutableStateFlow<List<Vendor>>(emptyList())
    val vendors: StateFlow<List<Vendor>> = _vendors

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _cart = MutableStateFlow<Map<String, Int>>(emptyMap())
    val cart: StateFlow<Map<String, Int>> = _cart

    private val _cartVendorId = MutableStateFlow<String?>(null)
    val cartVendorId: StateFlow<String?> = _cartVendorId

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var currentCustomerId: String = ""

    fun saveAddress(address: Address) {
        viewModelScope.launch {
            repository.saveAddress(currentCustomerId, address)
        }
    }

    fun removeAddress(addressId: String) {
        viewModelScope.launch {
            repository.deleteAddress(currentCustomerId, addressId)
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredProducts = combine(_products, _searchQuery) { products, query ->
        if (query.isBlank()) emptyList()
        else products.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun initialize(customerId: String) {
        if (currentCustomerId == customerId && _vendors.value.isNotEmpty()) return
        currentCustomerId = customerId
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Parallel fetching
                launch { repository.getCategoriesFlow().collect { _categories.value = it } }
                launch { repository.getVendorsFlow().collect { _vendors.value = it } }
                launch { repository.getProductsFlow().collect { _products.value = it } }
                launch { repository.getOrdersForCustomerFlow(customerId).collect { _orders.value = it } }
                launch { repository.getUserFlow(customerId).collect { _currentUser.value = it } }
                
                delay(800) // Small delay for smooth shimmer transition
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun vendorsForCategory(categoryId: String): List<Vendor> {
        return _vendors.value.filter { it.categoryId == categoryId }
    }

    fun productsForCategory(categoryId: String): List<Product> {
        val vendorIds = vendorsForCategory(categoryId).map { it.id }
        return _products.value.filter { it.vendorId in vendorIds }
    }

    private val _selectedVendorProducts = MutableStateFlow<List<Product>>(emptyList())
    val selectedVendorProducts: StateFlow<List<Product>> = _selectedVendorProducts

    fun loadProductsForVendor(vendorId: String) {
        _selectedVendorProducts.value = _products.value.filter { it.vendorId == vendorId }
    }

    fun addToCart(product: Product) {
        val currentVendor = _cartVendorId.value
        if (currentVendor != null && currentVendor != product.vendorId) {
            return
        }
        _cartVendorId.value = product.vendorId
        val currentQty = _cart.value[product.id] ?: 0
        _cart.value = _cart.value + (product.id to currentQty + 1)
    }

    fun removeFromCart(product: Product) {
        val currentQty = _cart.value[product.id] ?: 0
        if (currentQty <= 1) {
            _cart.value = _cart.value - product.id
            if (_cart.value.isEmpty()) {
                _cartVendorId.value = null
            }
        } else {
            _cart.value = _cart.value + (product.id to currentQty - 1)
        }
    }

    fun clearCart() {
        _cart.value = emptyMap()
        _cartVendorId.value = null
    }

    fun getCartSubtotal(): Double {
        return _cart.value.entries.sumOf { entry ->
            val product = _products.value.find { it.id == entry.key }
            (product?.price ?: 0.0) * entry.value
        }
    }

    fun getDeliveryFee(): Double = 25.0

    fun getCartTotal(): Double = getCartSubtotal() + getDeliveryFee()

    fun placeOrder(paymentMethod: String, address: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val vendorId = _cartVendorId.value ?: ""
            val vendor = _vendors.value.find { it.id == vendorId }
            val orderItems = _cart.value.map { entry ->
                val p = _products.value.find { it.id == entry.key } ?: Product()
                OrderItem(productId = p.id, name = p.name, price = p.price, qty = entry.value)
            }
            val order = Order(
                userId = currentCustomerId,
                userName = _currentUser.value?.name ?: "Customer",
                userPhone = _currentUser.value?.phone ?: "",
                vendorId = vendorId,
                vendorName = vendor?.storeName ?: "",
                items = orderItems,
                subtotal = getCartSubtotal(),
                deliveryFee = getDeliveryFee(),
                total = getCartTotal(),
                paymentMethod = paymentMethod,
                deliveryAddress = address,
                deliveryAddressObj = Address(fullAddress = address),
                status = "PLACED",
                createdAt = System.currentTimeMillis(),
                statusTimeline = mapOf("PLACED" to System.currentTimeMillis()),
                deliveryOtp = (1000..9999).random().toString()
            )
            val result = repository.createOrder(order)
            if (result.isSuccess) clearCart()
            _isLoading.value = false
        }
    }
}
