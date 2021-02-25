package com.example.runtracker.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("Select * from runnig_table order by timestamp desc")
    fun getAllRunSortedByDate():LiveData<List<Run>>

    @Query("Select * from runnig_table order by timeInMillis desc")
    fun getAllRunSortedByTimeinMills():LiveData<List<Run>>

    @Query("Select * from runnig_table order by caloriesBurned desc")
    fun getAllRunSortedByCaloriesBurned():LiveData<List<Run>>

    @Query("Select * from runnig_table order by avgSpeedInKMH desc")
    fun getAllRunSortedByAvgSpeed():LiveData<List<Run>>

    @Query("Select * from runnig_table order by distanceInMeter desc")
    fun getAllRunSortedByDistance():LiveData<List<Run>>

    @Query("Select sum(timeInMillis) from runnig_table")
    fun getTotalTimeinMills():LiveData<Long>

    @Query("Select sum(caloriesBurned) from runnig_table")
    fun getTotalCaloriesBurned():LiveData<Int>

    @Query("Select sum(distanceInMeter) from runnig_table")
    fun getTotalDistance():LiveData<Int>

    @Query("Select avg(avgSpeedInKMH) from runnig_table")
    fun getTotalAvgSpeed():LiveData<Float>

}