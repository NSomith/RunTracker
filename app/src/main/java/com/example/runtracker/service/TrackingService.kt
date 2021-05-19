package com.example.runtracker.service

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
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.runtracker.MainActivity
import com.example.runtracker.R
import com.example.runtracker.utils.Constants.ACTION_PAUSE_SERVICE
import com.example.runtracker.utils.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runtracker.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runtracker.utils.Constants.ACTION_STOP_SERVICE
import com.example.runtracker.utils.Constants.FASTEST_LOCATION_INTERVAL
import com.example.runtracker.utils.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runtracker.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runtracker.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runtracker.utils.Constants.NOTIFICATION_ID
import com.example.runtracker.utils.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService :LifecycleService() {

    var isRunning = true
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var currentNotificationBuilder:NotificationCompat.Builder

    val timeRunInSec = MutableLiveData<Long>() //life data for notification

    private var isTimerEnable = false
    private var laptime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer(){
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnable = true
//        how to track the current time or stop the current time ?? we dont want to call the observer all the time so use coroutine
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){
                laptime = System.currentTimeMillis() - timeStarted //timediff betn now and time started
                timeRunInMills.postValue(timeRun + laptime) // post the new lapTime
                if(timeRunInMills.value!! >= lastSecondTimeStamp+1000L){
                    timeRunInSec.postValue(timeRunInSec.value!! + 1)
                    lastSecondTimeStamp +=1000L
                }
                delay(50L)
            }
            timeRun +=laptime
        }
    }

    companion object{
        val isTracking = MutableLiveData<Boolean>()
//        val pathpoints = MutableLiveData<MutableList<MutableList<LatLng>>>()
        val pathpoints = MutableLiveData<Polylines>()

        val timeRunInMills = MutableLiveData<Long>() //life data for Tracking fragment
    }

//    TODO
    override fun onCreate() {
        super.onCreate()
//        fusedLocationProviderClient = FusedLocationProviderClient(this)
    currentNotificationBuilder = baseNotificationBuilder
        postIntialValue()
//        whenever our istracking is chaging we want to observe it
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

//TODO chek why do we need this postinitvalue
    
//    initlai our live data since we r not doing anything
    private fun postIntialValue(){
        isTracking.postValue(false)
        pathpoints.postValue(mutableListOf()) //assingning inital value to empty list
        timeRunInSec.postValue(0L)
        timeRunInMills.postValue(0L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE->{
                    if(isRunning){
                        startForegroundService()
                        isRunning = false
                    }else{
                        Timber.d("Runnign service")
                        startTimer()
                    }
                    Timber.d("start or resume")
                }
                ACTION_PAUSE_SERVICE->{
                    Timber.d("pause")
                    pauseService()
                }
                ACTION_STOP_SERVICE->{
                    Timber.d("stop")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnable =false
    }

    private fun updateNotificationTrackingState(istracking: Boolean){
        val notificationAction = if(istracking) "Pause" else "Resume"
        val pendingIntent = if(istracking){ //this intent when called trigger the onStartCommand() func
            val pauseIntent = Intent(this,TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this,1,pauseIntent, FLAG_UPDATE_CURRENT)
        }else{
            val resumeIntent = Intent(this,TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this,2,resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManger = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

//        below is used to updat the same notification 
        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder,ArrayList<NotificationCompat.Action>())
        }

        if(!serviceKilled){
            currentNotificationBuilder = baseNotificationBuilder
                    .addAction(R.drawable.ic_pause_black_24dp,notificationAction,pendingIntent)
            notificationManger.notify(NOTIFICATION_ID,currentNotificationBuilder.build())
        }

    }



//    TODO
//    add polyline to our list
    private fun addEmptyPolyline() = pathpoints.value?.apply {
    add(mutableListOf()) //scenario when we pause
    pathpoints.postValue(this) //add the changes in the live data }
    }
     ?: pathpoints.postValue(mutableListOf(mutableListOf())) //if the value is null then we want
    // to post the inital polyline



//    now to add latlong in the last location of the polyline
    private fun addPathPoints(location:Location?){
        location?.apply {
            val pos = LatLng(location.latitude,location.longitude)
            pathpoints.value?.apply {
                last().add(pos) //meas the last polyline in our pathpoint list
                pathpoints.postValue(this) //since we have mad e changes so psot the value
            }
        }
    }

    private fun killService(){
        serviceKilled = true
        isRunning = true
        pauseService()
        postIntialValue()
        stopForeground(true)
        stopSelf() //stop the whole service
    }

    @SuppressLint("MissingPermission") //since the easy is not part of real permission so we supressLint
    private fun updateLocationTracking(istracking:Boolean){
        if(istracking){
            if(TrackingUtility.hasLocationPermissio(this)){ //we already did the permission check with easy library
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                        request,
                        locationCallback,
                        Looper.getMainLooper()
                )
            }
        }else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

//    when we retrive new location we just add to the polyline
    val locationCallback = object : LocationCallback(){
        @SuppressLint("LogNotTimber")
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if(isTracking.value!!){ //means we are currently tracking
                result?.locations?.let {
                    for (loc in it){
                        addPathPoints(loc)
                        Log.d("loc","${loc.latitude},: ${loc.longitude}")
                    }
                }
            }
        }
    }


    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)
        val notificationmanger = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createnotificationchannel(notificationmanger)
        }
/*
//        now create the actual notification
        val notificationBuilder = NotificationCompat
                .Builder(this, NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false) //always want the notification to be active even we have clicked also
                .setOngoing(true) //meaning that it cannot be swiped away
                .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
                .setContentText("RunTracker")
                .setContentText("00:00:00")
                .setContentIntent(getMainActivityPendingIntent())

 */

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build()) //tells it is a foreground setvice with the id and the pending intent

        if (!serviceKilled) {
            timeRunInSec.observe(this, Observer {
                val notification = currentNotificationBuilder
                        .setContentText(TrackingUtility.formattedStopWatchTime(it * 1000L))
                notificationmanger.notify(NOTIFICATION_ID, notification.build())
            })
        }
    }

/*
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
            this,
            0,
            Intent(this,MainActivity::class.java).also {
                it.action = ACTION_SHOW_TRACKING_FRAGMENT
            },
            FLAG_UPDATE_CURRENT
    )

 */

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createnotificationchannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}