package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contact_leads",
    foreignKeys = [
        ForeignKey(
            entity = Campaign::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["campaignId"])]
)
data class ContactLead(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val campaignId: Long,
    val name: String,
    val phoneNumber: String,
    val email: String = "",
    val location: String = "",
    val company: String = "",
    val status: String = "PENDING", // PENDING, CALLING, CONNECTED, CONVERTED, BUSY, NO_ANSWER, VOICEMAIL, WRONG_NUMBER, SKIPPED
    val callAttempts: Int = 0,
    val lastDialedAt: Long? = null,
    val lastDisposition: String? = null,
    val notes: String = ""
)
