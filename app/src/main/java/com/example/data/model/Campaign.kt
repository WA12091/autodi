package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campaigns")
data class Campaign(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val delaySeconds: Int = 5,
    val maxRetries: Int = 2,
    val status: String = "ACTIVE", // ACTIVE, PAUSED, COMPLETED
    val createdAt: Long = System.currentTimeMillis()
)
