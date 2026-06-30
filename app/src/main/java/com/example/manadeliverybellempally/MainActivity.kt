package com.example.manadeliverybellempally

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.manadeliverybellempally.theme.ManaDeliveryBellempallyTheme
import com.example.manadeliverybellempally.theme.ManaBgPrimary
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    @android.annotation.SuppressLint("InvalidFragmentVersionForActivityResult")
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "Notification permission granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase + App Check are already initialized in ManaDeliveryApp.
        // No need to duplicate here — saves ~200ms on Activity launch.

        askNotificationPermission()
        fetchAndLogToken()

        enableEdgeToEdge()
        setContent {
            ManaDeliveryBellempallyTheme { 
                Surface(modifier = Modifier.fillMaxSize(), color = ManaBgPrimary) { 
                    MainNavigation() 
                } 
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun fetchAndLogToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM", "Token: $token")
        }
    }
}
