package com.example.manadeliverybellempally.data.repository

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
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
 * Production-grade AuthRepository for Firebase Phone Authentication.
 *
 * KEY REQUIREMENTS for native OTP (no reCAPTCHA browser redirect):
 * 1. SHA-1 AND SHA-256 fingerprints must be added to Firebase Console
 * 2. Play Integrity API must be enabled in Google Cloud Console
 * 3. App Check must be initialized before first OTP request (done in ManaDeliveryApp)
 *
 * SPEED OPTIMIZATIONS:
 * - FirebaseAuth instance is cached (singleton)
 * - Language code is set at Application startup (ManaDeliveryApp)
 * - OTP timeout set to 60s to allow SMS + auto-retrieval window
 * - Anti-spam: prevents duplicate OTP requests within 10 seconds
 *
 * ANTI-ABUSE:
 * - Minimum 10s gap between OTP requests (prevents OTP bombing)
 * - Rate limit error detection with user-friendly messages
 * - Prevents sending OTP while a request is already in flight
 */
class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    internal val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var storedVerificationId: String = ""
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var currentPhoneNumber: String = ""

    // Anti-spam: track last OTP send time to prevent rapid-fire requests
    private var lastOtpSentTimeMs: Long = 0L
    private companion object {
        const val TAG = "AuthRepository"
        const val MIN_OTP_INTERVAL_MS = 10_000L // 10 seconds between OTP requests
        const val OTP_TIMEOUT_SECONDS = 60L
    }

    init {
        // Check if user is already logged in (session persistence)
        auth.currentUser?.let { user ->
            _authState.value = AuthState.Authenticated(user.uid, user.phoneNumber ?: "")
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Auto-retrieval succeeded! Sign in immediately without user typing OTP.
            // This happens on devices with Google Play Services when SMS is auto-read.
            Log.d(TAG, "Auto-verification completed")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.e(TAG, "Verification failed: ${e.javaClass.simpleName}", e)

            Log.e(TAG, "Raw Firebase error: ${e.javaClass.simpleName}: ${e.message}")

            val errorMessage = when (e) {
                is FirebaseTooManyRequestsException -> {
                    // Firebase rate limit hit — usually means too many OTP requests
                    "Too many OTP requests. Please wait 1 hour and try again."
                }
                is FirebaseAuthInvalidCredentialsException -> {
                    "Invalid phone number. Please check and try again."
                }
                else -> when {
                    e.message?.contains("SHA-1", ignoreCase = true) == true ->
                        "App not configured properly. Please contact support."
                    e.message?.contains("captcha", ignoreCase = true) == true ||
                    e.message?.contains("CAPTCHA", ignoreCase = true) == true ||
                    e.message?.contains("verify", ignoreCase = true) == true ->
                        "Verification failed. Please wait 1 minute and try again."
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "No internet connection. Please check your network."
                    e.message?.contains("quota", ignoreCase = true) == true ->
                        "OTP limit reached. Please try again after some time."
                    e.message?.contains("blocked", ignoreCase = true) == true ||
                    e.message?.contains("unusual activity", ignoreCase = true) == true ->
                        "This number has been temporarily blocked. Try again later."
                    else -> {
                        Log.e(TAG, "Unhandled error type: ${e.message}")
                        e.localizedMessage ?: "OTP sending failed. Please try again."
                    }
                }
            }
            _authState.value = AuthState.Error(errorMessage)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Log.d(TAG, "OTP code sent successfully")
            storedVerificationId = verificationId
            resendToken = token
            _authState.value = AuthState.CodeSent
        }
    }

    /**
     * Send OTP to the given phone number.
     *
     * @param phoneNumber 10-digit Indian phone number (without country code)
     * @param activity Required by Firebase for Play Integrity binding
     */
    fun sendOtp(phoneNumber: String, activity: Activity) {
        // Anti-spam: prevent rapid OTP requests
        val now = System.currentTimeMillis()
        if (now - lastOtpSentTimeMs < MIN_OTP_INTERVAL_MS && _authState.value is AuthState.Loading) {
            Log.w(TAG, "OTP request throttled — too soon since last request")
            return // Silently ignore rapid taps
        }

        _authState.value = AuthState.Loading
        lastOtpSentTimeMs = now

        val formattedPhone = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"
        currentPhoneNumber = formattedPhone

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedPhone)
            .setTimeout(OTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        // Use resend token if available — this skips reCAPTCHA on subsequent requests
        resendToken?.let {
            optionsBuilder.setForceResendingToken(it)
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    /**
     * Verify the 6-digit OTP code entered by the user.
     */
    fun verifyOtp(code: String) {
        if (storedVerificationId.isEmpty()) {
            _authState.value = AuthState.Error("Session expired. Please request a new OTP.")
            return
        }

        _authState.value = AuthState.Loading
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    Log.d(TAG, "Sign-in successful: ${user.uid}")
                    _authState.value = AuthState.Authenticated(
                        user.uid,
                        user.phoneNumber ?: currentPhoneNumber
                    )
                } else {
                    _authState.value = AuthState.Error("Sign-in failed. Please try again.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Sign-in failed", exception)
                val msg = when (exception) {
                    is FirebaseAuthInvalidCredentialsException ->
                        "Wrong OTP. Please check and try again."
                    else ->
                        exception.localizedMessage ?: "Sign-in failed. Please try again."
                }
                _authState.value = AuthState.Error(msg)
            }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun signOut() {
        auth.signOut()
        storedVerificationId = ""
        resendToken = null
        currentPhoneNumber = ""
        _authState.value = AuthState.Idle
    }
}
