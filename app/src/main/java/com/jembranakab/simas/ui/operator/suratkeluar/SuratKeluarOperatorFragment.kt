package com.jembranakab.simas.ui.operator.suratkeluar

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
import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.SuratOpd
import com.jembranakab.simas.ui.operator.suratkeluar.adapter.ArsipKeluarAdapter
import com.jembranakab.simas.ui.operator.suratkeluar.adapter.ArsipKeluarAdapter.ButtonAdapterListener
import com.jembranakab.simas.ui.operator.suratkeluar.dialog.DetailKeluarDialogFragment
import com.jembranakab.simas.ui.operator.suratkeluar.dialog.KirimKeluarDialogFragment
import com.jembranakab.simas.model.viewmodel.SuratOperatorViewModel
import com.jembranakab.simas.ui.general.dialog.PdfDialogFragment
import es.voghdev.pdfviewpager.library.util.FileUtil
import kotlinx.android.synthetic.main.operator_suratkeluar.*


class SuratKeluarOperatorFragment : BaseFragment() {

    private var thisUnit: Int? = null

    private val suratOperatorVM by activityViewModels<SuratOperatorViewModel>()

    private lateinit var mAdapter: ArsipKeluarAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var detailDialog: DetailKeluarDialogFragment
    private lateinit var kirimDialog: KirimKeluarDialogFragment
    private lateinit var pdfDialog: PdfDialogFragment

    private var dataList = mutableListOf<SuratOpd>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.operator_suratkeluar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setAdapter()
        loadData()
    }

    private fun loadData() {
        suratOperatorVM.listSuratKeluar.observe(viewLifecycleOwner, {
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
                    dataList.clear()
                    dataList.addAll(it.data)
                    progressBar.hide()
                    mAdapter.notifyDataSetChanged()
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

    private fun setAdapter() {
        (suratpager_rcv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        mAdapter = ArsipKeluarAdapter(dataList, object : ButtonAdapterListener {

            override fun tampilOnClick(v: View?, suratOpd: SuratOpd) {
                val args = Bundle()
                args.putString("uri", suratOpd.getSurat()!!.pdfUrl)
                args.putString("header", suratOpd.getSurat()!!.namaPdf)
                pdfDialog.arguments = args
                pdfDialog.show(childFragmentManager, "pdfDialog")
            }

            override fun detailOnClick(v: View?, suratOpd: SuratOpd) {
                suratOperatorVM.getDetailKeluar(suratOpd.id!!, suratOpd.penerima)
                detailDialog.show(childFragmentManager, "DetailDialog")
            }

            override fun kirimOnClick(v: View?, suratOpd: SuratOpd) {
                val args = Bundle()
                val suratOpd2: SuratOpd = suratOpd
                suratOpd2.setExpanded(false)
                args.putSerializable("suratOpd", suratOpd2)
                args.putInt("thisUnit", thisUnit!!)
                kirimDialog.arguments = args
                kirimDialog.show(childFragmentManager, "dialog")
            }
        }, requireContext())

        linearLayoutManager = LinearLayoutManager(context)
        suratpager_rcv.adapter = mAdapter
        suratpager_rcv.layoutManager = linearLayoutManager
        suratpager_rcv.setHasFixedSize(true)
    }

    private fun init() {
        thisUnit =
                findNavController().currentDestination!!.arguments["thisUnit"]?.defaultValue as Int
        tambah_fab.setOnClickListener { findNavController().navigate(R.id.nav_tambaharsip_keluar) }
        kirimDialog = KirimKeluarDialogFragment.newInstance()
        detailDialog = DetailKeluarDialogFragment.newInstance()
        pdfDialog = PdfDialogFragment.newInstance()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mAdapter.setAllExpanded(false)
    }

}