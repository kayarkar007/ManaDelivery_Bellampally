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
            Category("cat_grocery", "Groceries", "ShoppingCart", true, 1),
            Category("cat_food", "Restaurants", "Restaurant", true, 2),
            Category("cat_biryani", "Biryani", "DinnerDining", true, 3),
            Category("cat_chicken", "Meat & Fish", "SetMeal", true, 4),
            Category("cat_medicine", "Pharmacy", "LocalPharmacy", true, 5),
            Category("cat_vegetables", "Vegetables", "Eco", true, 6),
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
            Product("p1", "v1", "Rice (5kg)", "Sona Masoori", 350.0, 320.0, "bag", "", true, true),
            Product("p2", "v1", "Sugar (1kg)", "Pure white", 50.0, 0.0, "kg", "", true, true),
            Product("p3", "v2", "Idli (4pcs)", "Soft & hot", 40.0, 0.0, "plate", "", true, true),
            Product("p4", "v2", "Masala Dosa", "Crispy", 60.0, 55.0, "plate", "", true, true),
            Product("p5", "v3", "Chicken Biryani", "Single portion", 180.0, 160.0, "plate", "", true, false),
            Product("p6", "v3", "Family Biryani", "Serves 3-4", 450.0, 420.0, "pack", "", true, false),
            Product("p7", "v4", "Fresh Chicken (1kg)", "With skin", 240.0, 220.0, "kg", "", true, false),
            Product("p8", "v5", "Dolo 650", "Strip of 15", 30.0, 0.0, "strip", "", true, true)
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
