package com.example.manadeliverybellempally.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.manadeliverybellempally.MainActivity
import com.example.manadeliverybellempally.R
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ManaMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Mana Delivery"
        val message = remoteMessage.notification?.body ?: remoteMessage.data["message"] ?: "New Update"
        val type = remoteMessage.data["type"] ?: "SYSTEM"

        Log.d("ManaFCM", "Message received: type=$type, title=$title")
        showNotification(title, message, type)
    }

    private fun showNotification(title: String, message: String, type: String) {
        val channelId = when (type) {
            "ORDER" -> "order_alerts"
            "PROMO" -> "promotions"
            "RIDER" -> "rider_alerts"
            else -> "default"
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = when (type) {
                "ORDER" -> "New Order Alerts"
                "PROMO" -> "Offers & Promotions"
                "RIDER" -> "Delivery Alerts"
                else -> "System Notifications"
            }
            val importance = if (type == "ORDER" || type == "RIDER") 
                NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            
            if (type == "ORDER" || type == "RIDER") {
                channel.description = "Important alerts for orders and deliveries"
                channel.enableVibration(true)
            }
            
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(if (type == "ORDER" || type == "RIDER") NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    override fun onNewToken(token: String) {
        Log.d("ManaFCM", "New FCM token generated: ${token.take(10)}...")
        // Save the new token to Firestore so push notifications reach this device
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .update("fcmToken", token)
            .addOnFailureListener { e ->
                Log.e("ManaFCM", "Failed to update FCM token", e)
            }
    }
}
