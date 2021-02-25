package com.example.runtracker.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.runtracker.repo.MainRepository

class StatisticsViewModel @ViewModelInject constructor(
        val repository: MainRepository
):ViewModel() {

    val totalTimeRun = repository.getTotalTimeinMills()
    val totalAvgspeed = repository.getTotalAvgSpeed()
    val totalCaloriesBurned = repository.getTotalCaloriesBurned()
    val totalDistanceRun = repository.getTotalDistance()

    val runsSortedByDate = repository.getAllRunSortedByDate()
}