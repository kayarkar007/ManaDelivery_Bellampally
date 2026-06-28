package com.example.manadeliverybellempally.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.manadeliverybellempally.MainActivity
import com.example.manadeliverybellempally.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ManaMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Mana Delivery"
        val message = remoteMessage.notification?.body ?: remoteMessage.data["message"] ?: "New Update"
        val type = remoteMessage.data["type"] ?: "SYSTEM"

        showNotification(title, message, type)
    }

    private fun showNotification(title: String, message: String, type: String) {
        val channelId = when (type) {
            "ORDER" -> "order_alerts"
            "PROMO" -> "promotions"
            else -> "default"
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = when (type) {
                "ORDER" -> "New Order Alerts"
                "PROMO" -> "Offers & Promotions"
                else -> "System Notifications"
            }
            val importance = if (type == "ORDER") NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            
            if (type == "ORDER") {
                channel.description = "Loud alerts for new incoming orders"
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
            .setPriority(if (type == "ORDER") NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    override fun onNewToken(token: String) {
        // Token update logic would go here
    }
}
