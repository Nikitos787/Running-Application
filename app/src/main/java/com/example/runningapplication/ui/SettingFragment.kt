package com.example.runningapplication.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.runningapplication.R
import com.example.runningapplication.databinding.FragmentSettingBinding
import com.example.runningapplication.other.Constants
import com.example.runningapplication.other.Constants.KEY_NAME
import com.example.runningapplication.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingBinding
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFieldFromSharedPref()
        binding.buttonApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if (success) {
                Snackbar.make(view, "Your data is saved successfully", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(view, "Please fill out all fields", Snackbar.LENGTH_LONG).show()

            }
        }
    }

    private fun loadFieldFromSharedPref() {
        val name = sharedPreferences.getString(KEY_NAME, "") ?: ""
        val weight = sharedPreferences.getFloat(KEY_WEIGHT, 80f)
        with(binding) {
            edName.setText(name)
            edWeight.setText(weight.toString())
        }
    }

    private fun applyChangesToSharedPref(): Boolean {
        val name = binding.edName.text.toString()
        val weight = binding.edWeight.text.toString()
        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }
        sharedPreferences.edit()
            .putString(Constants.KEY_NAME, name)
            .putFloat(Constants.KEY_WEIGHT, weight.toFloat())
            .apply()
        val tooBarText = "Let's go, $name!"
        requireActivity().findViewById<MaterialTextView>(R.id.tvToolBar).text = tooBarText
        return true
    }
}
