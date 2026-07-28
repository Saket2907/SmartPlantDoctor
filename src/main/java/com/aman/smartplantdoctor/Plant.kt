package com.aman.smartplantdoctor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val species: String,
    val healthScore: Int = 100,
    val wateringIntervalDays: Int = 3,
    val lastWateredTimestamp: Long = System.currentTimeMillis()
)
