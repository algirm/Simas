package com.jembranakab.simas.base

import android.content.Context
import androidx.fragment.app.DialogFragment
import com.jembranakab.simas.ui.general.dialog.ConfirmDialogListener

abstract class BaseDialogFragment : DialogFragment() {

    lateinit var listener: ConfirmDialogListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as ConfirmDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement ConfirmDialogListener"))
        }

    }

}