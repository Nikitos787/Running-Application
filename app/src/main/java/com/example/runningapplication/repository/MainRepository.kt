package com.example.runningapplication.repository

import com.example.runningapplication.db.Run
import com.example.runningapplication.db.RunDao
import javax.inject.Inject

class MainRepository @Inject constructor(val runDao: RunDao) {

    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunSortedByDate() = runDao.getAllRunSortedByDate()

    fun getAllRunSortedByAverageSpeed() = runDao.getAllRunSortedByAverageSpeed()

    fun getAllRunSortedByDistance()= runDao.getAllRunSortedByDistance()

    fun getAllRunSortedByTime()= runDao.getAllRunSortedByTime()

    fun getAllRunSortedByCalories() = runDao.getAllRunSortedByCalories()

    fun getTotalTimeRunInMilliseconds() = runDao.getTotalTimeRunInMilliseconds()

    fun getTotalCaloriesBurnt() = runDao.getTotalCaloriesBurnt()

    fun getTotalDistanceInMiters() = runDao.getTotalDistanceInMiters()

    fun getAverageSpeedOfAllRuns() = runDao.getAverageSpeedOfAllRuns()
}
