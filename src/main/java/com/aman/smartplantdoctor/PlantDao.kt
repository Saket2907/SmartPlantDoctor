package com.aman.smartplantdoctor

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

@Dao
interface PlantDao {
    @Query("SELECT * FROM plants")
    suspend fun getAllPlants(): List<Plant>

    @Insert
    suspend fun insertPlant(plant: Plant)

    @Update
    suspend fun updatePlant(plant: Plant)

    @Delete
    suspend fun deletePlant(plant: Plant)
}
