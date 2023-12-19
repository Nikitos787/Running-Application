package com.example.runningapplication.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
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
        requestPermissions()
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
    }

    private fun requestPermissions() {
        if (TrackingUtility.isLocationOk(requireContext())) {
            Toast.makeText(requireContext(), "Permission done", Toast.LENGTH_LONG).show()
        } else {
            TrackingUtility.requestLocationPermission(requireActivity())
        }
    }

    private fun setupRecyclerView() = with(binding) {
        runAdapter = RunAdapter()
        rvRuns.adapter = runAdapter
        rvRuns.layoutManager = LinearLayoutManager(requireContext())
    }
}
