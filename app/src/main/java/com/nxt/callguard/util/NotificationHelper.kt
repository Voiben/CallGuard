package com.nxt.callguard.util

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.nxt.callguard.CallGuardApp
import com.nxt.callguard.service.QuickBlockReceiver

object NotificationHelper {
    private const val UNKNOWN_CALLER_NOTIFICATION_ID = 1
    private var nextId = 1000

    fun notify(context: Context, title: String, text: String) {
        postNotification(context, tag = null, id = nextId++, title = title, text = text, action = null)
    }

    /**
     * Caller-ID style notification for an incoming call from an unknown
     * (non-contact) number, with a one-tap "Chặn số này" action.
     */
    fun notifyUnknownCaller(context: Context, title: String, number: String) {
        val blockIntent = Intent(context, QuickBlockReceiver::class.java).apply {
            putExtra(QuickBlockReceiver.EXTRA_NUMBER, number)
            putExtra(QuickBlockReceiver.EXTRA_NOTIFICATION_ID, UNKNOWN_CALLER_NOTIFICATION_ID)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            number.hashCode(),
            blockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        postNotification(
            context,
            tag = number,
            id = UNKNOWN_CALLER_NOTIFICATION_ID,
            title = title,
            text = number,
            action = NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Chặn số này",
                pendingIntent
            ).build()
        )
    }

    private fun postNotification(
        context: Context,
        tag: String?,
        id: Int,
        title: String,
        text: String,
        action: NotificationCompat.Action?
    ) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return

        val builder = NotificationCompat.Builder(context, CallGuardApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
        if (action != null) builder.addAction(action)

        ContextCompat.getSystemService(context, NotificationManager::class.java)
            ?.notify(tag, id, builder.build())
    }
}
