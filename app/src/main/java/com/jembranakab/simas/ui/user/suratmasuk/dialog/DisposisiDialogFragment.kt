package com.jembranakab.simas.ui.user.suratmasuk.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.jembranakab.simas.R
import com.jembranakab.simas.base.BaseDialogFragment
import com.jembranakab.simas.model.entities.Organisasi
import com.jembranakab.simas.model.entities.SuratDisposisi
import com.jembranakab.simas.model.entities.SuratOpd
import com.jembranakab.simas.ui.user.suratmasuk.adapter.KepadaAdapter
import com.jembranakab.simas.utilities.App
import com.jembranakab.simas.utilities.DataConverter
import kotlinx.android.synthetic.main.user_disposisi_dialog.view.*

class DisposisiDialogFragment : BaseDialogFragment(), AdapterView.OnItemSelectedListener {

    private lateinit var kepadaAdapter: KepadaAdapter
    private lateinit var dView: View
    private var bawahan = arrayListOf<Organisasi>()
    private var instruksi = ""

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val suratOpd = requireArguments().getSerializable("suratOpd") as SuratOpd?
            val suratDisposisi = requireArguments().getSerializable("suratDisposisi") as SuratDisposisi?
            val builder = AlertDialog.Builder(it)
            dView = layoutInflater.inflate(R.layout.user_disposisi_dialog, null)
            builder.setTitle(getString(R.string.disposisi))
            builder.setView(dView)

            bawahan = requireArguments().getParcelableArrayList<Organisasi>("bawahan") as ArrayList<Organisasi>
            kepadaAdapter = KepadaAdapter(bawahan)
            if (suratOpd != null) {
                with(dView) {
                    kepada_rcv.adapter = kepadaAdapter
                    kepada_rcv.layoutManager = LinearLayoutManager(context)
                    kepada_rcv.setHasFixedSize(true)
                    nomor_et.setText(suratOpd.getSurat()!!.nomor)
                    isi_ringkas_et.setText(suratOpd.getSurat()!!.perihal)

                    // kepada spinner
                    ArrayAdapter.createFromResource(
                            requireContext(),
                            R.array.instruksi,
                            android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
                        instruksi_spinner.adapter = adapter
                        instruksi_spinner.onItemSelectedListener = this@DisposisiDialogFragment
                    }
                }

                builder.setPositiveButton(getString(R.string.simpan)) {_, _ ->
                    val kepada = arrayListOf<Int>()
                    for (i in 0 until kepadaAdapter.itemCount) {
                        val item = kepadaAdapter.getItem(i)
                        if (item.isChecked) {
                            item.isChecked = false
                            kepada.add(item.unit!!)
                        }
                    }

                    val suratDisposisiConverted = DataConverter.convertToSuratDisposisi(suratOpd)
                    suratDisposisiConverted.isiRingkas = dView.isi_ringkas_et.text.toString()
                    suratDisposisiConverted.tambahanInstruksi = dView.tambahan_instruksi_et.text.toString()
                    suratDisposisiConverted.instruksi = instruksi
                    val bundle = Bundle()
                    bundle.putIntegerArrayList("kepada", kepada)
                    bundle.putSerializable("suratDisposisi", suratDisposisiConverted)
                    listener.onDialogPositiveClick(this, App.DISPOSISI, bundle)
                }.setNegativeButton(getString(R.string.batal)) {_, _ ->
                    listener.onDialogNegativeClick(this)
                }
            } else if (suratDisposisi != null){
                with(dView) {
                    edit_disposisi_tv.visibility = View.VISIBLE
                    kepada_rcv.adapter = kepadaAdapter
                    kepada_rcv.layoutManager = LinearLayoutManager(context)
                    kepada_rcv.setHasFixedSize(true)
                    nomor_et.setText(suratDisposisi.getSurat()!!.nomor)
                    isi_ringkas_et.setText(suratDisposisi.getSurat()!!.perihal)
                    tambahan_instruksi_et.setText(suratDisposisi.tambahanInstruksi)

                    // kepada spinner
                    ArrayAdapter.createFromResource(
                            requireContext(),
                            R.array.instruksi,
                            android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
                        instruksi_spinner.adapter = adapter
                        instruksi_spinner.onItemSelectedListener = this@DisposisiDialogFragment
                    }
                    val arrayInstruksi = resources.getStringArray(R.array.instruksi)
                    for (i in arrayInstruksi.indices) {
                        if (arrayInstruksi[i] == suratDisposisi.instruksi) {
                            instruksi_spinner.setSelection(i)
                            break
                        }
                    }
                }

                builder.setPositiveButton(getString(R.string.simpan)) {_, _ ->
                    val kepada = arrayListOf<Int>()
                    for (i in 0 until kepadaAdapter.itemCount) {
                        val item = kepadaAdapter.getItem(i)
                        if (item.isChecked) {
                            item.isChecked = false
                            kepada.add(item.unit!!)
                        }
                    }

                    suratDisposisi.isiRingkas = dView.isi_ringkas_et.text.toString()
                    suratDisposisi.tambahanInstruksi = dView.tambahan_instruksi_et.text.toString()
                    suratDisposisi.instruksi = instruksi
                    val bundle = Bundle()
                    bundle.putIntegerArrayList("kepada", kepada)
                    bundle.putSerializable("suratDisposisi", suratDisposisi)
                    listener.onDialogPositiveClick(this, App.DISPOSISI, bundle)
                }.setNegativeButton(getString(R.string.batal)) {_, _ ->
                    listener.onDialogNegativeClick(this)
                }
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        if (p0?.id == dView.instruksi_spinner.id)
            instruksi = p0.getItemAtPosition(p2).toString()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    override fun onDestroyView() {
        super.onDestroyView()
        kepadaAdapter.setAllCheck(false)
    }

    companion object {

        fun newInstance(): DisposisiDialogFragment {
            return DisposisiDialogFragment()
        }

    }

}