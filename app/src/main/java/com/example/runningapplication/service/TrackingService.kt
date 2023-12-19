package com.example.runningapplication.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.runningapplication.R
import com.example.runningapplication.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningapplication.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapplication.other.Constants.ACTION_STOP_SERVICE
import com.example.runningapplication.other.Constants.LOCATION_FASTEST_INTERVAL
import com.example.runningapplication.other.Constants.LOCATION_MAX_DELAY_INTERVAL
import com.example.runningapplication.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runningapplication.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runningapplication.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runningapplication.other.Constants.NOTIFICATION_ID
import com.example.runningapplication.other.Constants.TIMER_UPDATE_INTERVAL
import com.example.runningapplication.other.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>;
typealias SeveralPolylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder
    private lateinit var currentNotificationBuilder: NotificationCompat.Builder
    private var isItFirstRun = true
    private val timeRunInSeconds = MutableLiveData<Long>()
    private var isKilledService = false

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoint = MutableLiveData<SeveralPolylines>()
        val timeRunsInMillis = MutableLiveData<Long>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoint.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunsInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        isTracking.observe(this) {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        }
    }

    @Suppress("DEPRECATION")
    private fun killService() {
        isKilledService = true
        isItFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isItFirstRun) {
                        startForegroundService()
                        isItFirstRun = false
                        Timber.d("Started service")
                    } else {
                        startTimer()
                        Timber.d("Resume service")
                    }

                }

                ACTION_PAUSE_SERVICE -> {
                    pauseService()
                    Timber.d("Pause service")
                }

                ACTION_STOP_SERVICE -> {
                    killService()
                    Timber.d("Stop service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnable = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnable = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                // time difference now and time when started
                lapTime = System.currentTimeMillis() - timeStarted
                //post new lap time
                timeRunsInMillis.postValue(timeRun + lapTime)

                if (timeRunsInMillis.value!! > lastSecondTimeStamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun pauseService() {
        isTimerEnable = false
        isTracking.postValue(false)
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if(isTracking) "Pause" else "Resume"
        val pendingIntent = if(isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if(!isKilledService) {
            currentNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = LOCATION_FASTEST_INTERVAL
                    priority = Priority.PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {

        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result.locations.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("Adding new location. Location is: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val position = LatLng(it.latitude, it.longitude)
            pathPoint.value?.apply {
                last().add(position)
                pathPoint.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoint.value?.apply {
        add(mutableListOf())
        pathPoint.postValue(this)
    } ?: pathPoint.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)


        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }


        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())
        timeRunInSeconds.observe(this) {
            if (!isKilledService) {
                val notification = currentNotificationBuilder.setContentText(
                    TrackingUtility.getFormattedStopWatchTime(it * 1000L)
                )
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManger: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManger.createNotificationChannel(channel)
    }
}
