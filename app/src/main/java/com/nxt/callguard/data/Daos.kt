package com.nxt.callguard.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedNumberDao {
    @Query("SELECT * FROM blocked_numbers ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<BlockedNumber>>

    @Query("SELECT * FROM blocked_numbers WHERE number = :number LIMIT 1")
    suspend fun find(number: String): BlockedNumber?

    @Query("SELECT * FROM blocked_numbers")
    suspend fun getAllOnce(): List<BlockedNumber>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(numbers: List<BlockedNumber>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOne(number: BlockedNumber)

    @Delete
    suspend fun delete(number: BlockedNumber)

    @Query("SELECT COUNT(*) FROM blocked_numbers")
    suspend fun count(): Int
}

@Dao
interface SpamReportDao {
    @Query("SELECT * FROM spam_reports ORDER BY reportedAt DESC")
    fun observeAll(): Flow<List<SpamReport>>

    @Insert
    suspend fun insert(report: SpamReport)

    @Query("SELECT COUNT(*) FROM spam_reports WHERE number = :number")
    suspend fun reportCountFor(number: String): Int

    @Query("SELECT category FROM spam_reports WHERE number = :number ORDER BY reportedAt DESC LIMIT 1")
    suspend fun latestCategoryFor(number: String): SpamCategory?
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_log ORDER BY timestamp DESC LIMIT 200")
    fun observeRecent(): Flow<List<ActivityLogEntry>>

    @Insert
    suspend fun insert(entry: ActivityLogEntry)

    @Query("SELECT COUNT(*) FROM activity_log WHERE action = 'BLOCKED' AND timestamp >= :sinceEpochMillis")
    suspend fun blockedCountSince(sinceEpochMillis: Long): Int
}
