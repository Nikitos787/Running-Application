package com.example.runningapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.runningapplication.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(private val mainRepository: MainRepository): ViewModel(){
    val totalTimeRun = mainRepository.getTotalTimeRunInMilliseconds()
    val totalCaloriesBurnt = mainRepository.getTotalCaloriesBurnt()
    val totalDistanceInMeters = mainRepository.getTotalDistanceInMiters()
    val averageSpeedOfAllRuns = mainRepository.getAverageSpeedOfAllRuns()
    val sortedByDate = mainRepository.getAllRunSortedByDate()
}
