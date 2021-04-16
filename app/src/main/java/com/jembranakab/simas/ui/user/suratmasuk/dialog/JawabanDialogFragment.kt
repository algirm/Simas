package com.jembranakab.simas.ui.user.suratmasuk.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.jembranakab.simas.R
import com.jembranakab.simas.base.BaseDialogFragment
import com.jembranakab.simas.model.entities.SuratDisposisi
import com.jembranakab.simas.utilities.App
import kotlinx.android.synthetic.main.user_jawaban_dialog.view.*

class JawabanDialogFragment : BaseDialogFragment() {

    private lateinit var dView: View

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            dView = layoutInflater.inflate(R.layout.user_jawaban_dialog, null)
            builder.setTitle(getString(R.string.jawaban_disposisi))
            builder.setView(dView)

            val suratDisposisi =
                    requireArguments().getSerializable("suratDisposisi") as SuratDisposisi
            dView.isi_ringkas_et.setText(suratDisposisi.isiRingkas)

            // todo set error when jawaban is blank
            builder.setPositiveButton(R.string.iya) { _, _ ->
                val bundle = Bundle()
                bundle.putSerializable("suratDisposisi", suratDisposisi)
                bundle.putString("jawaban", dView.jawaban_et.text.toString())
                bundle.putInt("thisUnit", requireArguments().getInt("thisUnit"))
                listener.onDialogPositiveClick(this, App.JAWABAN, bundle)
            }.setNegativeButton(R.string.batal) { _, _ ->
                listener.onDialogNegativeClick(this)
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        fun newInstance(): JawabanDialogFragment {
            return JawabanDialogFragment()
        }
    }

}