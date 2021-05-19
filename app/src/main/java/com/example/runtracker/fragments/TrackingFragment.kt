package com.example.runtracker.fragments

import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runtracker.R
import com.example.runtracker.adapters.RunAdapter
import com.example.runtracker.db.Run
import com.example.runtracker.service.Polyline
import com.example.runtracker.service.TrackingService
import com.example.runtracker.utils.Constants.ACTION_PAUSE_SERVICE
import com.example.runtracker.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runtracker.utils.Constants.ACTION_STOP_SERVICE
import com.example.runtracker.utils.Constants.CANCEL_DIALOG_TAG
import com.example.runtracker.utils.Constants.MAP_ZOOM
import com.example.runtracker.utils.Constants.POLYLINE_COLOE
import com.example.runtracker.utils.Constants.POLYLINE_WIDTH
import com.example.runtracker.utils.TrackingUtility
import com.example.runtracker.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_run.*
import kotlinx.android.synthetic.main.fragment_tracking.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment:Fragment(R.layout.fragment_tracking) {

    private val mainViewModel: MainViewModel by viewModels()

    private var map:GoogleMap?=null
    private var isTracking = false
    private var pathpoints = mutableListOf<Polyline>()

    private var currentTimeinMills = 0L

    @set:Inject
    var weight = 50f

    private var menu:Menu? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        Log.d("viewmodels","address is trackingfrag is $mainViewModel")


//        used when we rotate the device
        if(savedInstanceState !=null){
            val cancelTrackingDialogue = parentFragmentManager
                    .findFragmentByTag(CANCEL_DIALOG_TAG) as CancelTrackingDialogue?
//            here ? is used to see that the tag we used it finds its value
            cancelTrackingDialogue?.setyesListener {
                stopRun()
            }
        }

        mapView.getMapAsync {
            map = it
            addAllPolyline() //we use it here becz the fragment is created when we rotate the device
        }

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        btnFinishRun?.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunandSaveToDB()
        }

        subscribetoObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu,menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
//        in here we can change the visibility of our item
        if(currentTimeinMills > 0L){
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelTracking->{
                showCancelTrackingDialogue()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialogue(){
        CancelTrackingDialogue().apply {
            setyesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_DIALOG_TAG)
//                }.show(parentFragmentManager,null)
    }

    private fun stopRun() {
        tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun sendCommandToService(action:String){
        Intent()
        Intent(requireContext(),TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

    private fun subscribetoObservers(){
//        viewLifecycleOwner becz we r working with a fragment
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathpoints.observe(viewLifecycleOwner, Observer {
            pathpoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMills.observe(viewLifecycleOwner, Observer {
            currentTimeinMills = it
            val formattedTime = TrackingUtility.formattedStopWatchTime(currentTimeinMills,true)
            tvTimer.text = formattedTime
        })
    }

    private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.builder()
        for(polyline in pathpoints){
            for(pos in polyline){
                bounds.include(pos)
            }
        }
        map?.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                        bounds.build(),
                        mapView.width,
                        mapView.height,
                        (mapView.height * 0.05f).toInt() //for padding in img
                )
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun endRunandSaveToDB(){
        //it takes the screenshot of the map
        map?.snapshot {
            var distanceInMeters = 0
            for(polyline in pathpoints){
                distanceInMeters += TrackingUtility.calculatePolylineDistance(polyline).toInt()
            }
            val avgSpeed = round((distanceInMeters /1000f) / (currentTimeinMills / 1000f/60/60)*10)/10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters/1000f)*weight).toInt()
            val run = Run(it,dateTimeStamp,avgSpeed,distanceInMeters,currentTimeinMills,caloriesBurned)
            mainViewModel.insertRun(run)
            Snackbar.make(
                    requireActivity().findViewById(R.id.rootView),
                    "Run saved",
                    Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }


//  connect the last point with the second last point
    private fun addLatestPolyline(){
        if(pathpoints.isNotEmpty() && pathpoints.last().size >1){
            val secondlastLatLng = pathpoints.last()[pathpoints.last().size - 2]
            val lastLatLng = pathpoints.last().last()
            val polylineoptions = PolylineOptions()
                    .color(POLYLINE_COLOE)
                    .width(POLYLINE_WIDTH)
                    .add(secondlastLatLng)
                    .add(lastLatLng)
            map?.addPolyline(polylineoptions)
        }
    }

//    handel fun when we rotate the devie then the polyline should be present
    private fun addAllPolyline(){
        for(pthpoints in pathpoints){
            val polylineoptions = PolylineOptions()
                    .color(POLYLINE_COLOE)
                    .width(POLYLINE_WIDTH)
                    .addAll(pthpoints)
            map?.addPolyline(polylineoptions)
        }
    }

//    fun to move the camera
    private fun moveCameraToUser(){
        if(pathpoints.isNotEmpty() &&  pathpoints.last().size >1){
            map?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            pathpoints.last().last(),
                            MAP_ZOOM
                    )
            )
        }
    }

    private fun updateTracking(isTracking:Boolean){
        this.isTracking = isTracking
        if(!isTracking && currentTimeinMills > 0L){
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        }else if(isTracking){
            btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            btnFinishRun.visibility = View.GONE
        }
    }

//    now to start or stop our service
    private fun toggleRun(){
        if(isTracking){
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

//  to cache our map
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
    }
//  TODO
/*   override fun onDestroy() {
        super.onDestroy()

        Regarding mapView being null at onDestroy():

        Any view you try to access in the fragment's onDestroy() is going to be null.
            Don't forget that a fragment has 2 lifecycles: the lifecycle of its instance
            (onCreate -> onDestroy) and the lifecycle its view (onViewCreated -> onDestroyView)
            The mapView lifecycle (just like any other view of the fragment)
            is bound to the lifecycle of the fragment's view. Meaning you should call
            mapView.onCreate() in the fragment's onViewCreated() (which you already do) & mapView.onDestroy()
            in the fragment's onDestroyView() (not in onDestroy())
    }
    */
}