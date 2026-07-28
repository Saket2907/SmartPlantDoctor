package com.aman.smartplantdoctor

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScanDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    suspend fun getAllScans(): List<ScanHistory>

    @Insert
    suspend fun insertScan(scan: ScanHistory)
}
