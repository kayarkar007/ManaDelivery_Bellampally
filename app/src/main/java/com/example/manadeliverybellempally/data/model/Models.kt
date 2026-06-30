package com.example.manadeliverybellempally.data.model

import com.google.firebase.firestore.PropertyName

// ═══════════════════════════════════════════
// User Roles
// ═══════════════════════════════════════════
enum class UserRole {
    CUSTOMER, VENDOR, RIDER, ADMIN
}

// ═══════════════════════════════════════════
// User Model
// ═══════════════════════════════════════════
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val whatsapp: String = "",
    val phone: String = "",
    val role: String = "user",
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true,
    @get:PropertyName("isBlocked") @set:PropertyName("isBlocked") var isBlocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis(),
    val fcmToken: String = "",
    val profileImageUrl: String = "",
    val savedAddresses: List<SavedAddress> = emptyList(),
    val addresses: List<Address> = emptyList(), // Added for backwards compatibility
    val walletBalance: Double = 0.0,
    val approvalStatus: String = "APPROVED",
    @get:PropertyName("isOnline") @set:PropertyName("isOnline") var isOnline: Boolean = false,
    val dutyStatus: String = "offline",
    val currentLat: Double = 0.0,
    val currentLng: Double = 0.0,
    val vehicleNumber: String = "",
    val vehicleType: String = "bike",
    val vehicleModel: String = "",
    val codBalance: Double = 0.0,
    val commissionRate: Double = 10.0,
    val kycStatus: Map<String, String> = emptyMap(),
    val kycDocuments: Map<String, String> = emptyMap(),
    // Legacy fields to fix compile errors
    val storeName: String = "",
    val storeAddress: String = "",
    @get:PropertyName("isStoreOpen") @set:PropertyName("isStoreOpen") var isStoreOpen: Boolean = true,
)

data class SavedAddress(
    val label: String = "Home",
    val address: String = "",
    val landmark: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
)

data class Address(
    val id: String = "",
    val label: String = "Home",
    val fullAddress: String = "",
    val landmark: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val isDefault: Boolean = false,
)

// ═══════════════════════════════════════════
// Order Model
// ═══════════════════════════════════════════
data class Order(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val riderId: String = "",
    val riderName: String = "",
    val vendorId: String = "",
    val vendorName: String = "",
    val items: List<OrderItem> = emptyList(),
    val status: String = "PLACED",
    val deliveryStatus: String = "PENDING",
    val paymentMethod: String = "COD",
    val paymentStatus: String = "PENDING", // PENDING, PAID, FAILED
    val riderLocationLat: Double = 0.0,
    val riderLocationLng: Double = 0.0,
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 25.0,
    val discount: Double = 0.0,
    val couponCode: String = "",
    val commission: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val statusTimeline: Map<String, Long> = emptyMap(),
    val customerName: String = "",
    val customerPhone: String = "",
    val deliveryAddress: String = "",
    val deliveryAddressObj: Address? = null,
    val deliveryOtp: String = "",
    val prescriptionImageUrl: String = "",
    // Cancellation
    val cancelledBy: String = "",
    val cancelReason: String = "",
    val cancelledAt: Long = 0,
    // Review
    val isReviewed: Boolean = false,
    // ETA
    val estimatedDeliveryMinutes: Int = 0,
) {
    companion object {
        /** Customer can cancel within 2 minutes of placing */
        const val CANCEL_WINDOW_MS = 2 * 60 * 1000L
    }

    fun canCancel(): Boolean {
        return status == "PLACED" &&
            (System.currentTimeMillis() - createdAt) < CANCEL_WINDOW_MS
    }
}

data class OrderItem(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val qty: Int = 1,
)

// Legacy CartItem to fix CustomerViewModel
data class CartItem(
    val product: Product = Product(),
    val quantity: Int = 1,
)

// ═══════════════════════════════════════════
// Master Data Models
// ═══════════════════════════════════════════
data class Category(
    val id: String = "",
    val name: String = "",
    val icon: String = "",
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true,
    val sortOrder: Int = 0,
)

data class Vendor(
    val id: String = "",
    val storeName: String = "",
    val storeAddress: String = "",
    val phone: String = "",
    val rating: Double = 4.0,
    val categoryId: String = "",
    val categoryName: String = "", // Added for compat
    @get:PropertyName("isStoreOpen") @set:PropertyName("isStoreOpen") var isStoreOpen: Boolean = true,
    @get:PropertyName("isBusy") @set:PropertyName("isBusy") var isBusy: Boolean = false,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val deliveryTimeMinutes: Int = 20,
    val minimumOrder: Double = 0.0,
    val ratingCount: Int = 0
)

data class Product(
    val id: String = "",
    val vendorId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val discountPrice: Double = 0.0, // Added for compat
    val unit: String = "pcs", // Added for compat
    val imageUrl: String = "",
    @get:PropertyName("isAvailable") @set:PropertyName("isAvailable") var isAvailable: Boolean = true,
    @get:PropertyName("isVeg") @set:PropertyName("isVeg") var isVeg: Boolean = true,
    val categoryId: String = "",
    val rating: Double = 4.0,
    val prescriptionRequired: Boolean = false
)

data class Payout(
    val id: String = "",
    val vendorId: String = "",
    val amount: Double = 0.0,
    val status: String = "PENDING",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class SupportTicket(
    val id: String = "",
    val userId: String = "",
    val userName: String = "Unknown",
    val userRole: String = "CUSTOMER",
    val subject: String = "",
    val description: String = "",
    val status: String = "OPEN",
    val createdAt: Long = System.currentTimeMillis(),
    val internalNotes: String = ""
)

data class AdminSettings(
    val id: String = "global",
    val appName: String = "Mana Delivery",
    val deliveryFeeBase: Double = 25.0,
    val globalCommissionRate: Double = 10.0,
    val taxPercentage: Double = 5.0,
    val isServiceEnabled: Boolean = true,
    val supportPhone: String = "9494378247",
    val maintenanceMessage: String = "We are temporarily closed for maintenance. Please check back later."
)

data class AuditLog(
    val id: String = "",
    val adminId: String = "",
    val adminName: String = "Admin",
    val action: String = "",
    val targetId: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Coupon(
    val id: String = "",
    val code: String = "",
    val description: String = "",
    val discountType: String = "PERCENTAGE", // PERCENTAGE or FLAT
    val discountValue: Double = 0.0,
    val minOrderAmount: Double = 0.0,
    val maxDiscountAmount: Double = 0.0, // Cap for percentage discounts
    val expiresAt: Long = 0, // 0 = never expires
    val usageLimit: Int = 0, // 0 = unlimited
    val usedCount: Int = 0,
    val isActive: Boolean = true,
) {
    fun isValid(orderSubtotal: Double): Boolean {
        if (!isActive) return false
        if (expiresAt > 0 && System.currentTimeMillis() > expiresAt) return false
        if (usageLimit > 0 && usedCount >= usageLimit) return false
        if (minOrderAmount > 0 && orderSubtotal < minOrderAmount) return false
        return true
    }

    fun calculateDiscount(orderSubtotal: Double): Double {
        if (!isValid(orderSubtotal)) return 0.0
        val raw = when (discountType) {
            "PERCENTAGE" -> orderSubtotal * (discountValue / 100.0)
            "FLAT" -> discountValue
            else -> 0.0
        }
        return if (maxDiscountAmount > 0) minOf(raw, maxDiscountAmount) else raw
    }
}

data class Banner(
    val id: String = "",
    val imageUrl: String = "",
    val actionType: String = "NONE",
    val targetId: String = "",
    val isActive: Boolean = true,
    val title: String = "",
    val sortOrder: Int = 0
)

// ═══════════════════════════════════════════
// Review Model
// ═══════════════════════════════════════════
data class Review(
    val id: String = "",
    val orderId: String = "",
    val userId: String = "",
    val userName: String = "",
    val vendorId: String = "",
    val rating: Int = 5, // 1-5 stars
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)
