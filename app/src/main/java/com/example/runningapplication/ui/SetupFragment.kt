package com.example.runningapplication.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.runningapplication.R
import com.example.runningapplication.databinding.FragmentSetupBinding
import com.example.runningapplication.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runningapplication.other.Constants.KEY_NAME
import com.example.runningapplication.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment() {
    private lateinit var binding: FragmentSetupBinding
    @set:Inject
    var isFirstAppOpen = true
    @Inject
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var navController: NavController


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        shouldSkipThisFragment(savedInstanceState)
        setupListeners()
    }

    private fun shouldSkipThisFragment(savedInstanceState: Bundle?) {
        if (!isFirstAppOpen) {
            val navOption = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()
            navController.navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOption
            )
        }
    }

    private fun setupListeners() {
        binding.tvContinue.setOnClickListener {
            val success = writePersonalDataToSharePreference()
            if (success) {
                val action = SetupFragmentDirections.actionSetupFragmentToRunFragment()
                navController.navigate(action)
            } else {
                Snackbar.make(
                    requireView(), "You should write your name and weight for start using this app",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun writePersonalDataToSharePreference(): Boolean {
        val name = binding.edName.text.toString()
        val weight = binding.edWeight.text.toString()
        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }
        sharedPreferences.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()
        val tooBarText = "Let's go, $name!"
        requireActivity().findViewById<MaterialTextView>(R.id.tvToolBar).text = tooBarText
        return true
    }

    companion object {

        @JvmStatic
        fun newInstance() = SetupFragment()
    }
}
