package com.jembranakab.simas.base

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

abstract class BaseFragment : Fragment() {

    fun toastThis(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun toastError(e: Throwable?) {
        val error = e?.message
        Toast.makeText(
                context,
                "An Error Has Occurred: ${error ?: "Something Wrong"}",
                Toast.LENGTH_SHORT
        ).show()
    }

    fun snackShort(text: String?) {
        Snackbar.make(
                requireView(),
                text.toString(),
                Snackbar.LENGTH_SHORT
        ).show()
    }

    fun snackLong(text: String?) {
        Snackbar.make(
                requireView(),
                text.toString(),
                Snackbar.LENGTH_LONG
        ).show()
    }

    companion object {
        const val PICK_PDF = 50
        const val REQUEST_PERMISSION = 40
        const val RESULT_OK = 21
    }

}