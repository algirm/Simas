package com.jembranakab.simas.ui.user.suratkeluar

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jembranakab.simas.R
import com.jembranakab.simas.base.BaseDialogFragment
import com.jembranakab.simas.base.BaseFragment
import com.jembranakab.simas.model.entities.DraftSurat
import com.jembranakab.simas.utilities.App
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.DIAJUKAN
import com.jembranakab.simas.utilities.DataConverter
import es.voghdev.pdfviewpager.library.RemotePDFViewPager
import es.voghdev.pdfviewpager.library.adapter.PDFPagerAdapter
import es.voghdev.pdfviewpager.library.remote.DownloadFile
import es.voghdev.pdfviewpager.library.util.FileUtil
import kotlinx.android.synthetic.main.user_draftsurat_dialog.view.*

class DraftSuratDialogFragment : BaseDialogFragment(), DownloadFile.Listener {

    private lateinit var remotePDFViewPager: RemotePDFViewPager
    private lateinit var adapter: PDFPagerAdapter
    private lateinit var dv: View
    private var isFailure = true

    private var pdfUrl: String = ""
    private var namaFile: String = ""

    private val mStorageReference = FirebaseStorage.getInstance().reference

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val draftSurat = requireArguments().getSerializable("draftSurat") as DraftSurat
            val tipeAjukan = requireArguments().getInt("tipeAjukan")
            val builder = AlertDialog.Builder(it)
            dv = layoutInflater.inflate(R.layout.user_draftsurat_dialog, null)
            builder.setView(dv)

            dv.nomor_et.setText(draftSurat.surat?.nomor)
            if (draftSurat.surat?.namaDinasLuar.isNullOrBlank()) {
                dv.etKepada.setText(DataConverter.getNamaOrgFromAll(draftSurat.surat?.kepada!!))
            } else {
                dv.etKepada.setText(draftSurat.surat?.namaDinasLuar)
            }
            dv.etTanggalSurat.setText(draftSurat.surat?.tanggal)
            dv.etPerihal.setText(draftSurat.surat?.perihal)
            val atasan = DataConverter.getAtasanUnitExceptTopUnit(draftSurat.pengirim!!)
            dv.etDiajukanKepada.setText(DataConverter.getNamaOrgFromAll(atasan))

            dv.button_pilihFile.setOnClickListener {
                getPDF()
            }

            builder.setPositiveButton(getString(R.string.simpan)) { _, _ ->
                if (pdfUrl.isBlank()) {
                    Toast.makeText(context, "Upload File PDF Terlebih Dahulu", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                draftSurat.apply {
                    status = tipeAjukan
                    pengirim = draftSurat.asalDraft
                    penerima = atasan
                    lastModified = Timestamp.now()
                    surat?.pdfUrl = pdfUrl
                    surat?.namaPdf = namaFile
                    keterangan = null
                }
                val bundle = Bundle()
                bundle.putSerializable("draftSurat", draftSurat)
                bundle.putInt("tipeAjukan", tipeAjukan)
                listener.onDialogPositiveClick(this, DIAJUKAN, bundle)
            }.setNegativeButton(getString(R.string.batal)) {_, _ ->
                listener.onDialogNegativeClick(this)
            }

            builder.create()
        }?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BaseFragment.PICK_PDF) {
            if (data?.data != null) {
                uploadFile(data.data!!)
            }
        }
    }

    private fun uploadFile(data: Uri) {
        dv.pdfDialog_progress_bar.show()
        dv.button_pilihFile.visibility = View.GONE
        dv.filename_tv.visibility = View.VISIBLE
        namaFile = "${System.currentTimeMillis()}_${FileUtil.extractFileNameFromURL(data.lastPathSegment)}"
        val sRef: StorageReference = mStorageReference.child(namaFile)
        sRef.putFile(data)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    dv.pdfDialog_progress_bar.hide()
                    dv.filename_tv.text = namaFile
                    dv.button_pilihFile.visibility = View.GONE
                    pdfUrl = uri.toString()
                    remotePDFViewPager = RemotePDFViewPager(context, pdfUrl, this)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                dv.pdfDialog_progress_bar.hide()
            }
            .addOnProgressListener { taskSnapshot ->
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                "${progress.toInt()}% Diunggah...".also { dv.filename_tv.text = it }
            }
    }

    private fun getPDF() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            val permission: Array<String> = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(requireActivity(), permission,
                BaseFragment.REQUEST_PERMISSION
            )
            return
        }

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Pilih File"), BaseFragment.PICK_PDF)
    }

    override fun onSuccess(url: String?, destinationPath: String?) {
        isFailure = false
        dv.loading_container.visibility = View.GONE
        adapter = PDFPagerAdapter(context, FileUtil.extractFileNameFromURL(url))
        remotePDFViewPager.adapter = adapter
        dv.pdfContainer.addView(remotePDFViewPager, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        dv.text_unggah.visibility = View.GONE
    }

    override fun onFailure(e: Exception?) {
        isFailure = true
        dv.loading_container.visibility = View.GONE
        dv.loading_tv.text = getString(R.string.terjadi_kesalahan_tidak_dapat_memuat_data)
        Toast.makeText(context, e?.message, Toast.LENGTH_SHORT).show()
    }

    override fun onProgressUpdate(progress: Int, total: Int) {
        val prog = 100 * progress / total
        "Loading $prog/100".also { dv.loading_tv.text = it }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isFailure)
            adapter.close()
    }

    companion object {
        fun newInstance(): DraftSuratDialogFragment {
            return DraftSuratDialogFragment()
        }
    }

}