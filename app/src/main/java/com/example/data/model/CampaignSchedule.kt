package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campaign_schedules")
data class CampaignSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val campaignId: Long,
    val campaignName: String,
    val title: String,
    val scheduledTimeMillis: Long,
    val repeatOption: String = "ONCE", // "ONCE", "DAILY", "WEEKDAYS"
    val status: String = "SCHEDULED", // "SCHEDULED", "RUNNING", "COMPLETED", "CANCELLED"
    val notes: String = "",
    val autoStartCockpit: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastExecutedAt: Long? = null
)
