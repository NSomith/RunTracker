package com.example.runtracker.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runtracker.R
import com.example.runtracker.adapters.RunAdapter
import com.example.runtracker.utils.Constants.REQUEST_CODE_LOC
import com.example.runtracker.utils.SortType
import com.example.runtracker.utils.TrackingUtility
import com.example.runtracker.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunFragment:Fragment(R.layout.fragment_run),EasyPermissions.PermissionCallbacks{

    private val mainViewModel:MainViewModel by viewModels()
    private lateinit var runAdapter: RunAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("viewmodels","address is Runfrag is $mainViewModel")
        requestPermission()
        setupRecylerview()
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }

        when(mainViewModel.sortType){
            SortType.Date->spFilter.setSelection(0)
            SortType.Running_Time->spFilter.setSelection(1)
            SortType.Distance->spFilter.setSelection(2)
            SortType.Average_Speed->spFilter.setSelection(3)
            SortType.Calories_Burned->spFilter.setSelection(4)
        }

        spFilter.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position){
                    0 -> mainViewModel.sortType(SortType.Date)
                    1 -> mainViewModel.sortType(SortType.Running_Time)
                    2 -> mainViewModel.sortType(SortType.Distance)
                    3 -> mainViewModel.sortType(SortType.Average_Speed)
                    4 -> mainViewModel.sortType(SortType.Calories_Burned)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        mainViewModel.runs.observe(viewLifecycleOwner, Observer {
            runAdapter.submitRun(it)
        })
    }

    private fun setupRecylerview() = rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun requestPermission(){
        if(TrackingUtility.hasLocationPermissio(requireContext())){ //required context becz
            // we r inside a fragment and we cannot use activity becz it is null and we want not null
            return
        }
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q){
//            rational is the our own string which will be showed when the user denied the permission
            EasyPermissions.requestPermissions(
                    this,
                    "Need Location Permission to use this app",
                    REQUEST_CODE_LOC,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }else{
            EasyPermissions.requestPermissions(
                    this,
                    "Need Location Permission to use this app",
                    REQUEST_CODE_LOC,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            AppSettingsDialog.Builder(this).build().show()
        }else{
            requestPermission()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }
}