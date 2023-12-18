package com.example.runningapplication.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RunDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getAllRunSortedByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY averageSpeedInKMH DESC")
    fun getAllRunSortedByAverageSpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY distanceInMeters DESC")
    fun getAllRunSortedByDistance(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY timeInMilliseconds DESC")
    fun getAllRunSortedByTime(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY caloriesBurnt DESC")
    fun getAllRunSortedByCalories(): LiveData<List<Run>>

    @Query("SELECT SUM(timeInMilliseconds) FROM running_table")
    fun getTotalTimeRunInMilliseconds(): LiveData<Long>

    @Query("SELECT SUM(caloriesBurnt) FROM running_table")
    fun getTotalCaloriesBurnt(): LiveData<Int>

    @Query("SELECT SUM(distanceInMeters) FROM running_table")
    fun getTotalDistanceInMiters(): LiveData<Int>

    @Query("SELECT AVG(averageSpeedInKMH) FROM running_table")
    fun getAverageSpeedOfAllRuns(): LiveData<Float>
}
