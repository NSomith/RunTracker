package com.example.runtracker.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.runtracker.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDialogue : DialogFragment() {

    private var yesListener:(()->Unit)?= null
    fun setyesListener(listener:()->Unit){
        yesListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Cancle the Run")
                .setMessage("Are you sure to cancel the current run and delete the data")
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton("Yes"){_,_ ->
                    yesListener?.let {
                        it()
                    }
                }
                .setNegativeButton("No"){dialogInterface,_->
                    dialogInterface.cancel()
                }
                .create()
    }
}