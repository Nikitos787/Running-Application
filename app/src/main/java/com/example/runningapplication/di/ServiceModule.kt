package com.example.runningapplication.di

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.runningapplication.R
import com.example.runningapplication.other.Constants
import com.example.runningapplication.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runningapplication.ui.MainActivity
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context) =
        LocationServices.getFusedLocationProviderClient(context)

    @ServiceScoped
    @Provides
    fun getMainActivityPendingIntent(@ApplicationContext context: Context) =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).also {
                it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
            },
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT // that means when ever we lunch the pending intent it will update it self instead of re crating
        )

    @ServiceScoped
    @Provides
    fun getNotificationBuilder(@ApplicationContext context: Context, pendingIntent: PendingIntent) =
        NotificationCompat.Builder(
            context,
            NOTIFICATION_CHANNEL_ID
        )
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_run)
            .setContentTitle("Running app")
            .setContentText("00:00:00")
            .setContentIntent(pendingIntent)
}
