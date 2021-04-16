package com.jembranakab.simas.ui.user.suratmasuk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.jembranakab.simas.R
import com.jembranakab.simas.base.BaseFragment
import com.jembranakab.simas.model.JsonOrg
import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.Organisasi
import com.jembranakab.simas.model.entities.SuratDisposisi
import com.jembranakab.simas.model.entities.SuratOpd
import com.jembranakab.simas.model.viewmodel.SuratOpdViewModel
import com.jembranakab.simas.ui.general.dialog.PdfDialogFragment
import com.jembranakab.simas.ui.user.suratmasuk.adapter.DisposisiAdapter
import com.jembranakab.simas.ui.user.suratmasuk.adapter.SuratMasukAdapter
import com.jembranakab.simas.ui.user.suratmasuk.dialog.DisposisiDialogFragment
import kotlinx.android.synthetic.main.user_suratmasuk.*

class SuratMasukUserFragment : BaseFragment() {

    private val suratOpdVM by activityViewModels<SuratOpdViewModel>()

    private var thisUnit: Int? = null
    private var accessCode: Int? = null

    private lateinit var suratMasukAdapter: SuratMasukAdapter
    private lateinit var disposisiAdapter: DisposisiAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var disposisiDialog: DisposisiDialogFragment
    private lateinit var pdfDialog: PdfDialogFragment

    private var listSuratMasuk = mutableListOf<SuratOpd>()
    private var listSuratDisposisi = mutableListOf<SuratDisposisi>()
    private var bawahan = arrayListOf<Organisasi>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_suratmasuk, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        if (accessCode!! > 1) {
            setAdapterDisposisi()
            loadDisposisi()
        } else {
            setAdapterSuratMasuk()
            loadSuratMasuk()
        }
    }

    private fun loadDisposisi() {
        suratOpdVM.listSuratDisposisi.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Loading -> {
                    progressBar.show()
                    kosong_tv.visibility = View.GONE
                    error_tv.visibility = View.GONE
                    retry_button.visibility = View.GONE
                }
                is Resource.Success -> {
                    if (it.data.isEmpty()) {
                        kosong_tv.visibility = View.VISIBLE
                    }
                    kosong_tv.visibility = View.GONE
                    listSuratDisposisi.clear()
                    listSuratDisposisi.addAll(it.data)
                    progressBar.hide()
                    disposisiAdapter.notifyDataSetChanged()
                }
                is Resource.Failure -> {
                    toastError(it.throwable)
                    progressBar.hide()
                    error_tv.visibility = View.VISIBLE
                    retry_button.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun loadSuratMasuk() {
        suratOpdVM.listSuratMasuk.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Loading -> {
                    progressBar.show()
                    kosong_tv.visibility = View.GONE
                    error_tv.visibility = View.GONE
                    retry_button.visibility = View.GONE
                }
                is Resource.Success -> {
                    if (it.data.isEmpty()) {
                        kosong_tv.visibility = View.VISIBLE
                    } else {
                        kosong_tv.visibility = View.GONE
                    }
                    listSuratMasuk.clear()
                    listSuratMasuk.addAll(it.data)
                    progressBar.hide()
                    suratMasukAdapter.notifyDataSetChanged()
                }
                is Resource.Failure -> {
                    toastError(it.throwable)
                    progressBar.hide()
                    error_tv.visibility = View.VISIBLE
                    retry_button.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setAdapterDisposisi() {
        (suratpager_rcv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        disposisiAdapter = DisposisiAdapter(accessCode!!, listSuratDisposisi, object : DisposisiAdapter.ButtonAdapterListener {

            override fun tampilOnClick(v: View?, suratDisposisi: SuratDisposisi) {
                val args = Bundle()
                args.putString("uri", suratDisposisi.getSurat()!!.pdfUrl)
                args.putString("header", suratDisposisi.getSurat()!!.namaPdf)
                pdfDialog.arguments = args
                pdfDialog.show(childFragmentManager, "pdfDialog")
            }

            override fun detailOnClick(v: View?, suratDisposisi: SuratDisposisi) {

            }

            override fun disposisiOnClick(v: View?, suratDisposisi: SuratDisposisi) {

            }

            override fun jawabanOnClick(v: View?, suratDisposisi: SuratDisposisi) {

            }

            override fun penyelesaianOnClick(v: View?, suratDisposisi: SuratDisposisi) {

            }

        }, requireContext())

        linearLayoutManager = LinearLayoutManager(context)
        suratpager_rcv.adapter = disposisiAdapter
        suratpager_rcv.layoutManager = linearLayoutManager
        suratpager_rcv.setHasFixedSize(true)
    }

    private fun setAdapterSuratMasuk() {
        (suratpager_rcv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        suratMasukAdapter = SuratMasukAdapter(accessCode!!, listSuratMasuk, object : SuratMasukAdapter.ButtonAdapterListener {

            override fun tampilOnClick(v: View?, suratOpd: SuratOpd) {
                val args = Bundle()
                args.putString("uri", suratOpd.getSurat()!!.pdfUrl)
                args.putString("header", suratOpd.getSurat()!!.namaPdf)
                pdfDialog.arguments = args
                pdfDialog.show(childFragmentManager, "pdfDialog")
            }

            override fun detailOnClick(v: View?, suratOpd: SuratOpd) {

            }

            override fun disposisiOnClick(v: View?, suratOpd: SuratOpd) {
                val args = Bundle()
                suratOpd.setExpanded(false)
                args.putSerializable("suratOpd", suratOpd)
                args.putInt("thisUnit", thisUnit!!)
                args.putParcelableArrayList("bawahan", bawahan)
                disposisiDialog.arguments = args
                disposisiDialog.show(childFragmentManager, "disposisiDialog")
            }

        }, requireContext())

        linearLayoutManager = LinearLayoutManager(context)
        suratpager_rcv.adapter = suratMasukAdapter
        suratpager_rcv.layoutManager = linearLayoutManager
        suratpager_rcv.setHasFixedSize(true)
    }

    private fun init() {
        thisUnit =
                findNavController().currentDestination!!.arguments["thisUnit"]?.defaultValue as Int
        accessCode =
                findNavController().currentDestination!!.arguments["accessCode"]?.defaultValue as Int
        disposisiDialog = DisposisiDialogFragment.newInstance()
        pdfDialog = PdfDialogFragment.newInstance()
        if (accessCode != 3) {
            bawahan = JsonOrg.getBawahan(thisUnit!!)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (accessCode!! > 1) {
            disposisiAdapter.setAllExpanded(false)
        } else {
            suratMasukAdapter.setAllExpanded(false)
        }
    }

}