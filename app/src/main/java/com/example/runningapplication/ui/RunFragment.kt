package com.example.runningapplication.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runningapplication.adapter.RunAdapter
import com.example.runningapplication.databinding.FragmentRunBinding
import com.example.runningapplication.other.SortType
import com.example.runningapplication.other.TrackingUtility
import com.example.runningapplication.ui.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunFragment : Fragment() {
    private var locationPermissionLauncher: ActivityResultLauncher<String>? = null
    private var backgroundLocationPermissionLauncher: ActivityResultLauncher<String>? = null
    private lateinit var binding: FragmentRunBinding
    private lateinit var runAdapter: RunAdapter
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRunBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val run = runAdapter.differ.currentList[position]
                viewModel.deleteRun(run)
                Snackbar.make(view, "Successfully deleted article", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") { viewModel.insertRun(run) }
                    show()
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.rvRuns)
        }


        when (viewModel.sortType) {
            SortType.DATE -> binding.spinnerFilter.setSelection(0)
            SortType.RUNNING_TIME -> binding.spinnerFilter.setSelection(1)
            SortType.AVERAGE_SPEED -> binding.spinnerFilter.setSelection(2)
            SortType.CALORIES_BURNT -> binding.spinnerFilter.setSelection(3)
            SortType.DISTANCE -> binding.spinnerFilter.setSelection(4)
        }
        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> viewModel.sortRun(SortType.DATE)
                    1 -> viewModel.sortRun(SortType.RUNNING_TIME)
                    2 -> viewModel.sortRun(SortType.AVERAGE_SPEED)
                    3 -> viewModel.sortRun(SortType.CALORIES_BURNT)
                    4 -> viewModel.sortRun(SortType.DISTANCE)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }
        viewModel.runs.observe(viewLifecycleOwner) {
            runAdapter.submitList(it)
        }

        binding.floatingActionButton.setOnClickListener {
            val action = RunFragmentDirections.actionRunFragmentToTrackingFragment()
            findNavController().navigate(action)
        }

        locationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    // Location permission is granted
                    checkBackgroundLocationPermission()
                } else {
                    showPermissionInfoAndNavigateToSettings()
                }
            }

        backgroundLocationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    return@registerForActivityResult
                } else {
                    Toast.makeText(requireContext(), "Background Location Permission is required", Toast.LENGTH_SHORT).show()
                }
            }

        checkLocationPermission()
    }

    private fun setupRecyclerView() = with(binding) {
        runAdapter = RunAdapter()
        rvRuns.adapter = runAdapter
        rvRuns.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Location permission is granted
                checkBackgroundLocationPermission()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> showLocationPermissionRationale()
            else -> requestLocationPermission()
        }
    }

    private fun showLocationPermissionRationale() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Needed!")
            .setMessage("Location Permission Needed!")
            .setPositiveButton("OK") { dialog, which ->
                requestLocationPermission()
            }
            .setNegativeButton("CANCEL") { dialog, which ->
                Toast.makeText(requireContext(), "Location Permission is required", Toast.LENGTH_SHORT).show()
            }
            .create().show()
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher?.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun checkBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    return
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) -> showBackgroundLocationPermissionRationale()
                else -> requestBackgroundLocationPermission()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showBackgroundLocationPermissionRationale() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Needed!")
            .setMessage("Background Location Permission Needed! Tap \"Allow all time in the next screen\"")
            .setPositiveButton("OK") { dialog, which ->
                requestBackgroundLocationPermission()
            }
            .setNegativeButton("CANCEL") { dialog, which ->
                Toast.makeText(requireContext(), "Background location Permission is required", Toast.LENGTH_SHORT).show()

            }
            .create().show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {
        backgroundLocationPermissionLauncher?.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    private fun showPermissionInfoAndNavigateToSettings() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Needed!")
            .setMessage("Location Permission Needed! Please go to app settings and enable the location permission.")
            .setPositiveButton("OK") { dialog, which ->
                navigateToAppSettings()
            }
            .create().show()
    }

    private fun navigateToAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireContext().packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
