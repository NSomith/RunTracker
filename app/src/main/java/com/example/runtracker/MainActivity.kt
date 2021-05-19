package com.example.runtracker

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runtracker.utils.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runtracker.utils.Constants.KEY_NAME
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        tvToolbarTitle.text = "Lets go ${sharedPreferences.getString(KEY_NAME,"")}"

        navigateToTrackingFragmentIfNeeded(intent) //calls when the activity is destroyed

        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        bottomNavigationView.setOnNavigationItemReselectedListener {
        /*no operation is performed so as to disable the same item selected */
        }

        //not showing bottom navigation in some screen
        navHostFragment.findNavController()
                .addOnDestinationChangedListener { _, destination, _ ->
                    when(destination.id){
                       R.id.settingFragment,R.id.runFragment,R.id.statisticFragment->
                        {
                            bottomNavigationView.visibility = View.VISIBLE
                        }
                     else->bottomNavigationView.visibility =View.GONE
                    }
                }
    }

//    calls when the when the activity is already created and from our service notification
//    we want to go at the main tracking screen
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    Log.d("ttt","called newIntent")
        navigateToTrackingFragmentIfNeeded(intent)
    }

//  calls when we click the notification not destroy no nothing
    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?){
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT){
            navHostFragment.findNavController().navigate(R.id.action_global_tracking_fragment)
        }
    }
}