package com.example.runningapplication.other

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import com.example.runningapplication.databinding.MarkerViewBinding
import com.example.runningapplication.db.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@SuppressLint("ViewConstructor")
class CustomMarkerView
    (
    val runs: List<Run>,
    context: Context,
    layoutId: Int
) :
    MarkerView(context, layoutId) {
    private var binding: MarkerViewBinding

    init { // inflate binding and add as view
        binding = MarkerViewBinding.inflate(LayoutInflater.from(context))
        addView(binding.root)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())

    }
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null) {
            return
        }
        val currentRunId = e.x.toInt()
        val run = runs[currentRunId]
        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        val dateFormat = SimpleDateFormat("dd:MM:yy", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(calendar.time)
        val averageSpeed = "${run.averageSpeedInKMH}km/h"
        binding.tvAvgSpeed.text = averageSpeed
        val distanceInKm = "${run.distanceInMeters / 1000f}km"
        binding.tvDistance.text = distanceInKm
        binding.tvDuration.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMilliseconds)
        val caloriesBurnt = "${run.caloriesBurnt}kcal"
        binding.tvCaloriesBurned.text = caloriesBurnt
    }
}