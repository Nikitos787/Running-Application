package com.example.runningapplication.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.runningapplication.R
import com.example.runningapplication.databinding.FragmentTrackingBinding
import com.example.runningapplication.db.Run
import com.example.runningapplication.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningapplication.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapplication.other.Constants.ACTION_STOP_SERVICE
import com.example.runningapplication.other.Constants.CANCEL_TRACKING_DIALOG_TAG
import com.example.runningapplication.other.Constants.MAP_ZOOM
import com.example.runningapplication.other.Constants.Polyline_COLOR
import com.example.runningapplication.other.Constants.Polyline_WIDTH
import com.example.runningapplication.other.TrackingUtility
import com.example.runningapplication.service.Polyline
import com.example.runningapplication.service.TrackingService
import com.example.runningapplication.ui.viewmodel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(), MenuProvider {
    private lateinit var binding: FragmentTrackingBinding
    private val viewModel: MainViewModel by viewModels()
    private var map: GoogleMap? = null
    private var isTracking = false
    private var pathPoint = mutableListOf<Polyline>()
    private var currentTimeMillis = 0L
    private var menu: Menu? = null
    @set:Inject
    var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    private fun stopRun() {
        binding.tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        val action = TrackingFragmentDirections.actionTrackingFragmentToRunFragment()
        findNavController().navigate(action)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        if (savedInstanceState != null) {
            val cancelTrackingManager = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG) as CancelTrackingDialog?
            cancelTrackingManager?.setYesListener { stopRun() }
        }
        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }
        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveInDb()
        }

        setupGoogleMapInstance(savedInstanceState)
        subscribeToObservers()
    }

    private fun setupGoogleMapInstance(savedInstanceState: Bundle?) = with(binding) {
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync {
            this@TrackingFragment.map = it
            addAllPolylines()
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        binding.mapView.onDestroy()
//    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    private fun moveCameraToUser() {
        if (pathPoint.isNotEmpty() && pathPoint.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoint.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }

        TrackingService.pathPoint.observe(viewLifecycleOwner) {
            pathPoint = it
            addLatestPolyline()
            moveCameraToUser()
        }

        TrackingService.timeRunsInMillis.observe(viewLifecycleOwner) {
            currentTimeMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeMillis, true)
            binding.tvTimer.text = formattedTime
        }
    }

    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && currentTimeMillis > 0L) {
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.visibility = View.VISIBLE
        } else if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            binding.btnToggleRun.text = "Stop"
            binding.btnFinishRun.visibility = View.GONE
        }
    }

    private fun addAllPolylines() {
        for (polyline in pathPoint) {
            val polylineOption = PolylineOptions()
                .color(Polyline_COLOR)
                .width(Polyline_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOption)
        }
    }

    private fun addLatestPolyline() {
        if (pathPoint.isNotEmpty() && pathPoint.last().size > 1) {
            val preLastLatLng = pathPoint.last()[pathPoint.last().size - 2]
            val lastLatLng = pathPoint.last().last()
            val polylineOptions = PolylineOptions()
                .color(Polyline_COLOR)
                .width(Polyline_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bound = LatLngBounds.builder()
        for (polyline in pathPoint) {
            for (position in polyline) {
                bound.include(position)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bound.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveInDb() {
        map?.snapshot { bmp ->
            var distanceInMiters = 0
            for (polyline in pathPoint) {
                distanceInMiters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val averageSpeedInKMH = round(
                (distanceInMiters / 1000f) / (currentTimeMillis / 1000f / 60 / 60) * 10
            ) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurnt = ((distanceInMiters / 1000f) * weight).toInt()
            val run = Run(
                image = bmp,
                timestamp = dateTimeStamp,
                averageSpeedInKMH = averageSpeedInKMH,
                distanceInMeters = distanceInMiters,
                timeInMilliseconds = currentTimeMillis,
                caloriesBurnt = caloriesBurnt
            )
            viewModel.insertRun(run)
            Snackbar.make(requireActivity().findViewById(R.id.rootView),
                "Run is saved successfully",
                Snackbar.LENGTH_LONG).show()
            stopRun()
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.tool_bar_menu_tracking, menu)
        this.menu = menu
        if (currentTimeMillis > 0) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.miCancelTracking -> showCancelTrackingDialog()
        }
        return false
    }

    companion object {

        @JvmStatic
        fun newInstance() = TrackingFragment()
    }
}
