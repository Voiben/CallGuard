package com.nxt.callguard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A number the user wants blocked outright (manual entry or imported list).
 */
@Entity(tableName = "blocked_numbers")
data class BlockedNumber(
    @PrimaryKey
    val number: String,
    val reason: String,
    val source: BlockSource,
    val addedAt: Long = System.currentTimeMillis()
)

enum class BlockSource {
    MANUAL,
    SCAM_PREFIX_SEED,
    COMMUNITY_REPORT
}

/**
 * A scam/spam report the user filed for a number, kept locally.
 * Reports accumulate into a local "trust score" per number.
 */
@Entity(tableName = "spam_reports")
data class SpamReport(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val number: String,
    val category: SpamCategory,
    val note: String,
    val reportedAt: Long = System.currentTimeMillis()
)

enum class SpamCategory {
    LUA_DAO_TAI_CHINH,
    GIA_MAO_CO_QUAN_NHA_NUOC,
    QUANG_CAO_SPAM,
    DOI_NO,
    KHAC
}

fun SpamCategory.label(): String = when (this) {
    SpamCategory.LUA_DAO_TAI_CHINH -> "Lừa đảo tài chính"
    SpamCategory.GIA_MAO_CO_QUAN_NHA_NUOC -> "Giả mạo cơ quan nhà nước"
    SpamCategory.QUANG_CAO_SPAM -> "Quảng cáo/spam"
    SpamCategory.DOI_NO -> "Đòi nợ/khủng bố điện thoại"
    SpamCategory.KHAC -> "Khác"
}

/**
 * History of what CallGuard did with an incoming call or SMS.
 */
@Entity(tableName = "activity_log")
data class ActivityLogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val number: String,
    val type: ActivityType,
    val action: ActivityAction,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ActivityType { CALL, SMS }
enum class ActivityAction { BLOCKED, ALLOWED, FLAGGED }
