package com.nxt.callguard

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.nxt.callguard.data.CallGuardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CallGuardApp : Application() {

    val repository: CallGuardRepository by lazy { CallGuardRepository(this) }
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        appScope.launch { repository.seedIfEmpty() }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Cảnh báo CallGuard",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Thông báo chặn cuộc gọi/SMS spam" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "callguard_alerts"
    }
}
