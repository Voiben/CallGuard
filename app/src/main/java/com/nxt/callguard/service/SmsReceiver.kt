package com.nxt.callguard.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.nxt.callguard.CallGuardApp
import com.nxt.callguard.core.SpamKeywordMatcher
import com.nxt.callguard.data.ActivityAction
import com.nxt.callguard.data.ActivityType
import com.nxt.callguard.util.NotificationHelper
import kotlinx.coroutines.launch

/**
 * CallGuard is NOT a default SMS app, so it cannot delete/hide messages from
 * the system inbox - only detect and flag them. Making this the default SMS
 * app (full send/receive/compose UI) would be a much larger project; this
 * receiver is read-only and side-effect free beyond logging + notifying.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val app = context.applicationContext as CallGuardApp

        // goAsync() keeps the process alive long enough for the DB writes
        // below to finish, even if all messages are processed on background
        // dispatchers - a plain fire-and-forget launch{} per message risks
        // the process dying mid-write.
        val pendingResult = goAsync()
        app.appScope.launch {
            try {
                for (message in messages) {
                    val sender = message.originatingAddress ?: continue
                    val body = message.messageBody ?: ""

                    val blockedSender = app.repository.isBlocked(sender)
                    val keywords = app.repository.loadSpamKeywords()
                    val matchedKeyword = SpamKeywordMatcher.findMatch(body, keywords)

                    when {
                        blockedSender -> {
                            app.repository.logActivity(sender, ActivityType.SMS, ActivityAction.BLOCKED, "Số gửi nằm trong danh sách chặn")
                            NotificationHelper.notify(context, "SMS từ số bị chặn", sender)
                        }
                        matchedKeyword != null -> {
                            app.repository.logActivity(sender, ActivityType.SMS, ActivityAction.FLAGGED, "Chứa từ khoá nghi spam: \"$matchedKeyword\"")
                            NotificationHelper.notify(context, "Tin nhắn nghi spam/lừa đảo", "$sender: $matchedKeyword")
                        }
                        else -> {
                            app.repository.logActivity(sender, ActivityType.SMS, ActivityAction.ALLOWED, "Không phát hiện dấu hiệu spam")
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
