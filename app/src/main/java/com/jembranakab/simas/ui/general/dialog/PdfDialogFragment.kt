package com.jembranakab.simas.ui.general.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.jembranakab.simas.R
import com.jembranakab.simas.base.BaseDialogFragment
import es.voghdev.pdfviewpager.library.PDFViewPager
import es.voghdev.pdfviewpager.library.RemotePDFViewPager
import es.voghdev.pdfviewpager.library.adapter.PDFPagerAdapter
import es.voghdev.pdfviewpager.library.remote.DownloadFile
import es.voghdev.pdfviewpager.library.util.FileUtil
import kotlinx.android.synthetic.main.pdf_dialog.*
import kotlinx.android.synthetic.main.pdf_dialog.view.*
import java.io.BufferedInputStream
import java.io.InputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class PdfDialogFragment : BaseDialogFragment(), DownloadFile.Listener {

    private lateinit var remotePDFViewPager: RemotePDFViewPager
    private lateinit var pdfViewPager: PDFViewPager
    private lateinit var adapter: PDFPagerAdapter
    private lateinit var dv: View

    private var isFailure = true

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            dv = layoutInflater.inflate(R.layout.pdf_dialog, null)
            builder.setView(dv)
            val uri = arguments?.getString("uri")
            remotePDFViewPager = RemotePDFViewPager(context, uri, this)

            // Set the dialog title
            builder.setTitle(arguments?.getString("header"))
                // Set the action buttons
                .setNegativeButton(R.string.kembali) { _, _ ->
                    listener.onDialogNegativeClick(this)
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onSuccess(url: String?, destinationPath: String?) {
        isFailure = false
        dv.loading_container.visibility = View.GONE
        adapter = PDFPagerAdapter(context, FileUtil.extractFileNameFromURL(url))
        remotePDFViewPager.adapter = adapter
        dv.container.addView(remotePDFViewPager, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    override fun onFailure(e: Exception?) {
        isFailure = true
        dv.pdfDialog_progress_bar.hide()
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
        fun newInstance(): PdfDialogFragment {
            return PdfDialogFragment()
        }
    }

}