package com.nxt.callguard.data

import android.content.Context
import com.nxt.callguard.core.BlockDecisionEngine
import com.nxt.callguard.core.CallDecision
import com.nxt.callguard.core.PhoneNumberUtils
import com.nxt.callguard.core.ReportEscalation
import com.nxt.callguard.core.SpamKeywordMatcher
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

class CallGuardRepository(context: Context, db: AppDatabase = AppDatabase.get(context)) {
    private val db = db
    private val appContext = context.applicationContext

    val blockedNumbers: Flow<List<BlockedNumber>> = db.blockedNumberDao().observeAll()
    val spamReports: Flow<List<SpamReport>> = db.spamReportDao().observeAll()
    val recentActivity: Flow<List<ActivityLogEntry>> = db.activityLogDao().observeRecent()

    /** Loads bundled example keyword/prefix lists once, on first run. */
    suspend fun seedIfEmpty() {
        if (db.blockedNumberDao().count() > 0) return
        val json = appContext.assets.open("scam_prefixes_vn.json").bufferedReader().use { it.readText() }
        val prefixes = JSONObject(json).getJSONArray("prefixes")
        val seeded = buildList {
            for (i in 0 until prefixes.length()) {
                val entry = prefixes.getJSONObject(i)
                add(
                    BlockedNumber(
                        number = PhoneNumberUtils.normalize(entry.getString("prefix")),
                        reason = entry.optString("label", "Đầu số mẫu"),
                        source = BlockSource.SCAM_PREFIX_SEED
                    )
                )
            }
        }
        if (seeded.isNotEmpty()) db.blockedNumberDao().upsert(seeded)
    }

    fun loadSpamKeywords(): List<String> {
        val json = appContext.assets.open("spam_sms_keywords_vn.json").bufferedReader().use { it.readText() }
        val arr = JSONObject(json).getJSONArray("keywords")
        return (0 until arr.length()).map { arr.getString(it).lowercase() }
    }

    /**
     * Decides block/allow using the same [BlockDecisionEngine] that is unit-tested
     * in the :core module - seeded prefix entries act as prefix rules, everything
     * else (manual additions, community-escalated numbers) must match exactly.
     */
    suspend fun isBlocked(number: String): Boolean = decide(number).decision == CallDecision.BLOCK

    suspend fun decide(number: String): com.nxt.callguard.core.CallVerdict {
        val all = db.blockedNumberDao().getAllOnce()
        val exact = all.filter { it.source != BlockSource.SCAM_PREFIX_SEED }.map { it.number }.toSet()
        val prefixes = all.filter { it.source == BlockSource.SCAM_PREFIX_SEED }.map { it.number }
        return BlockDecisionEngine.decide(number, exact, prefixes)
    }

    suspend fun addToBlocklist(number: String, reason: String, source: BlockSource = BlockSource.MANUAL) {
        db.blockedNumberDao().upsertOne(
            BlockedNumber(number = PhoneNumberUtils.normalize(number), reason = reason, source = source)
        )
    }

    suspend fun removeFromBlocklist(entry: BlockedNumber) {
        db.blockedNumberDao().delete(entry)
    }

    suspend fun reportNumber(number: String, category: SpamCategory, note: String) {
        db.spamReportDao().insert(
            SpamReport(number = PhoneNumberUtils.normalize(number), category = category, note = note)
        )
        val count = db.spamReportDao().reportCountFor(PhoneNumberUtils.normalize(number))
        if (ReportEscalation.shouldEscalate(count)) {
            addToBlocklist(number, "Bị báo cáo spam/lừa đảo $count lần", BlockSource.COMMUNITY_REPORT)
        }
    }

    suspend fun logActivity(number: String, type: ActivityType, action: ActivityAction, detail: String) {
        db.activityLogDao().insert(
            ActivityLogEntry(number = PhoneNumberUtils.normalize(number), type = type, action = action, detail = detail)
        )
    }

    /**
     * Local community-report info for a number, used to label calls from
     * numbers the user hasn't blocked outright yet. Null if no reports exist.
     */
    suspend fun spamLabelFor(number: String): String? {
        val normalized = PhoneNumberUtils.normalize(number)
        val count = db.spamReportDao().reportCountFor(normalized)
        if (count == 0) return null
        val category = db.spamReportDao().latestCategoryFor(normalized)?.label() ?: "nghi spam"
        return "$category ($count báo cáo)"
    }

    suspend fun blockedTodayCount(): Int {
        val startOfDay = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        return db.activityLogDao().blockedCountSince(startOfDay)
    }
}
