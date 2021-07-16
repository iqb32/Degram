package com.example.degram.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.degram.R
import com.example.degram.util.NotificationConstants.NOTIFICATION_CHANNEL_ID
import com.example.degram.util.NotificationConstants.NOTIFICATION_CHANNEL_NAME
import com.example.degram.util.NotificationConstants.NOTIFICATION_DESCRIPTION
import com.example.degram.util.NotificationConstants.NOTIFICATION_ID
import com.example.degram.util.NotificationConstants.NOTIFICATION_MESSAGE
import com.example.degram.util.NotificationConstants.NOTIFICATION_TITLE

fun showNotification(context : Context) {
    // Make a channel if necessary
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = NOTIFICATION_CHANNEL_NAME
        val description = NOTIFICATION_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
        channel.description = description

        // Add the channel
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        notificationManager?.createNotificationChannel(channel)
    }

    // Create the notification

    val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(NOTIFICATION_TITLE)
        .setContentText(NOTIFICATION_MESSAGE)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVibrate(LongArray(0))

    // Show the notification
    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
}

object NotificationConstants {
    val NOTIFICATION_CHANNEL_NAME: CharSequence = "Sync Degram Score"
    const val NOTIFICATION_DESCRIPTION = "Shows notifications whenever scores are bring synced."
    val NOTIFICATION_TITLE: CharSequence = "Degram"
    const val NOTIFICATION_CHANNEL_ID = "SYNC_NOTIFICATION"
    const val NOTIFICATION_ID = 1
    const val NOTIFICATION_MESSAGE = "Uploading Scores ..."
}

fun getRandomCode() : String {
    val allowedChars = (0..9)
    return (1..6)
        .map { allowedChars.random() }
        .joinToString("")
}