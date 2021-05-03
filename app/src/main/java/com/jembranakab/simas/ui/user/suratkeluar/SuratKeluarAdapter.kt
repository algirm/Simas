package com.jembranakab.simas.ui.user.suratkeluar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.jembranakab.simas.R
import com.jembranakab.simas.model.entities.DraftSurat
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.BELUM_PROSES
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.DIAJUKAN
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.DIAJUKAN_KEMBALI
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.DIKOREKSI
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.DILANJUTKAN
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.DISETUJUI_BIDANG
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.DISETUJUI_DINAS
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.KOREKSI
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.SETUJU
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.TELAH_DIKOREKSI
import com.jembranakab.simas.utilities.DataConverter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.user_item_draft_surat_keluar.*

class SuratKeluarAdapter(
    private val thisUnit: Int,
    private val mList: MutableList<DraftSurat>,
    private val listener: ButtonAdapterListener,
    private val mContext: Context
) : RecyclerView.Adapter<SuratKeluarAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(data: DraftSurat) {
            with(DataConverter) {
                "Tujuan:".also { text_atas.text = it }
                atas_tv.text =
                        if (data.surat?.dari == 99) {
                            data.surat?.namaDinasLuar
                        } else {
                            getNamaOrgFromAll(data.surat?.kepada!!)
                        }
                nomor_tv.text = data.surat?.nomor
                tanggal_tv.text = data.surat?.tanggal
                status_tv.text = status.text
                perihal_tv.text = data.surat?.perihal
                "Asal Surat".also { text_bawah.text = it }
                bawah_tv.text = getNamaOrgFromAll(data.asalDraft!!)

                var statusText = ""

                when (data.status) {
                    BELUM_PROSES -> {
                        statusText = "Belum Diproses"
                        btTampil.visibility = View.INVISIBLE
                        btKoreksi.visibility = View.INVISIBLE
                        btSetuju.visibility = View.INVISIBLE
                        btDraft.visibility = View.VISIBLE
                    }
                    DIAJUKAN -> {
                        if (data.penerima == thisUnit) {
                            btTampil.visibility = View.VISIBLE
                            btKoreksi.visibility = View.VISIBLE
                            btSetuju.visibility = View.VISIBLE
                            btDraft.visibility = View.INVISIBLE
                            statusText = "Pengajuan"
                        } else {
                            statusText = "Sedang Diajukan"
                            btTampil.visibility = View.VISIBLE
                            btKoreksi.visibility = View.INVISIBLE
                            btSetuju.visibility = View.INVISIBLE
                            btDraft.visibility = View.INVISIBLE
                        }
                    }
                    DIAJUKAN_KEMBALI -> {
                        statusText = "Sedang Diajukan Kembali"
                        btTampil.visibility = View.VISIBLE
                        btKoreksi.visibility = View.INVISIBLE
                        btSetuju.visibility = View.INVISIBLE
                        btDraft.visibility = View.INVISIBLE
                    }
                    KOREKSI -> {
                        statusText = "Sedang Dikoreksi"
                        btTampil.visibility = View.VISIBLE
                        btKoreksi.visibility = View.INVISIBLE
                        btSetuju.visibility = View.INVISIBLE
                        btDraft.visibility = View.INVISIBLE
                    }
                    DISETUJUI_BIDANG -> {
                        statusText = "Disetujui dan dilanjutkan"
                        btTampil.visibility = View.VISIBLE
                        btKoreksi.visibility = View.INVISIBLE
                        btSetuju.visibility = View.INVISIBLE
                        btDraft.visibility = View.INVISIBLE
                    }
                    DISETUJUI_DINAS -> {
                        statusText = "Selesai/Disetujui"
                        btTampil.visibility = View.VISIBLE
                        btKoreksi.visibility = View.INVISIBLE
                        btSetuju.visibility = View.INVISIBLE
                        btDraft.visibility = View.INVISIBLE
                    }
                    DIKOREKSI -> {
                        statusText = "Koreksi Surat"
                        if (data.asalDraft == thisUnit) {
                            btTampil.visibility = View.VISIBLE
                            btKoreksi.visibility = View.INVISIBLE
                            btSetuju.visibility = View.INVISIBLE
                            btDraft.visibility = View.VISIBLE
                        } else {
                            btTampil.visibility = View.VISIBLE
                            btKoreksi.visibility = View.INVISIBLE
                            btSetuju.visibility = View.INVISIBLE
                            btDraft.visibility = View.INVISIBLE
                        }
                    }
                    TELAH_DIKOREKSI -> {
                        statusText = "Pengajuan telah dikoreksi"
                        btTampil.visibility = View.VISIBLE
                        btKoreksi.visibility = View.VISIBLE
                        btSetuju.visibility = View.VISIBLE
                        btDraft.visibility = View.INVISIBLE
                    }
                    DILANJUTKAN -> {
                        statusText = "Dilanjutkan"
                        btTampil.visibility = View.VISIBLE
                        btKoreksi.visibility = View.INVISIBLE
                        btSetuju.visibility = View.INVISIBLE
                        btDraft.visibility = View.INVISIBLE
                    }
                    SETUJU -> {
                        statusText = "Selesai"
                        btTampil.visibility = View.VISIBLE
                        btKoreksi.visibility = View.INVISIBLE
                        btSetuju.visibility = View.INVISIBLE
                        btDraft.visibility = View.INVISIBLE
                    }
                }
                if (data.keterangan.isNullOrBlank()) {
                    container_keterangan.visibility = View.GONE
                } else {
                    container_keterangan.visibility = View.VISIBLE
                    tvKeterangan.text = data.keterangan
                }
                status.text = statusText
                status_tv.text = statusText

                if (data.isExpanded()) {
                    drop_arrow.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
                    item_expand_container.visibility = View.VISIBLE
                } else {
                    drop_arrow.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                    item_expand_container.visibility = View.GONE
                }
                val slideDown: Animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_down)
                item_expand_container.startAnimation(slideDown)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_item_draft_surat_keluar, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        with(holder) {
            bind(mList[position])

            background_item.setOnClickListener {
                if (mList[position].isExpanded()) {
                    mList[position].setExpanded(false)
                } else {
                    mList[position].setExpanded(true)
                }
                notifyItemChanged(position)
            }

            btTampil.setOnClickListener {
                listener.tampilOnClick(it, mList[position])
            }
            btKoreksi.setOnClickListener {
                listener.koreksiOnClick(it, mList[position])
            }
            btSetuju.setOnClickListener {
                listener.setujuOnClick(it, mList[position])
            }
            btDraft.setOnClickListener {
                listener.draftOnClick(it, mList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun setAllExpanded(expanded: Boolean) {
        for (dt in mList) {
            dt.setExpanded(expanded)
        }
    }

    interface ButtonAdapterListener {
        fun tampilOnClick(v: View?, draftSurat: DraftSurat)
        fun koreksiOnClick(v: View?, draftSurat: DraftSurat)
        fun setujuOnClick(v: View?, draftSurat: DraftSurat)
        fun draftOnClick(v: View?, draftSurat: DraftSurat)
    }

}