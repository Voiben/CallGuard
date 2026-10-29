package com.nxt.callguard.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.nxt.callguard.CallGuardApp
import com.nxt.callguard.data.ActivityAction
import com.nxt.callguard.data.ActivityType
import com.nxt.callguard.util.NotificationHelper
import kotlinx.coroutines.launch

/**
 * Called by the system for every incoming call once CallGuard is granted the
 * Call Screening role (Settings > Apps > Default apps > Caller ID & spam app
 * on Android 10+, or Settings > Apps > CallGuard > "Set as default" via
 * RoleManager.ROLE_CALL_SCREENING).
 */
class CallGuardScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart ?: run {
            respondAllow(callDetails)
            return
        }

        val app = application as CallGuardApp
        app.appScope.launch {
            val blocked = app.repository.isBlocked(number)
            if (blocked) {
                app.repository.logActivity(number, ActivityType.CALL, ActivityAction.BLOCKED, "Số trong danh sách chặn")
                respondBlock(callDetails)
                NotificationHelper.notify(
                    applicationContext,
                    "Đã chặn cuộc gọi nghi lừa đảo",
                    number
                )
            } else {
                app.repository.logActivity(number, ActivityType.CALL, ActivityAction.ALLOWED, "Không nằm trong danh sách chặn")
                respondAllow(callDetails)

                // The system only delivers calls here for numbers NOT already
                // saved as a contact, so every call reaching this branch is by
                // definition an unknown number - safe to always show caller ID.
                val spamLabel = app.repository.spamLabelFor(number)
                val title = if (spamLabel != null) "⚠️ Số lạ nghi $spamLabel" else "Cuộc gọi từ số lạ"
                NotificationHelper.notifyUnknownCaller(applicationContext, title, number)
            }
        }
    }

    private fun respondBlock(callDetails: Call.Details) {
        val response = CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipNotification(true)
            .setSkipCallLog(false)
            .build()
        respondToCall(callDetails, response)
    }

    private fun respondAllow(callDetails: Call.Details) {
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .build()
        respondToCall(callDetails, response)
    }
}
