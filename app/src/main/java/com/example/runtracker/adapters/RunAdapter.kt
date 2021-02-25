package com.example.runtracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runtracker.R
import com.example.runtracker.db.Run
import com.example.runtracker.utils.TrackingUtility
import kotlinx.android.synthetic.main.item_run.view.*
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter :RecyclerView.Adapter<RunAdapter.RunViewHolder>() {
    inner class RunViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview){

    }

    val diffcallback = object :DiffUtil.ItemCallback<Run>(){
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this,diffcallback)

    fun submitRun(run:List<Run>) = differ.submitList(run)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_run,parent,false))
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(run.img).into(ivRunImage)
            val calander = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateformat = SimpleDateFormat("dd/MM/yy",Locale.getDefault())
            tvDate.text = dateformat.format(calander.time)

            tvAvgSpeed.text ="${run.avgSpeedInKMH}Km/h"
            tvDistance.text = "${run.distanceInMeter / 1000f}Km"

            tvTime.text = TrackingUtility.formattedStopWatchTime(run.timeInMillis)

            tvCalories.text = "${run.caloriesBurned}kcal"
        }
    }

    override fun getItemCount(): Int {
       return differ.currentList.size
    }
}