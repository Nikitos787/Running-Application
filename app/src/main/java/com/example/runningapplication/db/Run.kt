package com.example.runningapplication.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table")
data class Run(
    var image: Bitmap? = null,
    var timestamp: Long = 0L,
    var averageSpeedInKMH: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMilliseconds: Long = 0L,
    var caloriesBurnt: Int = 0
) {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
