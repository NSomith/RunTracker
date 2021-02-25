package com.example.runtracker.utils

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import com.example.runtracker.service.Polyline
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

object TrackingUtility {

    fun hasLocationPermissio(context:Context)=
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                EasyPermissions.hasPermissions(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            }else{
                EasyPermissions.hasPermissions(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )

            }


    fun formattedStopWatchTime(ms:Long,includeMills:Boolean = false): String {
        var millisec = ms
        val hours = TimeUnit.MILLISECONDS.toHours(millisec)
        millisec -=TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisec)
        millisec -= TimeUnit.MINUTES.toMillis(minutes)
        val sec = TimeUnit.MILLISECONDS.toSeconds(millisec)
        if(!includeMills){
            return "${if(hours<10) "0" else ""}$hours:"+
                    "${if(minutes<10) "0" else ""}$minutes:"+
                    "${if(sec<10) "0" else ""}$sec"
        }
        millisec -= TimeUnit.SECONDS.toMillis(sec)
        millisec /=10 //since we need only two digit no
        return "${if(hours<10) "0" else ""}$hours:"+
                "${if(minutes<10) "0" else ""}$minutes:"+
                "${if(sec<10) "0" else ""}$sec:"+
                "${if(millisec<10) "0" else ""}$millisec"
    }

    fun calculatePolylineDistance(polyline: Polyline):Float{
        var distance = 0f
        for(i in 0..polyline.size-2){
            val pos1 = polyline[i]
            val pos2 = polyline[i+1]

            val result = FloatArray(1)
            Location.distanceBetween(
                    pos1.latitude,
                    pos1.longitude,
                    pos2.latitude,
                    pos2.longitude,
                    result
            )
            distance +=result[0]
        }
        return distance
    }
}