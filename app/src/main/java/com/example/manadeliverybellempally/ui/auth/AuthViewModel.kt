package com.example.manadeliverybellempally.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manadeliverybellempally.data.model.User
import com.example.manadeliverybellempally.data.model.UserRole
import com.example.manadeliverybellempally.data.model.Vendor
import com.example.manadeliverybellempally.data.repository.AuthRepository
import com.example.manadeliverybellempally.data.repository.AuthState
import com.example.manadeliverybellempally.data.repository.FirestoreRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    val authRepository = AuthRepository()
    private val firestoreRepository = FirestoreRepository()

    val authState: StateFlow<AuthState> = authRepository.authState

    private val _dbSyncState = MutableStateFlow<DbSyncState>(DbSyncState.Idle)

    // Store the user's name from signup for later use
    private var _userName: String = ""

    fun setUserName(name: String) {
        _userName = name
    }

    fun sendOtp(phoneNumber: String, activity: Activity) {
        authRepository.sendOtp(phoneNumber, activity)
    }

    fun verifyOtp(code: String) {
        authRepository.verifyOtp(code)
    }

    suspend fun getRoleForUser(uid: String): UserRole {
        val result = firestoreRepository.getUser(uid)
        val user = result.getOrNull()
        
        // Ensure token is fresh even on existing login
        if (user != null) {
            updateFcmTokenInternal(uid)
        }

        return if (user != null) {
            try { UserRole.valueOf(user.role) } catch (_: Exception) { UserRole.CUSTOMER }
        } else {
            UserRole.CUSTOMER
        }
    }

    suspend fun syncUserToDatabaseAndGetRole(uid: String, phone: String, requestedRole: UserRole): UserRole {
        _dbSyncState.value = DbSyncState.Syncing

        // Force Admin role for the specific number requested by the user
        val actualRole = if (phone.endsWith("7659989335")) {
            UserRole.ADMIN
        } else {
            requestedRole
        }

        val result = firestoreRepository.getUser(uid)
        if (result.isFailure) {
            _dbSyncState.value = DbSyncState.Error(result.exceptionOrNull()?.message ?: "Unable to load user")
            return UserRole.CUSTOMER
        }

        val existingUser = result.getOrNull()
        if (existingUser != null) {
            _dbSyncState.value = DbSyncState.Success
            updateFcmTokenInternal(uid)
            val dbRole = runCatching { UserRole.valueOf(existingUser.role) }.getOrDefault(UserRole.CUSTOMER)
            return if (phone.endsWith("7659989335")) UserRole.ADMIN else dbRole
        }

        val safeRequestedRole = if (actualRole == UserRole.ADMIN) UserRole.ADMIN else requestedRole
        val displayName = _userName.ifEmpty {
            when (actualRole) {
                UserRole.CUSTOMER -> "Customer"
                UserRole.VENDOR -> "Vendor"
                UserRole.RIDER -> "Rider"
                UserRole.ADMIN -> "Admin (Root)"
            }
        }

        val newUser = User(
            id = uid,
            phone = phone,
            role = actualRole.name,
            name = displayName,
            walletBalance = 0.0,
        )

        val createUserResult = firestoreRepository.createUser(newUser)
        if (createUserResult.isFailure) {
            _dbSyncState.value = DbSyncState.Error(createUserResult.exceptionOrNull()?.message ?: "Unable to create user")
            return UserRole.CUSTOMER
        }

        if (safeRequestedRole == UserRole.VENDOR) {
            val vendor = Vendor(
                id = uid,
                phone = phone,
                storeName = "$displayName Store",
                categoryName = "General",
                isStoreOpen = false,
            )
            firestoreRepository.createVendor(vendor)
        }

        updateFcmTokenInternal(uid)
        _dbSyncState.value = DbSyncState.Success
        return safeRequestedRole
    }

    private fun updateFcmTokenInternal(uid: String) {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                firestoreRepository.updateFcmToken(uid, token)
            } catch (e: Exception) {
                // Log failure but don't block auth
            }
        }
    }

    fun resetState() {
        authRepository.resetState()
    }

    fun signOut() {
        authRepository.signOut()
        _dbSyncState.value = DbSyncState.Idle
    }
}

sealed class DbSyncState {
    object Idle : DbSyncState()
    object Syncing : DbSyncState()
    object Success : DbSyncState()
    data class Error(val message: String) : DbSyncState()
}
