package com.example.manadeliverybellempally.data.repository

import com.example.manadeliverybellempally.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object SeedData {
    suspend fun seedAll() {
        val db = FirebaseFirestore.getInstance()
        seedCategories(db)
        seedVendorsAndProducts(db)
        seedRiders(db)
        seedSettings(db)
    }

    private suspend fun seedCategories(db: FirebaseFirestore) {
        val categories = listOf(
            Category(id = "cat_grocery", name = "Groceries", icon = "ShoppingCart", isActive = true, sortOrder = 1),
            Category(id = "cat_food", name = "Restaurants", icon = "Restaurant", isActive = true, sortOrder = 2),
            Category(id = "cat_biryani", name = "Biryani", icon = "DinnerDining", isActive = true, sortOrder = 3),
            Category(id = "cat_chicken", name = "Meat & Fish", icon = "SetMeal", isActive = true, sortOrder = 4),
            Category(id = "cat_medicine", name = "Pharmacy", icon = "LocalPharmacy", isActive = true, sortOrder = 5),
            Category(id = "cat_vegetables", name = "Vegetables", icon = "Eco", isActive = true, sortOrder = 6),
        )
        val batch = db.batch()
        categories.forEach { batch.set(db.collection("categories").document(it.id), it) }
        batch.commit().await()
    }

    private suspend fun seedVendorsAndProducts(db: FirebaseFirestore) {
        val vendors = listOf(
            Vendor(
                id = "v1", phone = "9000000001",
                storeName = "Raju Kirana & General", storeAddress = "Main Road, Bellempally",
                categoryId = "cat_grocery", categoryName = "Groceries",
                rating = 4.5, isStoreOpen = true, isBusy = false
            ),
            Vendor(
                id = "v2", phone = "9000000002",
                storeName = "Reddy Tiffins & Meals", storeAddress = "Station Area, Bellempally",
                categoryId = "cat_food", categoryName = "Restaurants",
                rating = 4.2, isStoreOpen = true, isBusy = false
            ),
            Vendor(
                id = "v3", phone = "9000000003",
                storeName = "Mehfil House", storeAddress = "Circle, Bellempally",
                categoryId = "cat_biryani", categoryName = "Biryani",
                rating = 4.8, isStoreOpen = true, isBusy = false
            ),
            Vendor(
                id = "v4", phone = "9000000004",
                storeName = "Sagar Chicken Center", storeAddress = "Market, Bellempally",
                categoryId = "cat_chicken", categoryName = "Meat & Fish",
                rating = 4.0, isStoreOpen = true, isBusy = true
            ),
            Vendor(
                id = "v5", phone = "9000000005",
                storeName = "Apollo Medicals", storeAddress = "Hospital Road",
                categoryId = "cat_medicine", categoryName = "Pharmacy",
                rating = 4.9, isStoreOpen = true, isBusy = false
            )
        )

        val products = listOf(
            Product(id = "p1", vendorId = "v1", name = "Rice (5kg)", description = "Sona Masoori", price = 350.0, discountPrice = 320.0, unit = "bag", isAvailable = true, isVeg = true),
            Product(id = "p2", vendorId = "v1", name = "Sugar (1kg)", description = "Pure white", price = 50.0, discountPrice = 0.0, unit = "kg", isAvailable = true, isVeg = true),
            Product(id = "p3", vendorId = "v2", name = "Idli (4pcs)", description = "Soft & hot", price = 40.0, discountPrice = 0.0, unit = "plate", isAvailable = true, isVeg = true),
            Product(id = "p4", vendorId = "v2", name = "Masala Dosa", description = "Crispy", price = 60.0, discountPrice = 55.0, unit = "plate", isAvailable = true, isVeg = true),
            Product(id = "p5", vendorId = "v3", name = "Chicken Biryani", description = "Single portion", price = 180.0, discountPrice = 160.0, unit = "plate", isAvailable = true, isVeg = false),
            Product(id = "p6", vendorId = "v3", name = "Family Biryani", description = "Serves 3-4", price = 450.0, discountPrice = 420.0, unit = "pack", isAvailable = true, isVeg = false),
            Product(id = "p7", vendorId = "v4", name = "Fresh Chicken (1kg)", description = "With skin", price = 240.0, discountPrice = 220.0, unit = "kg", isAvailable = true, isVeg = false),
            Product(id = "p8", vendorId = "v5", name = "Dolo 650", description = "Strip of 15", price = 30.0, discountPrice = 0.0, unit = "strip", isAvailable = true, isVeg = true)
        )

        val batch = db.batch()
        vendors.forEach { 
            batch.set(db.collection("vendors").document(it.id), it)
            val user = User(
                id = it.id, name = "Proprietor", phone = it.phone, role = "VENDOR", 
                storeName = it.storeName, approvalStatus = "APPROVED", isStoreOpen = it.isStoreOpen
            )
            batch.set(db.collection("users").document(it.id), user)
        }
        products.forEach { batch.set(db.collection("products").document(it.id), it) }
        batch.commit().await()
    }

    private suspend fun seedRiders(db: FirebaseFirestore) {
        val riders = listOf(
            User(id = "r1", name = "Ramesh Rider", phone = "8000000001", role = "RIDER", isOnline = true, approvalStatus = "APPROVED", vehicleNumber = "TS-19-1234"),
            User(id = "r2", name = "Suresh Rider", phone = "8000000002", role = "RIDER", isOnline = true, approvalStatus = "APPROVED", vehicleNumber = "TS-19-5678")
        )
        val batch = db.batch()
        riders.forEach { batch.set(db.collection("users").document(it.id), it) }
        batch.commit().await()
    }

    private suspend fun seedSettings(db: FirebaseFirestore) {
        val settings = AdminSettings(
            deliveryFeeBase = 20.0,
            globalCommissionRate = 10.0
        )
        db.collection("settings").document("global").set(settings).await()
    }
}
