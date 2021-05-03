package com.jembranakab.simas.ui.user.suratkeluar

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.jembranakab.simas.R
import com.jembranakab.simas.base.BaseDialogFragment
import com.jembranakab.simas.model.entities.DraftSurat
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.KOREKSI
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.SETUJU
import com.jembranakab.simas.utilities.DataConverter
import kotlinx.android.synthetic.main.user_koreksi_dialog.view.*

class KoreksiDialogFragment : BaseDialogFragment() {

    private lateinit var dv: View

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val draftSurat = requireArguments().getSerializable("draftSurat") as DraftSurat
            val tipe = requireArguments().getInt("tipe")
            val thisUnit = requireArguments().getInt("thisUnit")
            val builder = AlertDialog.Builder(it)
            dv = layoutInflater.inflate(R.layout.user_koreksi_dialog, null)
            builder.setView(dv)
            builder.setTitle(requireArguments().getString("header"))

            val thisUnitCharArray = thisUnit.toString().toCharArray()
            // top unit
            if (thisUnitCharArray.size < 3) {
                dv.containerBawah.visibility = View.GONE
            } else {
                dv.containerBawah.visibility = View.VISIBLE
                val atasan = DataConverter.getAtasanUnitExceptTopUnit(thisUnit)
                dv.etDiajukanKepada.setText(DataConverter.getNamaOrgFromAll(atasan))
            }
            val positiveButtonText = if (tipe == KOREKSI) {
                dv.containerBawah.visibility = View.GONE
                dv.etKeterangan.setText(draftSurat.keterangan)
                "Koreksi"
            } else {
                "Setuju"
            }

            builder.setPositiveButton(positiveButtonText) { _, _ ->
                if (dv.etKeterangan.text.isNotBlank()) {
                    draftSurat.keterangan = dv.etKeterangan.text.toString()
                }
                val eventCode = if (tipe == KOREKSI) KOREKSI else SETUJU
                val bundle = Bundle()
                bundle.putSerializable("draftSurat", draftSurat)
                listener.onDialogPositiveClick(this, eventCode, bundle)
            }.setNegativeButton(getString(R.string.batal)) {_, _ ->
                listener.onDialogNegativeClick(this)
            }

            builder.create()
        }?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        fun newInstance(): KoreksiDialogFragment {
            return KoreksiDialogFragment()
        }
    }

}