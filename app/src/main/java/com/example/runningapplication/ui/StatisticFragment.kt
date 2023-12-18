package com.example.runningapplication.ui

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.example.runningapplication.R
import com.example.runningapplication.databinding.FragmentStatisticBinding
import com.example.runningapplication.other.CustomMarkerView
import com.example.runningapplication.other.TrackingUtility
import com.example.runningapplication.ui.viewmodel.StatisticViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatisticFragment : Fragment() {
    private lateinit var binding: FragmentStatisticBinding
    private val viewModel: StatisticViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setupBarChart()
    }

    private fun subscribeToObservers() {
        viewModel.totalTimeRun.observe(viewLifecycleOwner) {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                binding.tvTotalTime.text = totalTimeRun
            }
        }
        viewModel.totalDistanceInMeters.observe(viewLifecycleOwner) {
            it?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10f) / 10f
                val totalDistanceString = "${totalDistance.toString()}km"
                binding.tvTotalDistance.text = totalDistanceString
            }
        }
        viewModel.averageSpeedOfAllRuns.observe(viewLifecycleOwner) {
            it?.let {
                val averageSpeed = round(it * 10f) / 10f
                val averageSpeedString = "${averageSpeed}km/h"
                binding.tvAverageSpeed.text = averageSpeedString
            }
        }
        viewModel.totalCaloriesBurnt.observe(viewLifecycleOwner) {
            it?.let {
                val totalCaloriesBurnt = "${it}kcal"
                binding.tvTotalCalories.text = totalCaloriesBurnt
            }
        }
        viewModel.sortedByDate.observe(viewLifecycleOwner) {
            it?.let {
                val allAverageSpeeds = it.indices.map { i ->
                    BarEntry(i.toFloat(), it[i].averageSpeedInKMH)
                }
                val barDataSet = BarDataSet(allAverageSpeeds, "Average speed over time").apply {
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                }
                with(binding.barChart) {
                    data = BarData(barDataSet)
                    marker =
                        CustomMarkerView(it.reversed(), requireContext(), R.layout.marker_view)
                    invalidate()
                }
            }
        }
    }

    private fun setupBarChart() = with(binding.barChart) {
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        apply {
            description.text = "Avarage speed over time"
            legend.isEnabled = false
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = StatisticFragment()
    }
}
