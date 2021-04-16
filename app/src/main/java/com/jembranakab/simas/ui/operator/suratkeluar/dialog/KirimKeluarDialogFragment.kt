package com.jembranakab.simas.ui.operator.suratkeluar.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.jembranakab.simas.R
import com.jembranakab.simas.base.BaseDialogFragment
import com.jembranakab.simas.model.JsonOrg
import com.jembranakab.simas.model.entities.Organisasi
import com.jembranakab.simas.model.entities.SuratOpd
import com.jembranakab.simas.ui.general.adapter.CheckboxCallback
import com.jembranakab.simas.ui.operator.suratkeluar.adapter.TeruskanAdapter
import com.jembranakab.simas.utilities.App.Companion.KIRIM_ARSIP_KELUAR
import com.jembranakab.simas.utilities.DataConverter
import kotlinx.android.synthetic.main.list_view.view.*
import kotlinx.android.synthetic.main.operator_header_list_kirim_keluar.view.*

class KirimKeluarDialogFragment : BaseDialogFragment() {

    private lateinit var adapter: TeruskanAdapter
    private var arrayTeruskan = arrayListOf<Organisasi>()

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val args = requireArguments().getSerializable("suratOpd") as SuratOpd
            val builder = AlertDialog.Builder(it)
            val dialogView = layoutInflater.inflate(R.layout.list_view, null)
            val headerListView = layoutInflater.inflate(R.layout.operator_header_list_kirim_keluar, null)
            builder.setView(dialogView)

            with(headerListView) {
                kategori_et.setText(DataConverter.getKategori(args.getSurat()!!.kategori!!))
                perincian_et.setText(args.getSurat()!!.perincian!!)
                nomor_et.setText(args.getSurat()!!.nomor)
                kepada_et.setText(DataConverter.getNamaOrgFromTopLayer(args.getSurat()!!.kepada!!))
                dari_et.setText(DataConverter.getNamaOrgFromTopLayer(args.getSurat()!!.dari!!))
                tanggal_et.setText(args.getSurat()!!.tanggal)
                perihal_et.setText(args.getSurat()!!.perihal)
                lampiran_et.setText(args.getSurat()!!.lampiran)
                file_et.setText(args.getSurat()!!.namaPdf)
            }

            with(dialogView) {
                progressBar.hide()
                listView.addHeaderView(headerListView)
                arrayTeruskan.addAll(
                        JsonOrg.getAllTopExcepts(args.getSurat()!!.dari!!, args.getSurat()!!.kepada!!)
                )
                adapter = TeruskanAdapter(context, arrayTeruskan, object : CheckboxCallback {

                    override fun setHeaderCheckbox(check: Boolean) {
                        headerListView.header_checkbox_item.isChecked = check
                    }

                    override fun checkCountCheck() {
                        for (i in 0 until adapter.count) {
                            if (!adapter.getItem(i).isChecked) {
                                return
                            }
                        }
                        headerListView.header_checkbox_item.isChecked = true
                    }

                })

                listView.adapter = adapter

                headerListView.pilih_semua_container.setOnClickListener {
                    header_checkbox_item.isChecked = !header_checkbox_item.isChecked
                    if (!header_checkbox_item.isChecked) {
                        for (org in arrayTeruskan) {
                            org.isChecked = false
                        }
                        adapter.notifyDataSetChanged()
                    }
                }

                headerListView.header_checkbox_item.setOnClickListener {
                    if (!header_checkbox_item.isChecked) {
                        for (org in arrayTeruskan) {
                            org.isChecked = false
                        }
                        adapter.notifyDataSetChanged()
                    }
                }

                headerListView.header_checkbox_item.setOnCheckedChangeListener { _, b ->
                    if (b) {
                        for (org in arrayTeruskan) {
                            org.isChecked = true
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
            }

            builder.setPositiveButton(R.string.kirim) { _, _ ->
                val terusan = arrayListOf<Int>()
                for (i in 0 until adapter.count) {
                    val item = adapter.getItem(i)
                    if (item.isChecked) {
                        item.isChecked = false
                        terusan.add(item.unit!!)
                    }
                }
                val bundleKirim = Bundle()
                bundleKirim.putIntegerArrayList("terusan", terusan)
                bundleKirim.putSerializable("suratUtama", requireArguments().getSerializable("suratOpd"))
                listener.onDialogPositiveClick(this, KIRIM_ARSIP_KELUAR, bundleKirim)

            }.setNegativeButton(R.string.batal) { _, _ ->
                listener.onDialogNegativeClick(this)
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        arrayTeruskan.clear()
    }

    companion object {

        fun newInstance(): KirimKeluarDialogFragment {
            return KirimKeluarDialogFragment()
        }

    }

}