package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Campaign
import kotlinx.coroutines.flow.Flow

@Dao
interface CampaignDao {
    @Query("SELECT * FROM campaigns ORDER BY createdAt DESC")
    fun getAllCampaigns(): Flow<List<Campaign>>

    @Query("SELECT * FROM campaigns WHERE id = :id")
    fun getCampaignById(id: Long): Flow<Campaign?>

    @Query("SELECT * FROM campaigns WHERE id = :id")
    suspend fun getCampaignByIdSync(id: Long): Campaign?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign): Long

    @Update
    suspend fun updateCampaign(campaign: Campaign)

    @Delete
    suspend fun deleteCampaign(campaign: Campaign)

    @Query("DELETE FROM campaigns WHERE id = :id")
    suspend fun deleteCampaignById(id: Long)

    @Query("UPDATE campaigns SET status = :status WHERE id = :id")
    suspend fun updateCampaignStatus(id: Long, status: String)
}
