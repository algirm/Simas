package com.jembranakab.simas.base

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

open class BaseActivity : AppCompatActivity() {

    fun toastThis(text: String?) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    fun toastError(e: Throwable?) {
        val error = e?.message
        Toast.makeText(
            this,
            "An Error Has Occurred: ${error ?: "Something Wrong"}",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun snackShort(text: String?) {
        Snackbar.make(
                findViewById(android.R.id.content),
                text.toString(),
                Snackbar.LENGTH_SHORT
        ).show()
    }

    fun snackLong(text: String?) {
        Snackbar.make(
                findViewById(android.R.id.content),
                text.toString(),
                Snackbar.LENGTH_LONG
        ).show()
    }

}