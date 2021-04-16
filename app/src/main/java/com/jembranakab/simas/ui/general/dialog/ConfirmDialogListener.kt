package com.jembranakab.simas.ui.general.dialog

import android.os.Bundle
import androidx.fragment.app.DialogFragment

interface ConfirmDialogListener {
    /**
     *
     * Event Code
     * Code 1 = Logout
     * Code 2 = Exit App
     * Code 3 = Batal Tambah Surat
     * Code 4 = Disposisi
     * Code 5 = Koreksi
     * Code 6 = Kirim
     * Code 7 = Status Detail
     *
     **/
    fun onDialogPositiveClick(dialog: DialogFragment, eventCode: Int?, bundle: Bundle?)
    fun onDialogNegativeClick(dialog: DialogFragment)
}