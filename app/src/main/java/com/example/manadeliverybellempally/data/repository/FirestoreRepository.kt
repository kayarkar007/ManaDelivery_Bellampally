package com.example.manadeliverybellempally.data.repository

import com.example.manadeliverybellempally.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    // ═══════════════════════════════════════════
    // USERS
    // ═══════════════════════════════════════════
    suspend fun createUser(user: User): Result<Unit> {
        return try {
            db.collection("users").document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<User?> {
        return try {
            val snapshot = db.collection("users").document(userId).get().await()
            if (snapshot.exists()) {
                Result.success(snapshot.toObject(User::class.java))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserFlow(userId: String): Flow<User?> = callbackFlow {
        val listener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(User::class.java))
            }
        awaitClose { listener.remove() }
    }

    fun getUsersFlow(): Flow<List<User>> = callbackFlow {
        val listener = db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(User::class.java).orEmpty())
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            db.collection("users").document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveAddress(userId: String, address: Address): Result<Unit> {
        return try {
            val userRef = db.collection("users").document(userId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val user = snapshot.toObject(User::class.java) ?: throw Exception("User not found")
                val updatedAddresses = user.addresses.toMutableList().apply {
                    val existingIndex = indexOfFirst { it.id == address.id }
                    if (existingIndex != -1) set(existingIndex, address) else add(address.copy(id = db.collection("users").document().id))
                }
                transaction.update(userRef, "addresses", updatedAddresses)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAddress(userId: String, addressId: String): Result<Unit> {
        return try {
            val userRef = db.collection("users").document(userId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val user = snapshot.toObject(User::class.java) ?: throw Exception("User not found")
                val updatedAddresses = user.addresses.filter { it.id != addressId }
                transaction.update(userRef, "addresses", updatedAddresses)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════
    // VENDORS
    // ═══════════════════════════════════════════
    suspend fun getVendors(): Result<List<Vendor>> {
        return try {
            val snapshot = db.collection("vendors").get().await()
            Result.success(snapshot.toObjects(Vendor::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getVendorsFlow(): Flow<List<Vendor>> = callbackFlow {
        val listener = db.collection("vendors")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val vendors = snapshot?.toObjects(Vendor::class.java)
                    ?.sortedBy { it.storeName }
                    .orEmpty()
                trySend(vendors)
            }
        awaitClose { listener.remove() }
    }

    fun getVendorFlow(vendorId: String): Flow<Vendor?> = callbackFlow {
        val listener = db.collection("vendors").document(vendorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Vendor::class.java))
            }
        awaitClose { listener.remove() }
    }

    suspend fun createVendor(vendor: Vendor): Result<Unit> {
        return try {
            db.collection("vendors").document(vendor.id).set(vendor).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVendor(vendor: Vendor): Result<Unit> {
        return try {
            val batch = db.batch()
            batch.set(db.collection("vendors").document(vendor.id), vendor)
            // Sync specific fields back to user record for dashboard/navigation fast access
            batch.update(db.collection("users").document(vendor.id), 
                mapOf(
                    "storeName" to vendor.storeName,
                    "isStoreOpen" to vendor.isStoreOpen,
                    "storeAddress" to vendor.storeAddress
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVendorOpenStatus(vendorId: String, isOpen: Boolean): Result<Unit> {
        return try {
            val batch = db.batch()
            batch.update(db.collection("vendors").document(vendorId), "isStoreOpen", isOpen)
            batch.update(db.collection("users").document(vendorId), "isStoreOpen", isOpen)
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════
    // PRODUCTS & MENU
    // ═══════════════════════════════════════════
    fun getProductsForVendorFlow(vendorId: String): Flow<List<Product>> = callbackFlow {
        val listener = db.collection("products")
            .whereEqualTo("vendorId", vendorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Product::class.java).orEmpty())
            }
        awaitClose { listener.remove() }
    }

    fun getProductsFlow(): Flow<List<Product>> = callbackFlow {
        val listener = db.collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Product::class.java).orEmpty())
            }
        awaitClose { listener.remove() }
    }

    fun getCategoriesFlow(): Flow<List<Category>> = callbackFlow {
        val listener = db.collection("categories")
            .orderBy("sortOrder")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Category::class.java).orEmpty())
            }
        awaitClose { listener.remove() }
    }

    suspend fun addProduct(product: Product): Result<Unit> {
        return try {
            val id = product.id.ifEmpty { db.collection("products").document().id }
            db.collection("products").document(id).set(product.copy(id = id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            db.collection("products").document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProductAvailability(productId: String, isAvailable: Boolean): Result<Unit> {
        return try {
            db.collection("products").document(productId).update("isAvailable", isAvailable).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════
    // ORDERS
    // ═══════════════════════════════════════════
    suspend fun createOrder(order: Order): Result<Unit> {
        return try {
            val id = order.id.ifEmpty { db.collection("orders").document().id }
            db.collection("orders").document(id).set(order.copy(id = id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getOrdersForVendorFlow(vendorId: String): Flow<List<Order>> = callbackFlow {
        val listener = db.collection("orders")
            .whereEqualTo("vendorId", vendorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.toObjects(Order::class.java)
                    ?.sortedByDescending { it.createdAt }
                    .orEmpty()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    fun getAllOrdersFlow(): Flow<List<Order>> = callbackFlow {
        val listener = db.collection("orders")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                try {
                    val orders = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(Order::class.java)
                        } catch (e: Exception) {
                            val data = doc.data ?: return@mapNotNull null
                            Order(
                                id = doc.id,
                                status = data["status"] as? String ?: "PLACED",
                                total = (data["total"] as? Number)?.toDouble() ?: 0.0,
                                subtotal = (data["subtotal"] as? Number)?.toDouble() ?: 0.0,
                                deliveryFee = (data["deliveryFee"] as? Number)?.toDouble() ?: 25.0,
                                commission = (data["commission"] as? Number)?.toDouble() ?: 0.0,
                                vendorName = data["vendorName"] as? String ?: "",
                                userName = data["userName"] as? String ?: "",
                                createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis()
                            )
                        }
                    }.orEmpty()
                    trySend(orders)
                } catch (e: Exception) {
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    fun getAvailableDeliveriesFlow(): Flow<List<Order>> = callbackFlow {
        val listener = db.collection("orders")
            .whereEqualTo("status", "READY")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Order::class.java).orEmpty())
            }
        awaitClose { listener.remove() }
    }

    fun getOrdersForRiderFlow(riderId: String): Flow<List<Order>> = callbackFlow {
        val listener = db.collection("orders")
            .whereEqualTo("riderId", riderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Order::class.java).orEmpty())
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateOrderRiderLocation(orderId: String, lat: Double, lng: Double): Result<Unit> {
        return try {
            db.collection("orders").document(orderId).update(
                mapOf(
                    "riderLocationLat" to lat,
                    "riderLocationLng" to lng
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun assignRiderToOrder(orderId: String, riderId: String, riderName: String, riderPhone: String): Result<Unit> {
        return try {
            val orderRef = db.collection("orders").document(orderId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(orderRef)
                val status = snapshot.getString("status").orEmpty()
                val currentRider = snapshot.getString("riderId").orEmpty()
                
                if (status != "READY" || currentRider.isNotEmpty()) {
                    throw IllegalStateException("Order no longer available")
                }
                
                transaction.update(orderRef, mapOf(
                    "riderId" to riderId,
                    "riderName" to riderName,
                    "riderPhone" to riderPhone,
                    "status" to "ACCEPTED",
                    "updatedAt" to System.currentTimeMillis(),
                    "statusTimeline.ACCEPTED" to System.currentTimeMillis()
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getOrdersForCustomerFlow(customerId: String): Flow<List<Order>> = callbackFlow {
        val listener = db.collection("orders")
            .whereEqualTo("userId", customerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Order::class.java).orEmpty())
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> {
        return try {
            db.collection("orders").document(orderId)
                .update(
                    mapOf(
                        "status" to status,
                        "updatedAt" to System.currentTimeMillis(),
                        "statusTimeline.$status" to System.currentTimeMillis()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePaymentStatus(orderId: String, paymentId: String, status: String): Result<Unit> {
        return try {
            db.collection("orders").document(orderId)
                .update(
                    mapOf(
                        "paymentStatus" to status,
                        "paymentId" to paymentId,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectOrder(orderId: String, reason: String): Result<Unit> {
        return try {
            db.collection("orders").document(orderId)
                .update(
                    mapOf(
                        "status" to "CANCELLED",
                        "rejectionReason" to reason,
                        "updatedAt" to System.currentTimeMillis(),
                        "statusTimeline.CANCELLED" to System.currentTimeMillis()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun processPayment(orderId: String, method: String): Result<Unit> {
        return try {
            db.collection("orders").document(orderId)
                .update(
                    mapOf(
                        "paymentMethod" to method,
                        "paymentStatus" to "PAID",
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderRiderLocation(orderId: String, lat: Double, lng: Double): Result<Unit> {
        return try {
            db.collection("orders").document(orderId)
                .update(
                    mapOf(
                        "riderLocationLat" to lat,
                        "riderLocationLng" to lng,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════
    // RIDER & FINANCE
    // ═══════════════════════════════════════════
    suspend fun updateRiderOnlineStatus(riderId: String, isOnline: Boolean): Result<Unit> {
        return try {
            db.collection("users").document(riderId).update("isOnline", isOnline).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPayoutsForUserFlow(userId: String): Flow<List<Payout>> = callbackFlow {
        val listener = db.collection("payouts")
            .whereEqualTo("vendorId", userId) // In our model vendorId serves as ownerId
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Payout::class.java).orEmpty())
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateFcmToken(userId: String, token: String): Result<Unit> {
        return try {
            db.collection("users").document(userId).update("fcmToken", token).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════
    // PROMOTIONS & CMS
    // ═══════════════════════════════════════════
    fun getCouponsFlow(): Flow<List<Coupon>> = callbackFlow {
        val listener = db.collection("coupons")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Coupon::class.java).orEmpty())
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveCoupon(coupon: Coupon): Result<Unit> {
        return try {
            val id = coupon.id.ifEmpty { db.collection("coupons").document().id }
            db.collection("coupons").document(id).set(coupon.copy(id = id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getBannersFlow(): Flow<List<Banner>> = callbackFlow {
        val listener = db.collection("banners")
            .orderBy("sortOrder")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Banner::class.java).orEmpty())
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveBanner(banner: Banner): Result<Unit> {
        return try {
            val id = banner.id.ifEmpty { db.collection("banners").document().id }
            db.collection("banners").document(id).set(banner.copy(id = id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════
    // SUPPORT & AUDIT
    // ═══════════════════════════════════════════
    fun getTicketsFlow(userId: String): Flow<List<SupportTicket>> = callbackFlow {
        val baseQuery = db.collection("tickets")
        val query = if (userId == "admin_all") baseQuery else baseQuery.whereEqualTo("userId", userId)
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val tickets = snapshot?.toObjects(SupportTicket::class.java).orEmpty()
                .sortedByDescending { it.createdAt }
            trySend(tickets)
        }
        awaitClose { listener.remove() }
    }

    suspend fun createTicket(ticket: SupportTicket): Result<Unit> {
        return try {
            val id = db.collection("tickets").document().id
            db.collection("tickets").document(id).set(ticket.copy(id = id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTicket(ticket: SupportTicket): Result<Unit> {
        return try {
            db.collection("tickets").document(ticket.id).set(ticket).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAdminSettingsFlow(): Flow<AdminSettings> = callbackFlow {
        val listener = db.collection("settings").document("global")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val settings = snapshot?.toObject(AdminSettings::class.java) ?: AdminSettings()
                trySend(settings)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateAdminSettings(settings: AdminSettings): Result<Unit> {
        return try {
            db.collection("settings").document("global").set(settings).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logAction(action: AuditLog): Result<Unit> {
        return try {
            val id = db.collection("audit_logs").document().id
            db.collection("audit_logs").document(id).set(action.copy(id = id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════
    // RIDER SPECIFIC ADVANCED
    // ═══════════════════════════════════════════
    fun getPayoutsFlow(userId: String): Flow<List<Payout>> = callbackFlow {
        val listener = db.collection("payouts")
            .whereEqualTo("vendorId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val payouts = snapshot?.toObjects(Payout::class.java).orEmpty()
                    .sortedByDescending { it.createdAt }
                trySend(payouts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateRiderLocation(riderId: String, lat: Double, lng: Double): Result<Unit> {
        return try {
            db.collection("users").document(riderId)
                .update(mapOf("currentLat" to lat, "currentLng" to lng, "lastSeen" to System.currentTimeMillis()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRiderKYC(riderId: String, docType: String, url: String): Result<Unit> {
        return try {
            db.collection("users").document(riderId).update(
                mapOf(
                    "kycDocuments.$docType" to url,
                    "kycStatus.$docType" to "PENDING",
                    "approvalStatus" to "PENDING"
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmCODCollection(orderId: String, riderId: String, amount: Double): Result<Unit> {
        return try {
            val riderRef = db.collection("users").document(riderId)
            val orderRef = db.collection("orders").document(orderId)
            
            db.runTransaction { transaction ->
                val riderSnap = transaction.get(riderRef)
                val currentBalance = riderSnap.getDouble("codBalance") ?: 0.0
                
                transaction.update(riderRef, "codBalance", currentBalance + amount)
                transaction.update(orderRef, mapOf(
                    "status" to "DELIVERED",
                    "paymentStatus" to "COMPLETED",
                    "updatedAt" to System.currentTimeMillis(),
                    "statusTimeline.DELIVERED" to System.currentTimeMillis()
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRiderVehicle(riderId: String, type: String, number: String, model: String): Result<Unit> {
        return try {
            db.collection("users").document(riderId).update(
                mapOf(
                    "vehicleType" to type,
                    "vehicleNumber" to number,
                    "vehicleModel" to model
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════
    // ORDER CANCELLATION
    // ═══════════════════════════════════════════
    suspend fun cancelOrder(orderId: String, userId: String, reason: String): Result<Unit> {
        return try {
            val orderRef = db.collection("orders").document(orderId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(orderRef)
                val status = snapshot.getString("status") ?: ""
                val createdAt = snapshot.getLong("createdAt") ?: 0L
                val orderUserId = snapshot.getString("userId") ?: ""

                // Validate: only PLACED orders within cancel window, by order owner
                if (status != "PLACED") {
                    throw IllegalStateException("Order cannot be cancelled in '$status' state")
                }
                if (orderUserId != userId) {
                    throw IllegalStateException("You can only cancel your own orders")
                }
                val elapsed = System.currentTimeMillis() - createdAt
                if (elapsed > 2 * 60 * 1000L) {
                    throw IllegalStateException("Cancellation window (2 minutes) has passed")
                }

                transaction.update(orderRef, mapOf(
                    "status" to "CANCELLED",
                    "cancelledBy" to userId,
                    "cancelReason" to reason,
                    "cancelledAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis(),
                    "statusTimeline.CANCELLED" to System.currentTimeMillis()
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════
    // REVIEWS
    // ═══════════════════════════════════════════
    suspend fun submitReview(review: Review): Result<Unit> {
        return try {
            val id = review.id.ifEmpty { db.collection("reviews").document().id }
            val batch = db.batch()

            // 1. Save the review
            batch.set(db.collection("reviews").document(id), review.copy(id = id))

            // 2. Mark the order as reviewed
            if (review.orderId.isNotEmpty()) {
                batch.update(db.collection("orders").document(review.orderId), "isReviewed", true)
            }

            batch.commit().await()

            // 3. Update vendor's average rating (separate transaction for safety)
            updateVendorRating(review.vendorId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateVendorRating(vendorId: String) {
        try {
            val reviews = db.collection("reviews")
                .whereEqualTo("vendorId", vendorId)
                .get().await()
            val ratings = reviews.toObjects(Review::class.java)
            if (ratings.isNotEmpty()) {
                val avgRating = ratings.map { it.rating }.average()
                db.collection("vendors").document(vendorId).update(
                    mapOf(
                        "rating" to (Math.round(avgRating * 10) / 10.0),
                        "ratingCount" to ratings.size
                    )
                ).await()
            }
        } catch (e: Exception) {
            // Non-critical — rating update can fail silently
        }
    }

    fun getReviewsForVendorFlow(vendorId: String): Flow<List<Review>> = callbackFlow {
        val listener = db.collection("reviews")
            .whereEqualTo("vendorId", vendorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reviews = snapshot?.toObjects(Review::class.java).orEmpty()
                    .sortedByDescending { it.createdAt }
                trySend(reviews)
            }
        awaitClose { listener.remove() }
    }

    // ═══════════════════════════════════════════
    // COUPON LOOKUP
    // ═══════════════════════════════════════════
    suspend fun getCouponByCode(code: String): Result<Coupon?> {
        return try {
            val snapshot = db.collection("coupons")
                .whereEqualTo("code", code.uppercase().trim())
                .limit(1)
                .get().await()
            val coupon = snapshot.toObjects(Coupon::class.java).firstOrNull()
            Result.success(coupon)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getActiveCouponsFlow(): kotlinx.coroutines.flow.Flow<List<Coupon>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = db.collection("coupons")
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Coupon::class.java).orEmpty())
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMockPushNotification(title: String, message: String): Result<Unit> {
        return try {
            val notif = mapOf(
                "title" to title,
                "message" to message,
                "type" to "PROMO",
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("promo_notifications").add(notif).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════
    // REORDER — Fetch original order items
    // ═══════════════════════════════════════════
    suspend fun getOrderById(orderId: String): Result<Order?> {
        return try {
            val snapshot = db.collection("orders").document(orderId).get().await()
            Result.success(snapshot.toObject(Order::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════
    // PROFILE PHOTO UPLOAD
    // ═══════════════════════════════════════════
    suspend fun uploadProfileImage(userId: String, imageUri: Uri): Result<String> {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            // Update user document
            db.collection("users").document(userId).update("profileImageUrl", downloadUrl).await()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════
    // PHASE 3: VENDOR & RIDER STREAMS
    // ═══════════════════════════════════════════
    fun getVendorOrdersFlow(vendorId: String): Flow<List<Order>> = callbackFlow {
        val listener = db.collection("orders")
            .whereEqualTo("vendorId", vendorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.toObjects(Order::class.java).orEmpty().sortedByDescending { it.createdAt }
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    fun getRiderOrdersFlow(riderId: String): Flow<List<Order>> = callbackFlow {
        val listener = db.collection("orders")
            .whereEqualTo("riderId", riderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.toObjects(Order::class.java).orEmpty().sortedByDescending { it.updatedAt }
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    // ═══════════════════════════════════════════
    // PHASE 7: REVIEWS & RATINGS
    // ═══════════════════════════════════════════

    fun getVendorReviewsFlow(vendorId: String): Flow<List<Review>> = callbackFlow {
        val listener = db.collection("reviews")
            .whereEqualTo("vendorId", vendorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reviews = snapshot?.toObjects(Review::class.java).orEmpty().sortedByDescending { it.createdAt }
                trySend(reviews)
            }
        awaitClose { listener.remove() }
    }

}
