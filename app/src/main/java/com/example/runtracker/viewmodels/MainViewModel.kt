package com.example.runtracker.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runtracker.db.Run
import com.example.runtracker.repo.MainRepository
import com.example.runtracker.utils.SortType
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
        val repository: MainRepository
):ViewModel() {

    fun insertRun(run: Run)= viewModelScope.launch {
        repository.insertRun(run)
    }

    private fun runsSortedByDate() = repository.getAllRunSortedByDate()
    private fun runsAllRunSortedByAvgSpeed() = repository.getAllRunSortedByAvgSpeed()
    private fun runsAllRunSortedByCaloriesBurned() = repository.getAllRunSortedByCaloriesBurned()
    private fun runsAllRunSortedByDistance() = repository.getAllRunSortedByDistance()
    private fun runsAllRunSortedByTimeinMills() = repository.getAllRunSortedByTimeinMills()

//    take multiple live data and perform the action
    val runs = MediatorLiveData<List<Run>>()

    var sortType = SortType.Date

    init {
        runs.addSource(runsSortedByDate()){
            if(sortType == SortType.Date){
                it?.let {
                   runs.value = it
                }
            }
        }
        runs.addSource(runsAllRunSortedByAvgSpeed()){
            if(sortType == SortType.Average_Speed){
                it?.let {
                    runs.value = it
                }
            }
        }
        runs.addSource(runsAllRunSortedByCaloriesBurned()){
            if(sortType == SortType.Calories_Burned){
                it?.let {
                    runs.value = it
                }
            }
        }
        runs.addSource(runsAllRunSortedByDistance()){
            if(sortType == SortType.Distance){
                it?.let {
                    runs.value = it
                }
            }
        }
        runs.addSource(runsAllRunSortedByTimeinMills()){
            if(sortType == SortType.Running_Time){
                it?.let {
                    runs.value = it
                }
            }
        }
    }

    fun sortType(sortType: SortType) =
            when(sortType){
        SortType.Date -> runsSortedByDate().value?.let {
            runs.value = it
        }
        SortType.Running_Time -> runsAllRunSortedByTimeinMills().value?.let {
            runs.value = it
        }
        SortType.Calories_Burned -> runsAllRunSortedByCaloriesBurned().value?.let {
            runs.value = it
        }
        SortType.Average_Speed -> runsAllRunSortedByAvgSpeed().value?.let {
            runs.value = it
        }
        SortType.Distance -> runsAllRunSortedByDistance().value?.let {
            runs.value = it
        }
    }.also {
        this.sortType = sortType
    }

}