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

    private val _activePromos = MutableStateFlow<List<Coupon>>(emptyList())
    val activePromos: StateFlow<List<Coupon>> = _activePromos

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners: StateFlow<List<Banner>> = _banners

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Coupon State
    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon

    private val _couponError = MutableStateFlow<String?>(null)
    val couponError: StateFlow<String?> = _couponError

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
                launch { repository.getBannersFlow().collect { _banners.value = it } }
                launch { repository.getVendorsFlow().collect { _vendors.value = it } }
                launch { repository.getProductsFlow().collect { _products.value = it } }
                launch { repository.getOrdersForCustomerFlow(customerId).collect { _orders.value = it } }
                launch { repository.getUserFlow(customerId).collect { _currentUser.value = it } }
                launch { repository.getActiveCouponsFlow().collect { _activePromos.value = it } }
                
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

    // Cart conflict state — observed by UI to show "Replace cart?" dialog
    private val _cartConflict = MutableStateFlow<Product?>(null)
    val cartConflict: StateFlow<Product?> = _cartConflict

    fun addToCart(product: Product) {
        val currentVendor = _cartVendorId.value
        if (currentVendor != null && currentVendor != product.vendorId) {
            // Don't silently ignore — tell the UI there's a conflict
            _cartConflict.value = product
            return
        }
        _cartVendorId.value = product.vendorId
        val currentQty = _cart.value[product.id] ?: 0
        _cart.value = _cart.value + (product.id to currentQty + 1)
    }

    /** User chose "Yes, replace cart" in the conflict dialog */
    fun replaceCartWith(product: Product) {
        _cart.value = emptyMap()
        _cartVendorId.value = product.vendorId
        _cart.value = mapOf(product.id to 1)
        _cartConflict.value = null
    }

    /** User chose "No, keep current cart" in the conflict dialog */
    fun dismissCartConflict() {
        _cartConflict.value = null
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

    fun getCartTotal(): Double = getCartSubtotal() + getDeliveryFee() - getDiscountAmount()

    fun getDiscountAmount(): Double {
        val coupon = _appliedCoupon.value ?: return 0.0
        return coupon.calculateDiscount(getCartSubtotal())
    }

    fun applyCoupon(code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _couponError.value = null
            
            val result = repository.getCouponByCode(code)
            val coupon = result.getOrNull()
            
            if (coupon == null) {
                _couponError.value = "Invalid coupon code."
            } else if (!coupon.isValid(getCartSubtotal())) {
                _couponError.value = "Coupon requirements not met (e.g., min order amount)."
            } else {
                _appliedCoupon.value = coupon
            }
            _isLoading.value = false
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
        _couponError.value = null
    }

    // Order placement result — observed by UI for success/error feedback
    private val _orderError = MutableStateFlow<String?>(null)
    val orderError: StateFlow<String?> = _orderError

    private val _orderSuccess = MutableStateFlow(false)
    val orderSuccess: StateFlow<Boolean> = _orderSuccess

    fun clearOrderState() {
        _orderError.value = null
        _orderSuccess.value = false
    }

    fun placeOrder(paymentMethod: String, address: String) {
        // ── Input Validation ──
        if (_cart.value.isEmpty()) {
            _orderError.value = "Cart is empty. Add items first."
            return
        }
        val vendorId = _cartVendorId.value
        if (vendorId.isNullOrEmpty()) {
            _orderError.value = "No vendor selected."
            return
        }
        val addressObj = _currentUser.value?.addresses?.find { it.fullAddress == address } ?: 
                         _currentUser.value?.savedAddresses?.find { it.address == address }?.let {
                             Address(fullAddress = it.address, lat = it.lat, lng = it.lng)
                         } ?: Address(fullAddress = address)
                         
        if (address.isBlank()) {
            _orderError.value = "Please provide a delivery address."
            return
        }
        if (address.length < 10) {
            _orderError.value = "Address too short. Please add landmark & area."
            return
        }
        val vendor = _vendors.value.find { it.id == vendorId }
        if (vendor != null && vendor.minimumOrder > 0 && getCartSubtotal() < vendor.minimumOrder) {
            _orderError.value = "Minimum order is ₹${vendor.minimumOrder.toInt()}. Add more items."
            return
        }
        if (vendor != null && !vendor.isStoreOpen) {
            _orderError.value = "${vendor.storeName} is currently closed."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _orderError.value = null

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
                discount = getDiscountAmount(),
                couponCode = _appliedCoupon.value?.code ?: "",
                total = getCartTotal(),
                paymentMethod = paymentMethod,
                deliveryAddress = address,
                deliveryAddressObj = addressObj,
                status = "PLACED",
                createdAt = System.currentTimeMillis(),
                statusTimeline = mapOf("PLACED" to System.currentTimeMillis()),
                deliveryOtp = (1000..9999).random().toString(),
                estimatedDeliveryMinutes = vendor?.deliveryTimeMinutes ?: 30
            )
            val result = repository.createOrder(order)
            if (result.isSuccess) {
                clearCart()
                _orderSuccess.value = true
                _appliedCoupon.value = null
            } else {
                _orderError.value = result.exceptionOrNull()?.message ?: "Order failed. Please try again."
            }
            _isLoading.value = false
        }
    }

    // ═══════════════════════════════════════════
    // ORDER CANCELLATION
    // ═══════════════════════════════════════════
    fun cancelOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.cancelOrder(orderId, currentCustomerId, reason)
            if (result.isFailure) {
                _orderError.value = result.exceptionOrNull()?.message ?: "Failed to cancel order."
            }
            _isLoading.value = false
        }
    }

    // ═══════════════════════════════════════════
    // REVIEWS
    // ═══════════════════════════════════════════
    fun submitReview(orderId: String, vendorId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val review = Review(
                orderId = orderId,
                userId = currentCustomerId,
                userName = _currentUser.value?.name ?: "Customer",
                vendorId = vendorId,
                rating = rating,
                comment = comment
            )
            val result = repository.submitReview(review)
            if (result.isFailure) {
                _orderError.value = "Failed to submit review."
            }
            _isLoading.value = false
        }
    }

    // ═══════════════════════════════════════════
    // REORDER
    // ═══════════════════════════════════════════
    fun reorder(orderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getOrderById(orderId)
            val order = result.getOrNull()
            if (order != null) {
                // Clear existing cart and replace with past order items
                _cart.value = emptyMap()
                _cartVendorId.value = order.vendorId
                
                val newCart = mutableMapOf<String, Int>()
                order.items.forEach { item ->
                    // Only add if product still exists
                    if (_products.value.any { it.id == item.productId }) {
                        newCart[item.productId] = item.qty
                    }
                }
                
                if (newCart.isNotEmpty()) {
                    _cart.value = newCart
                } else {
                    _orderError.value = "Items are no longer available."
                }
            } else {
                _orderError.value = "Failed to load order details."
            }
            _isLoading.value = false
        }
    }

    // ═══════════════════════════════════════════
    // PROFILE PHOTO
    // ═══════════════════════════════════════════
    fun uploadProfilePhoto(uri: android.net.Uri) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.uploadProfileImage(user.id, uri)
            if (result.isFailure) {
                _orderError.value = "Failed to upload photo."
            }
            _isLoading.value = false
        }
    }

    // ═══════════════════════════════════════════
    // PHASE 8: SUPPORT TICKETS
    // ═══════════════════════════════════════════
    fun createSupportTicket(orderId: String, issueType: String, description: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val ticket = SupportTicket(
                userId = currentCustomerId,
                userName = _currentUser.value?.name ?: "Customer",
                orderId = orderId,
                issueType = issueType,
                description = description
            )
            val result = repository.createTicket(ticket)
            if (result.isFailure) {
                _orderError.value = "Failed to submit support ticket."
            }
            _isLoading.value = false
        }
    }
}
