package com.example.runningapplication.ui.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningapplication.db.Run
import com.example.runningapplication.other.SortType
import com.example.runningapplication.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val mainRepository: MainRepository) : ViewModel() {
    private val runSortedByDate = mainRepository.getAllRunSortedByDate()
    private val runSortedByAverageSpeed = mainRepository.getAllRunSortedByAverageSpeed()
    private val runSortedByCalories = mainRepository.getAllRunSortedByCalories()
    private val runSortedByTime = mainRepository.getAllRunSortedByTime()
    private val runSortedByDistance = mainRepository.getAllRunSortedByDistance()
    val runs = MediatorLiveData<List<Run>>()
    var sortType = SortType.DATE

    init {
        runs.addSource(runSortedByDate) { result ->
            if (sortType == SortType.DATE) {
                result?.let {
                    runs.value = it
                }
            }
        }
        runs.addSource(runSortedByAverageSpeed) { result ->
            if (sortType == SortType.AVERAGE_SPEED) {
                result?.let {
                    runs.value = it
                }
            }
        }
        runs.addSource(runSortedByCalories) { result ->
            if (sortType == SortType.CALORIES_BURNT) {
                result?.let {
                    runs.value = it
                }
            }
        }
        runs.addSource(runSortedByTime) { result ->
            if (sortType == SortType.RUNNING_TIME) {
                result?.let {
                    runs.value = it
                }
            }
        }
        runs.addSource(runSortedByDistance) { result ->
            if (sortType == SortType.DISTANCE) {
                result?.let {
                    runs.value = it
                }
            }
        }
    }

    fun sortRun(sortType: SortType) = when (sortType) {
        SortType.RUNNING_TIME -> runSortedByTime.value?.let {
            runs.value = it
        }

        SortType.DATE -> runSortedByDate.value?.let {
            runs.value = it
        }

        SortType.AVERAGE_SPEED -> runSortedByAverageSpeed.value?.let {
            runs.value = it
        }

        SortType.CALORIES_BURNT -> runSortedByCalories.value?.let {
            runs.value = it
        }
        SortType.DISTANCE -> runSortedByDistance.value?.let {
            runs.value = it
        }.also { this.sortType = sortType }
    }

    fun insertRun(run: Run) = viewModelScope.launch { mainRepository.insertRun(run) }
    fun deleteRun(run: Run) = viewModelScope.launch { mainRepository.deleteRun(run) }
}
