package com.jembranakab.simas.ui.general.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.jembranakab.simas.R
import com.jembranakab.simas.base.BaseDialogFragment

class ConfirmDialogFragment : BaseDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val bundle = arguments?.getBundle("bundle")
            // Set the dialog title
            builder.setTitle(arguments?.getString("header"))
                // Set the action buttons
                .setPositiveButton(R.string.iya) { _, _ ->
                    listener.onDialogPositiveClick(this, arguments?.getInt("eventCode"), bundle)
                }
                .setNegativeButton(R.string.batal) { _, _ ->
                    listener.onDialogNegativeClick(this)
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        fun newInstance(): ConfirmDialogFragment {
            return ConfirmDialogFragment()
        }
    }

}