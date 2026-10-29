package com.nxt.callguard.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.nxt.callguard.CallGuardApp
import com.nxt.callguard.data.BlockSource
import com.nxt.callguard.util.NotificationHelper
import kotlinx.coroutines.launch

/**
 * Handles the "Chặn số này" action button on the "cuộc gọi từ số lạ"
 * notification (see CallGuardScreeningService). Not exported - only our own
 * PendingIntent can trigger it.
 */
class QuickBlockReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val number = intent.getStringExtra(EXTRA_NUMBER) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        // The process can be killed right after onReceive() returns, so the
        // DB write must finish inside the extended broadcast window goAsync()
        // grants - a plain fire-and-forget launch{} risks losing the write.
        val pendingResult = goAsync()
        val app = context.applicationContext as CallGuardApp
        app.appScope.launch {
            try {
                app.repository.addToBlocklist(number, "Chặn nhanh từ thông báo", BlockSource.MANUAL)
                if (notificationId != -1) {
                    ContextCompat.getSystemService(context, NotificationManager::class.java)?.cancel(number, notificationId)
                }
                NotificationHelper.notify(context, "Đã thêm vào danh sách chặn", number)
            } catch (e: Exception) {
                Log.e(TAG, "failed to quick-block $number", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "QuickBlockReceiver"
        const val EXTRA_NUMBER = "extra_number"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
