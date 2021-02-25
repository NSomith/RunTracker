package com.example.runtracker.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.runtracker.R
import com.example.runtracker.utils.CustomMarkerView
import com.example.runtracker.utils.TrackingUtility
import com.example.runtracker.viewmodels.MainViewModel
import com.example.runtracker.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import java.lang.Math.round

@AndroidEntryPoint
class StatisticFragment:Fragment(R.layout.fragment_statistics) {
    private val viewModel: StatisticsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setupBarcharts()
    }

    private fun setupBarcharts(){
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.BLACK
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.axisLeft.apply {
            axisLineColor = Color.BLACK
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.axisRight.apply {
            axisLineColor = Color.BLACK
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.apply {
            description.text = "Avg Speed over time"
            legend.isEnabled = false
        }
    }


    private fun subscribeToObservers() {
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun = TrackingUtility.formattedStopWatchTime(it)
                tvTotalTime.text = totalTimeRun
            }
        })
        viewModel.totalDistanceRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10f) / 10f
                val totalDistanceString = "${totalDistance}km"
                tvTotalDistance.text = totalDistanceString
            }
        })
        viewModel.totalAvgspeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed = round(it * 10f) / 10f
                val avgSpeedString = "${avgSpeed}km/h"
                tvAverageSpeed.text = avgSpeedString
            }
        })
        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalCalories = "${it}kcal"
                tvTotalCalories.text = totalCalories
            }
        })

        viewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let {
                val allAvgSpeed = it.indices.map{ i->
                    BarEntry(i.toFloat(),it[i].avgSpeedInKMH)
                }
                val bardata = BarDataSet(allAvgSpeed,"Avg Speed Over Time").apply {
                    valueTextColor = Color.BLACK
                    color = ContextCompat.getColor(requireContext(),R.color.colorAccent)
                }
                barChart.data = BarData(bardata)
                barChart.marker = CustomMarkerView(it,requireContext(),R.layout.marker_view)
                barChart.invalidate()
            }

        })
    }

}