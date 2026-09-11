package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CallLog
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallLog>>

    @Query("SELECT * FROM call_logs WHERE campaignId = :campaignId ORDER BY timestamp DESC")
    fun getCallLogsForCampaign(campaignId: Long): Flow<List<CallLog>>

    @Query("SELECT * FROM call_logs WHERE id = :id")
    suspend fun getCallLogById(id: Long): CallLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(callLog: CallLog): Long

    @Update
    suspend fun updateCallLog(callLog: CallLog)

    @Delete
    suspend fun deleteCallLog(callLog: CallLog)

    @Query("DELETE FROM call_logs")
    suspend fun clearAllLogs()

    @Query("SELECT COUNT(*) FROM call_logs")
    fun getTotalCallCount(): Flow<Int>
}
