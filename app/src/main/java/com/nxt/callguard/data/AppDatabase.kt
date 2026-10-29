package com.nxt.callguard.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromBlockSource(value: BlockSource): String = value.name
    @TypeConverter
    fun toBlockSource(value: String): BlockSource = BlockSource.valueOf(value)

    @TypeConverter
    fun fromSpamCategory(value: SpamCategory): String = value.name
    @TypeConverter
    fun toSpamCategory(value: String): SpamCategory = SpamCategory.valueOf(value)

    @TypeConverter
    fun fromActivityType(value: ActivityType): String = value.name
    @TypeConverter
    fun toActivityType(value: String): ActivityType = ActivityType.valueOf(value)

    @TypeConverter
    fun fromActivityAction(value: ActivityAction): String = value.name
    @TypeConverter
    fun toActivityAction(value: String): ActivityAction = ActivityAction.valueOf(value)
}

@Database(
    entities = [BlockedNumber::class, SpamReport::class, ActivityLogEntry::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun spamReportDao(): SpamReportDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "callguard.db"
                ).build().also { instance = it }
            }

        /**
         * A fresh, isolated in-memory database for tests - never the shared
         * singleton from [get]. Query/transaction executors run synchronously
         * on the calling thread, which avoids background-threadpool work that
         * can outlive a single Robolectric test sandbox.
         */
        fun createInMemoryForTesting(context: Context): AppDatabase {
            val directExecutor = java.util.concurrent.Executor { it.run() }
            return Room.inMemoryDatabaseBuilder(context.applicationContext, AppDatabase::class.java)
                .setQueryExecutor(directExecutor)
                .setTransactionExecutor(directExecutor)
                .allowMainThreadQueries()
                .build()
        }
    }
}
