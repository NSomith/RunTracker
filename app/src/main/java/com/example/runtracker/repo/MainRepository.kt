package com.example.runtracker.repo

import com.example.runtracker.db.Run
import com.example.runtracker.db.RunDao
import javax.inject.Inject

class MainRepository @Inject constructor(
        val runDao: RunDao
) {
    suspend fun insertRun(run: Run) = runDao.insertRun(run)
    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)
    fun getAllRunSortedByDate() = runDao.getAllRunSortedByDate()
    fun getAllRunSortedByDistance() = runDao.getAllRunSortedByDistance()
    fun getAllRunSortedByAvgSpeed() = runDao.getAllRunSortedByAvgSpeed()
    fun getAllRunSortedByCaloriesBurned() = runDao.getAllRunSortedByCaloriesBurned()
    fun getAllRunSortedByTimeinMills() = runDao.getAllRunSortedByTimeinMills()
    fun getTotalTimeinMills() = runDao.getTotalTimeinMills()
    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()
    fun getTotalDistance() = runDao.getTotalDistance()
    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()


}