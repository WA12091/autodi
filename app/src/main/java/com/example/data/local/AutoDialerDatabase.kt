package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CallLog
import com.example.data.model.Campaign
import com.example.data.model.CampaignSchedule
import com.example.data.model.ContactLead

@Database(
    entities = [Campaign::class, ContactLead::class, CallLog::class, CampaignSchedule::class],
    version = 3,
    exportSchema = false
)
abstract class AutoDialerDatabase : RoomDatabase() {
    abstract fun campaignDao(): CampaignDao
    abstract fun contactLeadDao(): ContactLeadDao
    abstract fun callLogDao(): CallLogDao
    abstract fun campaignScheduleDao(): CampaignScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AutoDialerDatabase? = null

        fun getDatabase(context: Context): AutoDialerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AutoDialerDatabase::class.java,
                    "autodialer_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
