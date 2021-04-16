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
import com.jembranakab.simas.model.viewmodel.SuratOpdViewModel
import com.jembranakab.simas.ui.general.dialog.ConfirmDialogFragment
import com.jembranakab.simas.ui.general.dialog.PdfDialogFragment
import com.jembranakab.simas.ui.user.suratmasuk.adapter.DisposisiAdapter
import com.jembranakab.simas.ui.user.suratmasuk.dialog.DetailDisposisiDialogFragment
import com.jembranakab.simas.ui.user.suratmasuk.dialog.DisposisiDialogFragment
import com.jembranakab.simas.ui.user.suratmasuk.dialog.JawabanDialogFragment
import com.jembranakab.simas.ui.user.suratmasuk.dialog.PenyelesaianDialogFragment
import kotlinx.android.synthetic.main.user_disposisi.*

class DisposisiUserFragment : BaseFragment() {

    private val suratOpdVM by activityViewModels<SuratOpdViewModel>()
    private var thisUnit: Int? = null
    private var accessCode: Int? = null

    private lateinit var mAdapter: DisposisiAdapter
    private lateinit var llm: LinearLayoutManager
    private lateinit var disposisiDialog: DisposisiDialogFragment
    private lateinit var detailDisposisiDialog: DetailDisposisiDialogFragment
    private lateinit var jawabanDialog: JawabanDialogFragment
    private lateinit var confirmDialog: ConfirmDialogFragment
    private lateinit var pdfDialog: PdfDialogFragment
    private lateinit var penyelesaianDialogFragment: PenyelesaianDialogFragment

    private var dataList = mutableListOf<SuratDisposisi>()
    private var bawahan = arrayListOf<Organisasi>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_disposisi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setAdapter()
        loadData()
    }

    private fun loadData() {
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
        mAdapter = DisposisiAdapter(accessCode!!, dataList, object : DisposisiAdapter.ButtonAdapterListener{

            override fun tampilOnClick(v: View?, suratDisposisi: SuratDisposisi) {
                val args = Bundle()
                args.putString("uri", suratDisposisi.getSurat()!!.pdfUrl)
                args.putString("header", suratDisposisi.getSurat()!!.namaPdf)
                pdfDialog.arguments = args
                pdfDialog.show(childFragmentManager, "pdfDialog")
            }

            override fun detailOnClick(v: View?, suratDisposisi: SuratDisposisi) {
                suratOpdVM.getDetailDisposisi(suratDisposisi)
                detailDisposisiDialog.show(childFragmentManager, "detailDisposisiDialog")
            }

            override fun disposisiOnClick(v: View?, suratDisposisi: SuratDisposisi) {
                suratDisposisi.setExpanded(false)
                val args = Bundle()
                args.putSerializable("suratDisposisi", suratDisposisi)
                args.putInt("thisUnit", thisUnit!!)
                args.putParcelableArrayList("bawahan", bawahan)
                disposisiDialog.arguments = args
                disposisiDialog.show(childFragmentManager, "disposisiDialog")
            }

            override fun jawabanOnClick(v: View?, suratDisposisi: SuratDisposisi) {
                val args = Bundle()
                args.putSerializable("suratDisposisi", suratDisposisi)
                args.putInt("thisUnit", thisUnit!!)
                jawabanDialog.arguments = args
                jawabanDialog.show(childFragmentManager, "jawabanDialog")
            }

            override fun penyelesaianOnClick(v: View?, suratDisposisi: SuratDisposisi) {
                val bundle = Bundle()
                bundle.putSerializable("suratDisposisi", suratDisposisi)
                penyelesaianDialogFragment.arguments = bundle
                penyelesaianDialogFragment.show(childFragmentManager, "PenyelesaianDialog")
            }

        }, requireContext())
        llm = LinearLayoutManager(context)
        suratpager_rcv.adapter = mAdapter
        suratpager_rcv.layoutManager = llm
        suratpager_rcv.setHasFixedSize(true)
    }

    private fun init() {
        thisUnit =
                findNavController().currentDestination!!.arguments["thisUnit"]?.defaultValue as Int
        accessCode =
                findNavController().currentDestination!!.arguments["accessCode"]?.defaultValue as Int
        disposisiDialog = DisposisiDialogFragment.newInstance()
        detailDisposisiDialog = DetailDisposisiDialogFragment.newInstance()
        jawabanDialog = JawabanDialogFragment.newInstance()
        confirmDialog = ConfirmDialogFragment.newInstance()
        pdfDialog = PdfDialogFragment.newInstance()
        penyelesaianDialogFragment = PenyelesaianDialogFragment.newInstance()
        if (accessCode != 3) {
            bawahan = JsonOrg.getBawahan(thisUnit!!)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mAdapter.setAllExpanded(false)
    }

}