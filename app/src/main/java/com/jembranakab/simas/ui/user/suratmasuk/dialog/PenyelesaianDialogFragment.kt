package com.jembranakab.simas.ui.user.suratmasuk.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.google.firebase.Timestamp
import com.jembranakab.simas.R
import com.jembranakab.simas.base.BaseDialogFragment
import com.jembranakab.simas.model.entities.JawabanDisposisi
import com.jembranakab.simas.model.entities.SuratDisposisi
import com.jembranakab.simas.utilities.App
import com.jembranakab.simas.utilities.DataConverter
import kotlinx.android.synthetic.main.user_penyelesaian_dialog.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class PenyelesaianDialogFragment : BaseDialogFragment() {

    private lateinit var dv: View

    private var pdfUrl = ""

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val suratDisposisi = requireArguments().getSerializable("suratDisposisi") as SuratDisposisi

            dv = layoutInflater.inflate(R.layout.user_penyelesaian_dialog, null)
            builder.setView(dv)
            builder.setTitle(getString(R.string.penyelesaian))

            with(dv) {
                dari_detail_disposisi.text = DataConverter.getNamaOrgFromAll(suratDisposisi.from_unit!!)
                tanggal_tv.text =
                    SimpleDateFormat("dd MMM yyyy", Locale.ROOT)
                        .format(suratDisposisi.last_modified!!.toDate())
                nomor_surat_et.text = suratDisposisi.getSurat()!!.nomor
                keterangan_tv.text = suratDisposisi.isiRingkas
                if (suratDisposisi.jawaban == null) {
                    jawaban_et.isEnabled = true
                } else {
                    jawaban_et.isEnabled = false
                    jawaban_et.setText(suratDisposisi.jawaban!!.jawaban)
                }
            }

            builder.setPositiveButton(getString(R.string.simpan)) { _, _ ->
                val bundle = Bundle()
                val map = HashMap<String, String>()
                map["keterangan"] = dv.keterangan_et.text.toString()
                map["pdfUrl"] = pdfUrl
                if (suratDisposisi.jawaban == null) {
                    val jd = JawabanDisposisi()
                    jd.jawaban = dv.jawaban_et.text.toString()
                    jd.waktuJawab = Timestamp.now()
                    jd.id = suratDisposisi.id
                    suratDisposisi.jawaban = jd
                }
                suratDisposisi.jawaban!!.penyelesaian = map
                bundle.putSerializable("suratDisposisi", suratDisposisi)
                listener.onDialogPositiveClick(this, App.PENYELESAIAN, bundle)
            }.setNegativeButton(getString(R.string.batal)) {_, _ ->
                listener.onDialogNegativeClick(this)
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {

        fun newInstance(): PenyelesaianDialogFragment {
            return PenyelesaianDialogFragment()
        }

    }

}