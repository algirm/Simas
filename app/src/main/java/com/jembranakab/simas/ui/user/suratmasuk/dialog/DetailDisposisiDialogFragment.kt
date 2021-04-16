package com.jembranakab.simas.ui.user.suratmasuk.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.jembranakab.simas.R
import com.jembranakab.simas.base.BaseDialogFragment
import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.viewmodel.SuratOpdViewModel
import com.jembranakab.simas.ui.user.suratmasuk.adapter.DetailKepadaDisposisiAdapter
import kotlinx.android.synthetic.main.user_detail_disposisi_dialog.view.*

class DetailDisposisiDialogFragment : BaseDialogFragment() {

    private val suratOpdVM by activityViewModels<SuratOpdViewModel>()
    private lateinit var mAdapter: DetailKepadaDisposisiAdapter
    private val kepadaList = mutableListOf<Map<String, String>>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let { activity ->
            val builder = AlertDialog.Builder(activity)
            val dialogView = layoutInflater.inflate(R.layout.user_detail_disposisi_dialog, null)
            builder.setView(dialogView)
            builder.setTitle("Detail Disposisi")

            with(dialogView) {
                mAdapter = DetailKepadaDisposisiAdapter(context, kepadaList)
                listview_detail_disposisi.adapter = mAdapter

                suratOpdVM.resultDetailDisposisi.observe(this@DetailDisposisiDialogFragment, {
                    when (it) {
                        is Resource.Loading -> progressBar.show()
                        is Resource.Success -> {
                            kepadaList.addAll((it.data["kepada"] as ArrayList<*>).filterIsInstance<Map<String, String>>())
                            progressBar.hide()
                            mAdapter.notifyDataSetChanged()
                            dari_detail_disposisi.text = it.data["dari"] as String
                            waktu_detail_disposisi.text = it.data["waktu"] as String
                            instruksi_detail_disposisi.text = it.data["instruksi"] as String
                            tambahan_detail_disposisi.text = it.data["tambahan"] as String
                        }
                        is Resource.Failure -> {
                            Toast.makeText(context, it.throwable?.message, Toast.LENGTH_SHORT).show()
                            progressBar.hide()
                        }
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
        kepadaList.clear()
        if (suratOpdVM.resultDetailDisposisi.hasObservers())
            suratOpdVM.resultDetailDisposisi.removeObservers(this)
    }

    companion object {
        fun newInstance(): DetailDisposisiDialogFragment {
            return DetailDisposisiDialogFragment()
        }
    }

}