package com.jembranakab.simas.ui.user.nomorsurat

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
import com.jembranakab.simas.ui.user.nomorsurat.adapter.NomorSuratAdapter
import com.jembranakab.simas.ui.user.suratkeluar.DraftSuratDialogFragment
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.DIAJUKAN
import kotlinx.android.synthetic.main.user_nomorsurat.*


class NomorSuratUserFragment : BaseFragment() {

    private var thisUnit: Int? = null
    private val suratOpdVM by activityViewModels<SuratOpdViewModel>()

    private lateinit var mAdapter: NomorSuratAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var draftSuratDialogFragment: DraftSuratDialogFragment

    private var dataList = mutableListOf<DraftSurat>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_nomorsurat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setAdapter()
        loadData()
    }

    private fun loadData() {
        suratOpdVM.listNomorSurat.observe(viewLifecycleOwner, {
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
        mAdapter = NomorSuratAdapter(dataList, object : NomorSuratAdapter.ButtonAdapterListener {

            override fun hapusOnClick(v: View?, surat: DraftSurat) {

            }

            override fun draftOnClick(v: View?, draftsurat: DraftSurat) {
                val args = Bundle()
                args.putSerializable("draftSurat", draftsurat)
                args.putInt("tipeAjukan", DIAJUKAN)
                draftSuratDialogFragment.arguments = args
                draftSuratDialogFragment.show(childFragmentManager, "draftSuratDialog")
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
        tambah_fab.setOnClickListener { findNavController().navigate(R.id.nav_user_tambah_nomorsurat) }
        draftSuratDialogFragment = DraftSuratDialogFragment.newInstance()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mAdapter.setAllExpanded(false)
    }

}