package com.example.runtracker.utils

import android.content.Context
import com.example.runtracker.db.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.marker_view.view.*
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
        val run:List<Run>,
        c : Context,
        layoutId : Int
) : MarkerView(c,layoutId){

    override fun getOffset(): MPPointF {
        return MPPointF(-width/2f,-height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if(e == null){
            return
        }
        val curIdx = e.x.toInt() //since we store it in map format
        val run = run[curIdx]

        val calander = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        val dateformat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        tvDate.text = dateformat.format(calander.time)

        tvAvgSpeed.text ="${run.avgSpeedInKMH}Km/h"
        tvDistance.text = "${run.distanceInMeter / 1000f}Km"

        tvDuration.text = TrackingUtility.formattedStopWatchTime(run.timeInMillis)

        tvCaloriesBurned.text = "${run.caloriesBurned}kcal"
    }
}