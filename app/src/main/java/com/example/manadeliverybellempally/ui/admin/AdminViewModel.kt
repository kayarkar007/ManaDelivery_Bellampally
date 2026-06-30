package com.example.manadeliverybellempally.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manadeliverybellempally.data.model.*
import com.example.manadeliverybellempally.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class AdminViewModel(private val repository: FirestoreRepository = FirestoreRepository()) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _revenue = MutableStateFlow(0.0)
    val revenue: StateFlow<Double> = _revenue

    private val _orderCount = MutableStateFlow(0)
    val orderCount: StateFlow<Int> = _orderCount

    private val _vendorCount = MutableStateFlow(0)
    val vendorCount: StateFlow<Int> = _vendorCount

    private val _riderCount = MutableStateFlow(0)
    val riderCount: StateFlow<Int> = _riderCount

    private val _allOrders = MutableStateFlow<List<Order>>(emptyList())
    val allOrders: StateFlow<List<Order>> = _allOrders

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers

    private val _allVendors = MutableStateFlow<List<Vendor>>(emptyList())
    val allVendors: StateFlow<List<Vendor>> = _allVendors

    private val _coupons = MutableStateFlow<List<Coupon>>(emptyList())
    val coupons: StateFlow<List<Coupon>> = _coupons

    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners: StateFlow<List<Banner>> = _banners

    private val _tickets = MutableStateFlow<List<SupportTicket>>(emptyList())
    val tickets: StateFlow<List<SupportTicket>> = _tickets

    private val _settings = MutableStateFlow(AdminSettings())
    val settings: StateFlow<AdminSettings> = _settings

    private val _pendingVendorsCount = MutableStateFlow(0)
    val pendingVendorsCount: StateFlow<Int> = _pendingVendorsCount

    private val _activeRidersCount = MutableStateFlow(0)
    val activeRidersCount: StateFlow<Int> = _activeRidersCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun initialize() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                launch {
                    repository.getAllOrdersFlow().collect { orders ->
                        _allOrders.value = orders
                        _orderCount.value = orders.size
                        _revenue.value = orders.filter { it.status == "DELIVERED" }.sumOf { it.total }
                    }
                }
                // BUG-14 FIX: Single listener for users, computing ALL derived counts
                launch {
                    repository.getUsersFlow().collect { users ->
                        _allUsers.value = users
                        _riderCount.value = users.count { it.role == "RIDER" }
                        _activeRidersCount.value = users.count { it.role == "RIDER" && it.isOnline }
                        _pendingVendorsCount.value = users.count { it.role == "VENDOR" && it.approvalStatus == "PENDING" }
                    }
                }
                launch {
                    repository.getVendorsFlow().collect { vendors ->
                        _allVendors.value = vendors
                        _vendorCount.value = vendors.size
                    }
                }
                
                launch { repository.getCouponsFlow().collect { _coupons.value = it } }
                launch { repository.getBannersFlow().collect { _banners.value = it } }
                launch { repository.getTicketsFlow("admin_all").collect { _tickets.value = it } }
                launch { repository.getAdminSettingsFlow().collect { _settings.value = it } }
                
                delay(1000)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: String, internalNotes: String = "") {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
            if (internalNotes.isNotEmpty()) {
                // In real app, we'd update a specific audit or notes field
            }
            logAudit("ORDER_STATUS_CHANGE", orderId, "Status updated to $status")
        }
    }

    fun assignRider(orderId: String, riderId: String) {
        viewModelScope.launch {
            val rider = _allUsers.value.find { it.id == riderId }
            repository.assignRiderToOrder(orderId, riderId, rider?.name ?: "Rider", "")
        }
    }

    fun toggleUserBlock(user: User) {
        viewModelScope.launch {
            repository.updateUser(user.copy(isBlocked = !user.isBlocked))
        }
    }

    fun updateApproval(user: User, status: String) {
        viewModelScope.launch {
            repository.updateUser(user.copy(approvalStatus = status))
        }
    }

    fun saveCoupon(coupon: Coupon) {
        viewModelScope.launch {
            repository.saveCoupon(coupon)
        }
    }

    fun sendPromoNotification(title: String, message: String) {
        viewModelScope.launch {
            repository.sendMockPushNotification(title, message)
        }
    }

    fun saveBanner(banner: Banner) {
        viewModelScope.launch {
            repository.saveBanner(banner)
        }
    }

    fun updateSettings(settings: AdminSettings) {
        viewModelScope.launch {
            repository.updateAdminSettings(settings)
        }
    }

    fun resolveTicket(ticket: SupportTicket, internalNotes: String) {
        viewModelScope.launch {
            val updated = ticket.copy(status = "RESOLVED", internalNotes = internalNotes)
            repository.updateTicket(updated)
            logAudit("TICKET_RESOLVE", ticket.id, "Ticket resolved")
        }
    }

    private suspend fun logAudit(action: String, targetId: String, description: String) {
        val user = auth.currentUser
        val log = AuditLog(
            adminId = user?.uid ?: "system",
            action = action,
            targetId = targetId,
            description = description
        )
        repository.logAction(log)
    }
}
