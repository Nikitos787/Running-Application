package com.example.runningapplication.ui

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runningapplication.NavGraphDirections
import com.example.runningapplication.R
import com.example.runningapplication.databinding.ActivityMainBinding
import com.example.runningapplication.other.Constants
import com.example.runningapplication.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    @Inject
    lateinit var username: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // to instantiate the navController value so we could use it.
        initNavController()
        // check if the activity wasn't alive and been called from the foreground service than whe need it to take this action
        receivedIntentActionToNavigateToTrackingFragment(intent)
        // set the custom toolbar that in the xml.
        setSupportActionBar(binding.toolBar)
        //.. init setups.
        init()
    }

    private fun init() {
        setUsernameInToolbar()
        setupBottomNavView()
    }

    @SuppressLint("SetTextI18n")
    private fun setUsernameInToolbar() {
        if (username.isNotEmpty())
            binding.tvToolBar.text = "Let's run, $username!"
    }

    /**
     * this if the activity wasn't dead
     * and it received a new intent
     * so we act accordingly
     * */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        receivedIntentActionToNavigateToTrackingFragment(intent)
    }

    private fun setupBottomNavView() {
        val bnv = binding.bottomNavMenu

        // set it up
        bnv.setupWithNavController(navController)
        // guard navigation to change visibility
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.settingFragment,
                R.id.runFragment,
                R.id.statisticFragment,
                -> bnv.isVisible = true

                else -> bnv.isVisible = false
            }
        }
    }

    private fun initNavController() {
        val mainNavHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment)!!
        navController = mainNavHostFragment
            .findNavController()
    }

    private fun receivedIntentActionToNavigateToTrackingFragment(intent: Intent?) {
        if (intent?.action == Constants.ACTION_SHOW_TRACKING_FRAGMENT) {
            navController.navigate(R.id.action_global_tracking_fragment)
        }
    }

}
