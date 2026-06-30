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
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

    companion object {
        var paymentCallback: ((Boolean, String?) -> Unit)? = null
    }

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
        
        Checkout.preload(applicationContext)

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

    override fun onPaymentSuccess(paymentId: String?, paymentData: PaymentData?) {
        Log.d("Razorpay", "Payment Success: $paymentId")
        paymentCallback?.invoke(true, paymentId)
        paymentCallback = null
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        Log.e("Razorpay", "Payment Error $code: $response")
        paymentCallback?.invoke(false, null)
        paymentCallback = null
    }
}
