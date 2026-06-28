package com.example.manadeliverybellempally

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// ═══════════════════════════════════════════
// Navigation Keys — All app screens
// ═══════════════════════════════════════════

// Auth
@Serializable data object Splash : NavKey
@Serializable data object Login : NavKey
@Serializable data object OtpVerify : NavKey
@Serializable data object Signup : NavKey

// Dashboards
@Serializable data object Main : NavKey
@Serializable data object CustomerHome : NavKey
@Serializable data object VendorHome : NavKey
@Serializable data object RiderHome : NavKey
@Serializable data object AdminHome : NavKey

// Customer screens
@Serializable data class CategoryDetail(val categoryId: String) : NavKey
@Serializable data class VendorStore(val vendorId: String) : NavKey
@Serializable data object Search : NavKey
@Serializable data object Checkout : NavKey
@Serializable data object CustomerOrders : NavKey
@Serializable data class OrderTracking(val orderId: String) : NavKey
@Serializable data object OrderHistory : NavKey
@Serializable data object WalletScreen : NavKey
@Serializable data object ProfileScreen : NavKey
@Serializable data object AddressManager : NavKey

// Vendor screens
@Serializable data object VendorOrders : NavKey
@Serializable data object VendorProducts : NavKey
@Serializable data object VendorRevenue : NavKey
@Serializable data object VendorSettings : NavKey

// Rider screens
@Serializable data object RiderDeliveries : NavKey
@Serializable data class ActiveDelivery(val orderId: String) : NavKey
@Serializable data object RiderEarnings : NavKey
@Serializable data object RiderHistory : NavKey
@Serializable data object RiderCompliance : NavKey

// Admin Advanced screens
@Serializable data class AdminOrderDetail(val orderId: String) : NavKey
@Serializable data class AdminUserDetail(val userId: String) : NavKey
@Serializable data class AdminVendorDetail(val vendorId: String) : NavKey
@Serializable data object AdminCouponManager : NavKey
@Serializable data object AdminBannerManager : NavKey
@Serializable data object AdminSupportCenter : NavKey
@Serializable data object AdminGlobalSettings : NavKey
@Serializable data object AdminReports : NavKey
@Serializable data object AdminAuditLogs : NavKey
@Serializable data object AdminBroadcast : NavKey
@Serializable data object AdminSupport : NavKey
