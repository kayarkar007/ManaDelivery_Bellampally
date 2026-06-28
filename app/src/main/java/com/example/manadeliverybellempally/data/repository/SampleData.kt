package com.example.manadeliverybellempally.data.repository

import com.example.manadeliverybellempally.BuildConfig
import com.example.manadeliverybellempally.data.model.*
import kotlin.math.*

/**
 * Bellempally-customized sample data.
 * In production, this comes from Firebase Firestore.
 */
object SampleData {

    // ═══════════════════════════════════════════
    // Bellempally Categories
    // ═══════════════════════════════════════════
    val categories = listOf(
        Category("cat_grocery", "Groceries & Kirana", "ShoppingCart", isActive = true, sortOrder = 1),
        Category("cat_food", "Food & Tiffins", "Restaurant", isActive = true, sortOrder = 2),
        Category("cat_biryani", "Biryani & Special", "DinnerDining", isActive = true, sortOrder = 3),
        Category("cat_chicken", "Chicken & Mutton", "SetMeal", isActive = true, sortOrder = 4),
        Category("cat_fish", "Fish & Sea Food", "Phishing", isActive = true, sortOrder = 5),
        Category("cat_medicine", "Medicines & Health", "LocalPharmacy", isActive = true, sortOrder = 6),
        Category("cat_vegetables", "Vegetables & Fruits", "Eco", isActive = true, sortOrder = 7),
        Category("cat_dairy", "Milk & Dairy", "WaterDrop", isActive = true, sortOrder = 8),
        Category("cat_sweet", "Sweets & Bakery", "Cake", isActive = true, sortOrder = 9),
        Category("cat_water", "Water Can & Gas", "LocalGasStation", isActive = true, sortOrder = 10),
    )

    val vendors = emptyList<Vendor>()
    val products = emptyList<Product>()
    val sampleOrders = emptyList<Order>()

    // ═══════════════════════════════════════════
    // Bellempally Geofence
    // ═══════════════════════════════════════════
    data class GeoPoint(val lat: Double, val lng: Double)

    val BELLEMPALLY_CENTER = GeoPoint(BuildConfig.BELLEMPALLY_LAT, BuildConfig.BELLEMPALLY_LNG)
    const val DELIVERY_RADIUS_KM = 10.0 // Hardcoded fallback or use BuildConfig if it's constant

    fun isInDeliveryZone(lat: Double, lng: Double): Boolean {
        val earthRadius = 6371.0 
        val dLat = Math.toRadians(lat - BELLEMPALLY_CENTER.lat)
        val dLng = Math.toRadians(lng - BELLEMPALLY_CENTER.lng)
        val a = (sin(dLat / 2) * sin(dLat / 2)) +
                (cos(Math.toRadians(BELLEMPALLY_CENTER.lat)) * cos(Math.toRadians(lat)) *
                sin(dLng / 2) * sin(dLng / 2))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadius * c
        return distance <= DELIVERY_RADIUS_KM
    }

    const val OUTSIDE_ZONE_MESSAGE = "🚫 Delivery services available only in Bellempally & Sirpur Kagaznagar area."
}
