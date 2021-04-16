package com.jembranakab.simas.ui.operator.suratkeluar.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.activityViewModels
import com.jembranakab.simas.R
import com.jembranakab.simas.base.BaseDialogFragment
import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.ui.general.adapter.DetailSuratAdapter
import com.jembranakab.simas.model.viewmodel.SuratOperatorViewModel
import kotlinx.android.synthetic.main.list_view.view.*

class DetailKeluarDialogFragment : BaseDialogFragment() {

    private val suratOperatorVM by activityViewModels<SuratOperatorViewModel>()
    private lateinit var mAdapter: DetailSuratAdapter
    private val detailList = mutableListOf<Map<String, String>>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let { activity ->
            val builder = AlertDialog.Builder(activity)
            val dialogView = layoutInflater.inflate(R.layout.list_view, null)

            builder.setView(dialogView)
            builder.setTitle("Penerima")

            with(dialogView) {
                mAdapter = DetailSuratAdapter(context, detailList)
                listView.adapter = mAdapter

                suratOperatorVM.listDetailKeluar.observe(this@DetailKeluarDialogFragment, {
                    when (it) {
                        is Resource.Loading -> progressBar.show()
                        is Resource.Success -> {
                            detailList.addAll(it.data)
                            progressBar.hide()
                            mAdapter.notifyDataSetChanged()
                        }
                        is Resource.Failure -> progressBar.hide()
                    }
                })
            }

            builder.setNegativeButton(getString(R.string.kembali)) { _, _ ->
                listener.onDialogNegativeClick(this)
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        detailList.clear()
        if (suratOperatorVM.listDetailKeluar.hasObservers())
            suratOperatorVM.listDetailKeluar.removeObservers(this)
    }

    companion object {
        fun newInstance(): DetailKeluarDialogFragment {
            return DetailKeluarDialogFragment()
        }
    }

}