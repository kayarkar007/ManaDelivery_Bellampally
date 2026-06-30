package com.example.manadeliverybellempally

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

/**
 * Application class that initializes Firebase as early as possible.
 *
 * WHY: Firebase Phone Auth needs Play Integrity / App Check to be ready
 * BEFORE the first OTP request. Initializing in Application.onCreate()
 * ensures everything is warmed up by the time the user taps "Send OTP",
 * eliminating the cold-start penalty that causes the reCAPTCHA browser
 * redirect on the first attempt.
 */
class ManaDeliveryApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Firebase as early as possible
        FirebaseApp.initializeApp(this)

        // 2. Set up App Check — only for RELEASE builds.
        if (!BuildConfig.DEBUG) {
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }

        // 3. Enable Firestore offline persistence (50MB cache)
        //    WHY: Bellempally has areas with poor connectivity (mining colonies,
        //    industrial area). Offline cache ensures the app shows data even
        //    without internet, and syncs automatically when back online.
        val firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(
                PersistentCacheSettings.newBuilder()
                    .setSizeBytes(50L * 1024 * 1024) // 50 MB cache
                    .build()
            )
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = firestoreSettings

        // 4. Set the auth language to match the device locale.
        FirebaseAuth.getInstance().setLanguageCode(
            resources.configuration.locales[0].language
        )

        Log.d("ManaDeliveryApp", "Firebase initialized: AppCheck=${!BuildConfig.DEBUG}, Offline=50MB, Locale=${resources.configuration.locales[0].language}")
    }
}
