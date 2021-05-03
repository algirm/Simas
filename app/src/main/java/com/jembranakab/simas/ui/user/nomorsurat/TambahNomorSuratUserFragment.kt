package com.jembranakab.simas.ui.user.nomorsurat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.jembranakab.simas.R
import com.jembranakab.simas.base.BaseFragment
import com.jembranakab.simas.model.JsonOrg
import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.DraftSurat
import com.jembranakab.simas.model.entities.Organisasi
import com.jembranakab.simas.model.entities.Surat
import com.jembranakab.simas.model.viewmodel.SuratOpdViewModel
import com.jembranakab.simas.ui.operator.suratkeluar.adapter.KepadaAdapter
import com.jembranakab.simas.utilities.App
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.BELUM_PROSES
import com.jembranakab.simas.utilities.DataConverter
import kotlinx.android.synthetic.main.user_tambah_nomorsurat.*


class TambahNomorSuratUserFragment : BaseFragment(), AdapterView.OnItemSelectedListener {

    private val suratOpdVM by activityViewModels<SuratOpdViewModel>()
    private var thisUnit: Int? = null

    private lateinit var tujuanAdapter: KepadaAdapter

    // local variable
    private var arrayTujuan = arrayListOf<Organisasi>()
    private var kategori: Int? = null
    private var perincian: String? = null
    private var tujuan: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.user_tambah_nomorsurat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initSpinner()
    }

    private fun tambahNomorSurat() {
        if (isDataSet()) {
            toastThis(getString(R.string.isi_semua_data))
            return
        }

        val surat = Surat()
        surat.createBy = thisUnit
        surat.createdAt = Timestamp.now()
        surat.kategori = kategori
        surat.perincian = perincian
        surat.nomor = nomor_et.text.toString()
        surat.kepada = tujuan
        surat.dari = DataConverter.getOrgTopOfThisUnit(thisUnit!!)
        surat.tanggal = tanggal_et.text.toString()
        surat.perihal = perihal_et.text.toString()
        surat.lampiran = ""
        surat.namaDinasLuar =
                if (surat.kepada == 99) {
                    nama_instansi_luar_et.text.toString()
                } else {
                    ""
                }

        val draftSurat = DraftSurat(
            asalDraft = thisUnit,
            surat = surat,
            pengirim = thisUnit,
            lastModified = Timestamp.now(),
            status = BELUM_PROSES
        )

        suratOpdVM.tambahNomorSurat(draftSurat)
        suratOpdVM.resultTambahNomor.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Loading -> tambah_progress_bar.show()
                is Resource.Success -> {
                    suratOpdVM.resultTambahNomor.removeObservers(this)
                    snackLong(getString(R.string.berhasil_tambah))
                    suratOpdVM.getNomorSurat(thisUnit!!)
                    findNavController().navigateUp()
                    tambah_progress_bar.hide()
                }
                is Resource.Failure -> {
                    toastError(it.throwable)
                    suratOpdVM.resultTambahNomor.removeObservers(this)
                    tambah_progress_bar.hide()
                }
            }
        })

    }

    private fun isDataSet(): Boolean {
        var kosong = false
        if (nomor_et.text.isBlank()) {
            nomor_et.error = getString(R.string.harus_diisi)
            kosong = true
        }
        if (nama_instansi_luar_container.visibility == View.VISIBLE
                && nama_instansi_luar_et.text.isBlank()) {
            nama_instansi_luar_et.error = getString(R.string.harus_diisi)
            kosong = true
        }
        if (tanggal_et.text.isBlank()) {
            tanggal_et.error = getString(R.string.harus_diisi)
            kosong = true
        } else {
            val regex = Regex(pattern = """\d{2}-\d{2}-\d{4}""")
            val matched = regex.matches(input = tanggal_et.text)
            if (!matched) {
                tanggal_et.error = getString(R.string.sesuaikan_format_tanggal)
                kosong = true
            }
        }
        if (perihal_et.text.isBlank()) {
            perihal_et.error = getString(R.string.harus_diisi)
            kosong = true
        }

        return kosong
    }

    private fun initSpinner() {
        // init Array Data
        for (data in JsonOrg.getAllTopWithDinasLuarExcept(DataConverter.getOrgTopOfThisUnit(thisUnit!!))) {
            arrayTujuan.add(data)
        }

        // kategori spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.kategori_surat,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
            kategori_spinner.adapter = adapter
            kategori_spinner.onItemSelectedListener = this
        }

        // perincian spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.perincian_surat,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            perincian_spinner.adapter = adapter
            perincian_spinner.onItemSelectedListener = this
        }

        // kepada spinner
        tujuanAdapter = KepadaAdapter(requireContext(), arrayTujuan)
        tujuan_spinner.adapter = tujuanAdapter
        tujuan_spinner.onItemSelectedListener = this

        tujuan_spinner.setSelection(2)
    }

    private fun init() {
        thisUnit =
            findNavController().currentDestination!!.arguments["thisUnit"]?.defaultValue as Int
        button_simpan.setOnClickListener { tambahNomorSurat() }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        when (p0?.id) {
            kategori_spinner.id -> kategori = p2
            perincian_spinner.id -> perincian = p0.getItemAtPosition(p2).toString()
            tujuan_spinner.id -> {
                tujuan = tujuanAdapter.getItem(p2).unit
                if (tujuan == 99) {
                    nama_instansi_luar_container.visibility = View.VISIBLE
                } else {
                    nama_instansi_luar_container.visibility = View.GONE
                    nama_instansi_luar_et.error = null
                }
            }
        }

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        toastThis("Harap Input Semua Data")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (suratOpdVM.resultTambahNomor.hasObservers()) {
            suratOpdVM.resultTambahNomor.removeObservers(this)
        }
    }

}