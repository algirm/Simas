package com.jembranakab.simas.ui.operator.suratkeluar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jembranakab.simas.R
import com.jembranakab.simas.base.BaseFragment
import com.jembranakab.simas.model.JsonOrg
import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.Organisasi
import com.jembranakab.simas.model.entities.Surat
import com.jembranakab.simas.model.viewmodel.SuratOperatorViewModel
import com.jembranakab.simas.ui.general.dialog.PdfDialogFragment
import com.jembranakab.simas.ui.operator.suratkeluar.adapter.KepadaAdapter
import es.voghdev.pdfviewpager.library.util.FileUtil
import kotlinx.android.synthetic.main.operator_tambah_keluar.*


class TambahArsipKeluarFragment : BaseFragment(), AdapterView.OnItemSelectedListener {

    private val suratOperatorVM by activityViewModels<SuratOperatorViewModel>()
    private var thisUnit: Int? = null

    private lateinit var kepadaAdapter: KepadaAdapter
    private lateinit var pdfDialog: PdfDialogFragment
    private lateinit var mStorageReference: StorageReference

    // local variable
    private var arrayDari = arrayListOf<String>()
    private var arrayKepada = arrayListOf<Organisasi>()
    private var kategori: Int? = null
    private var perincian: String? = null
    private var dari: Int? = null
    private var kepada: Int? = null
    private var pdfUrl: String = ""
    private var namaFile: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.operator_tambah_keluar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initSpinner()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF) {
            if (data?.data != null) {
                uploadFile(data.data!!)
            }
        }
    }

    private fun uploadFile(data: Uri) {
        tambah_progress_bar.show()
        button_pilih_file.visibility = View.GONE
        filename_tv.visibility = View.VISIBLE
        namaFile = "${System.currentTimeMillis()}_${FileUtil.extractFileNameFromURL(data.lastPathSegment)}"
        val sRef: StorageReference = mStorageReference.child(namaFile)
        sRef.putFile(data)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                        tambah_progress_bar.hide()
                        filename_tv.text = namaFile
                        button_tampil.visibility = View.VISIBLE
                        button_pilih_file.visibility = View.GONE
                        val args = Bundle()
                        args.putString("uri", it.toString())
                        args.putString("header", namaFile)
                        pdfDialog.arguments = args
                        pdfUrl = it.toString()
                    }
                }
                .addOnFailureListener { exception ->
                    toastError(exception)
                    tambah_progress_bar.hide()
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                    "${progress.toInt()}% Diunggah...".also { filename_tv.text = it }
                }
    }

    private fun getPDF() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            val permission: Array<String> = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(requireActivity(), permission, REQUEST_PERMISSION)
            return
        }

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Pilih File"), PICK_PDF)
    }

    private fun tambahArsip() {
        if (isDataSet()) {
            tambah_arsip_keluar_scrollview.scrollTo(0, 0)
            toastThis(getString(R.string.isi_semua_data))
            return
        }
        if (pdfUrl.isBlank()) {
            toastThis(getString(R.string.file_belum_upload))
            return
        }

        val surat = Surat()
        surat.createBy = thisUnit
        surat.createdAt = Timestamp.now()
        surat.kategori = kategori
        surat.perincian = perincian
        surat.nomor = nomor_et.text.toString()
        surat.kepada = kepada
        surat.dari = dari
        surat.tanggal = tanggal_et.text.toString()
        surat.perihal = perihal_et.text.toString()
        surat.lampiran = lampiran_et.text.toString()
        surat.pdfUrl = pdfUrl
        surat.namaPdf = namaFile

        suratOperatorVM.tambahArsip(surat, true)
        suratOperatorVM.resultTambah.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Loading -> tambah_progress_bar.show()
                is Resource.Success -> {
                    suratOperatorVM.resultTambah.removeObservers(this)
                    snackLong(getString(R.string.berhasil_tambah))
                    suratOperatorVM.fetchSuratKeluar(thisUnit!!)
                    findNavController().navigateUp()
                    tambah_progress_bar.hide()
                }
                is Resource.Failure -> {
                    toastError(it.throwable)
                    suratOperatorVM.resultTambah.removeObservers(this)
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
        if (lampiran_et.text.isBlank()) {
            lampiran_et.error = getString(R.string.harus_diisi)
            kosong = true
        }

        return kosong
    }

    private fun initSpinner() {
        // init Array Data
        arrayDari.add(JsonOrg.getNamaOrg(thisUnit!!).split("Operator Surat")[1])
        for (data in JsonOrg.getAllTopExcept(thisUnit!!.unaryMinus())) {
            arrayKepada.add(data)
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

        // dari spinner
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            arrayDari
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            dari_spinner.adapter = it
            dari_spinner.onItemSelectedListener = this
        }

        // kepada spinner
        kepadaAdapter = KepadaAdapter(requireContext(), arrayKepada)
        kepada_spinner.adapter = kepadaAdapter
        kepada_spinner.onItemSelectedListener = this

        dari_spinner.isEnabled = false
        kepada_spinner.setSelection(2)
    }

    private fun init() {
        thisUnit =
            findNavController().currentDestination!!.arguments["thisUnit"]?.defaultValue as Int
        mStorageReference = FirebaseStorage.getInstance().reference
        pdfDialog = PdfDialogFragment.newInstance()
        button_simpan.setOnClickListener { tambahArsip() }
        button_pilih_file.setOnClickListener { getPDF() }
        button_tampil.setOnClickListener { pdfDialog.show(childFragmentManager, "pdfDialog") }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        when (p0?.id) {
            kategori_spinner.id -> kategori = p2
            perincian_spinner.id -> perincian = p0.getItemAtPosition(p2).toString()
            dari_spinner.id -> dari = thisUnit!!.unaryMinus()
            kepada_spinner.id -> kepada = kepadaAdapter.getItem(p2).unit
        }

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        toastThis("Harap Input Semua Data")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (suratOperatorVM.resultTambah.hasObservers()) {
            suratOperatorVM.resultTambah.removeObservers(this)
        }
    }

}