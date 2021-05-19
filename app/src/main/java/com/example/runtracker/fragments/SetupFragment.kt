package com.example.runtracker.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.runtracker.R
import com.example.runtracker.utils.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runtracker.utils.Constants.KEY_NAME
import com.example.runtracker.utils.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment:Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @set:Inject //since this is a primitive data type means it can change
    var isFirstAppOpen = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!isFirstAppOpen){
            val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.setupFragment,true)
                    .build()
            findNavController()
                    .navigate(R.id.action_setupFragment_to_runFragment,
                            savedInstanceState,navOptions)
        }

        tvContinue.setOnClickListener {
            val success = writedPersonalDateToSharedpref()
            if(success){
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            }else{
                Snackbar.make(requireView(),"Please enter the fields",Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun writedPersonalDateToSharedpref():Boolean{
        val name = etName.text.toString()
        val weight = etWeight.text.toString()
        if(name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPreferences.edit()
                .putString(KEY_NAME,name)
                .putFloat(KEY_WEIGHT,weight.toFloat())
                .putBoolean(KEY_FIRST_TIME_TOGGLE,false) //tell that this is the first time of the user
                .apply() //asyncronous
//                .commit() //synchornous
        val toolbar = "Let go ${name}"
        requireActivity().tvToolbarTitle.text = toolbar
        return true
    }
}