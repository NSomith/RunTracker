package com.example.runtracker.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.runtracker.R
import com.example.runtracker.utils.Constants
import com.example.runtracker.utils.Constants.KEY_NAME
import com.example.runtracker.utils.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*

import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment:Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadFieldFromSharedpref()
        btnApplyChanges.setOnClickListener {
            val success = applychangestoSharedpref()
            if(success){
                Snackbar.make(view,"Saved",Snackbar.LENGTH_SHORT).show()
            }else{
                Snackbar.make(view,"Fill out the fields",Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadFieldFromSharedpref(){
        val name = sharedPreferences.getString(KEY_NAME,"")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT,50f)
        etName.setText(name)
        etWeight.setText(weight.toString())
    }

    private fun applychangestoSharedpref():Boolean{
        val name = etName.text.toString()
        val weight = etWeight.text.toString()
        if(name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPreferences.edit()
                .putString(KEY_NAME,name)
                .putFloat(KEY_WEIGHT,weight.toFloat())
                .apply()
        val toolbar = "Let go ${name}"
        requireActivity().tvToolbarTitle.text = toolbar
        return true
    }
}