package com.example.manadeliverybellempally.data.repository

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object CodeSent : AuthState()
    data class Authenticated(val uid: String, val phone: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * Real AuthRepository using Firebase Phone Authentication.
 * Requires SHA-1 and SHA-256 fingerprints to be added to Firebase Console for real SMS.
 */
class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var storedVerificationId: String = ""
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var currentPhoneNumber: String = ""

    init {
        // Check if user is already logged in
        auth.currentUser?.let { user ->
            _authState.value = AuthState.Authenticated(user.uid, user.phoneNumber ?: "")
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Auto-retrieval or instant verification
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.e("AuthRepository", "Verification failed", e)
            val errorMessage = when {
                e.message?.contains("SHA-1") == true -> "Device not registered. Please add SHA-1 fingerprint to Firebase Console."
                e.message?.contains("captcha") == true -> "reCAPTCHA check failed. Please try again."
                else -> e.message ?: "Verification failed."
            }
            _authState.value = AuthState.Error(errorMessage)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            storedVerificationId = verificationId
            resendToken = token
            _authState.value = AuthState.CodeSent
        }
    }

    fun sendOtp(phoneNumber: String, activity: Activity) {
        _authState.value = AuthState.Loading
        
        val formattedPhone = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"
        currentPhoneNumber = formattedPhone

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            // Note: Enabling Play Integrity and adding SHA keys in Console 
            // is mandatory to stop the browser redirect.

        resendToken?.let {
            optionsBuilder.setForceResendingToken(it)
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    fun verifyOtp(code: String) {
        if (storedVerificationId.isEmpty()) {
            _authState.value = AuthState.Error("Verification ID is missing. Request a new OTP.")
            return
        }

        _authState.value = AuthState.Loading
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        _authState.value = AuthState.Authenticated(user.uid, user.phoneNumber ?: currentPhoneNumber)
                    }
                } else {
                    val msg = task.exception?.message ?: "Sign-in failed."
                    _authState.value = AuthState.Error(msg)
                }
            }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }
}
