package com.jembranakab.simas.ui.user.suratkeluar

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
import com.jembranakab.simas.model.entities.DraftSurat
import com.jembranakab.simas.model.viewmodel.SuratOpdViewModel
import com.jembranakab.simas.ui.general.dialog.PdfDialogFragment
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.DIAJUKAN
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.DIAJUKAN_KEMBALI
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.KOREKSI
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.SETUJU
import kotlinx.android.synthetic.main.user_suratkeluar.*


class SuratKeluarUserFragment : BaseFragment() {

    private val suratOpdViewModel by activityViewModels<SuratOpdViewModel>()

    private var thisUnit: Int? = null

    private lateinit var mAdapter: SuratKeluarAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var pdfDialogFragment: PdfDialogFragment
    private lateinit var koreksiDialogFragment: KoreksiDialogFragment
    private lateinit var draftSuratDialogFragment: DraftSuratDialogFragment

    private var dataList = mutableListOf<DraftSurat>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_suratkeluar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setAdapter()
        loadData()
    }

    private fun loadData() {
        suratOpdViewModel.listDraftSurat.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Failure -> {
                    toastError(it.throwable)
                    progressBar.hide()
                }
                is Resource.Loading -> {
                    progressBar.show()
                }
                is Resource.Success -> {
                    dataList.clear()
                    dataList.addAll(it.data)
                    mAdapter.notifyDataSetChanged()
                    progressBar.hide()
                }
            }
        })
    }

    private fun setAdapter() {
        (suratpager_rcv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        mAdapter = SuratKeluarAdapter(
            thisUnit!!,
            dataList,
            object : SuratKeluarAdapter.ButtonAdapterListener {
                override fun tampilOnClick(v: View?, draftSurat: DraftSurat) {
                    draftSurat.surat?.pdfUrl?.let {
                        val args = Bundle()
                        args.putString("uri", it)
                        args.putString("header", draftSurat.surat?.namaPdf?: "Draft Surat")
                        pdfDialogFragment.arguments = args
                        pdfDialogFragment.show(childFragmentManager, "pdfDialog")
                    }
                }

                override fun koreksiOnClick(v: View?, draftSurat: DraftSurat) {
                    draftSurat.setExpanded(false)
                    val args = Bundle()
                    args.putSerializable("draftSurat", draftSurat)
                    args.putInt("tipe", KOREKSI)
                    args.putInt("thisUnit", thisUnit!!)
                    args.putString("header", "Koreksi")
                    koreksiDialogFragment.arguments = args
                    koreksiDialogFragment.show(childFragmentManager, "koreksiDialog")
                }

                override fun setujuOnClick(v: View?, draftSurat: DraftSurat) {
                    draftSurat.setExpanded(false)
                    val args = Bundle()
                    args.putSerializable("draftSurat", draftSurat)
                    args.putInt("tipe", SETUJU)
                    args.putInt("thisUnit", thisUnit!!)
                    args.putString("header", "Setuju")
                    koreksiDialogFragment.arguments = args
                    koreksiDialogFragment.show(childFragmentManager, "koreksiDialog")
                }

                override fun draftOnClick(v: View?, draftSurat: DraftSurat) {
                    val args = Bundle()
                    args.putSerializable("draftSurat", draftSurat)
                    if (draftSurat.status == 0) {
                        args.putInt("tipeAjukan", DIAJUKAN)
                    } else {
                        args.putInt("tipeAjukan", DIAJUKAN_KEMBALI)
                    }
                    draftSuratDialogFragment.arguments = args
                    draftSuratDialogFragment.show(childFragmentManager, "draftSuratDialog")
                }
            },
            requireContext()
        )
        linearLayoutManager = LinearLayoutManager(context)
        suratpager_rcv.adapter = mAdapter
        suratpager_rcv.layoutManager = linearLayoutManager
        suratpager_rcv.setHasFixedSize(true)
    }

    private fun init() {
        thisUnit =
                findNavController().currentDestination!!.arguments["thisUnit"]?.defaultValue as Int
        pdfDialogFragment = PdfDialogFragment.newInstance()
        koreksiDialogFragment = KoreksiDialogFragment.newInstance()
        draftSuratDialogFragment = DraftSuratDialogFragment.newInstance()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mAdapter.setAllExpanded(false)
    }

}