package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ContactLead
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactLeadDao {
    @Query("SELECT * FROM contact_leads WHERE campaignId = :campaignId ORDER BY id ASC")
    fun getLeadsForCampaign(campaignId: Long): Flow<List<ContactLead>>

    @Query("SELECT * FROM contact_leads ORDER BY id DESC")
    fun getAllLeads(): Flow<List<ContactLead>>

    @Query("SELECT * FROM contact_leads WHERE campaignId = :campaignId AND status = 'PENDING' ORDER BY id ASC")
    suspend fun getPendingLeadsSync(campaignId: Long): List<ContactLead>

    @Query("SELECT * FROM contact_leads WHERE campaignId = :campaignId AND (status = 'PENDING' OR (status IN ('BUSY', 'NO_ANSWER', 'VOICEMAIL') AND callAttempts < :maxAttempts)) ORDER BY id ASC")
    suspend fun getDialableLeadsSync(campaignId: Long, maxAttempts: Int): List<ContactLead>

    @Query("SELECT * FROM contact_leads WHERE id = :id")
    suspend fun getLeadById(id: Long): ContactLead?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: ContactLead): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeads(leads: List<ContactLead>)

    @Update
    suspend fun updateLead(lead: ContactLead)

    @Delete
    suspend fun deleteLead(lead: ContactLead)

    @Query("UPDATE contact_leads SET status = 'PENDING', callAttempts = 0, lastDisposition = null WHERE campaignId = :campaignId")
    suspend fun resetCampaignLeads(campaignId: Long)

    @Query("SELECT COUNT(*) FROM contact_leads WHERE campaignId = :campaignId")
    fun getLeadCountForCampaign(campaignId: Long): Flow<Int>

    @Query("DELETE FROM contact_leads WHERE campaignId = :campaignId")
    suspend fun deleteLeadsForCampaign(campaignId: Long)
}
