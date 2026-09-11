package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_logs")
data class CallLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val campaignId: Long,
    val campaignName: String,
    val leadId: Long,
    val contactName: String,
    val phoneNumber: String,
    val company: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val disposition: String = "CONNECTED",
    val notes: String = ""
)
