package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CampaignSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface CampaignScheduleDao {
    @Query("SELECT * FROM campaign_schedules ORDER BY scheduledTimeMillis ASC")
    fun getAllSchedules(): Flow<List<CampaignSchedule>>

    @Query("SELECT * FROM campaign_schedules WHERE status = 'SCHEDULED' ORDER BY scheduledTimeMillis ASC")
    fun getPendingSchedules(): Flow<List<CampaignSchedule>>

    @Query("SELECT * FROM campaign_schedules WHERE id = :id")
    fun getScheduleById(id: Long): Flow<CampaignSchedule?>

    @Query("SELECT * FROM campaign_schedules WHERE id = :id")
    suspend fun getScheduleByIdSync(id: Long): CampaignSchedule?

    @Query("SELECT * FROM campaign_schedules WHERE status = 'SCHEDULED' AND scheduledTimeMillis <= :currentTimeMillis")
    suspend fun getDueSchedulesSync(currentTimeMillis: Long): List<CampaignSchedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: CampaignSchedule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<CampaignSchedule>)

    @Update
    suspend fun updateSchedule(schedule: CampaignSchedule)

    @Delete
    suspend fun deleteSchedule(schedule: CampaignSchedule)

    @Query("DELETE FROM campaign_schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Long)

    @Query("UPDATE campaign_schedules SET status = :status WHERE id = :id")
    suspend fun updateScheduleStatus(id: Long, status: String)
}
