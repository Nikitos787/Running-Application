package com.example.runningapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningapplication.R
import com.example.runningapplication.databinding.ItemListRunBinding
import com.example.runningapplication.db.Run
import com.example.runningapplication.other.TrackingUtility
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RunAdapter : RecyclerView.Adapter<RunAdapter.ViewHolder>() {
    private val differCalBack = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCalBack)

    fun submitList(list: List<Run>) = differ.submitList(list)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemListRunBinding.bind(itemView)

        fun bind(run: Run) = with(binding) {
            Glide.with(itemView).load(run.image).into(imRun)
            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = SimpleDateFormat("dd:MM:yy", Locale.getDefault())
            tvDate.text = dateFormat.format(calendar.time)
            val averageSpeed = "${run.averageSpeedInKMH}km/h"
            tvAverageSpeed.text = averageSpeed
            val distanceInKm = "${run.distanceInMeters / 1000f}km"
            tvDistance.text = distanceInKm
            tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMilliseconds)
            val caloriesBurnt = "${run.caloriesBurnt}kcal"
            tvCalories.text = caloriesBurnt
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_list_run,
                    parent,
                    false
                )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }
}
