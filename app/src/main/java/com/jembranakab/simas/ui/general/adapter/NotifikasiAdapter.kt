package com.jembranakab.simas.ui.general.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.jembranakab.simas.R
import com.jembranakab.simas.model.entities.Notifikasi
import com.jembranakab.simas.utilities.App.Companion.DISPOSISI
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.DIAJUKAN
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.DIKOREKSI
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.DILANJUTKAN
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.DISETUJUI_BIDANG
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.DISETUJUI_DINAS
import com.jembranakab.simas.utilities.App.Companion.DraftSurat.TELAH_DIKOREKSI
import com.jembranakab.simas.utilities.App.Companion.JAWABAN
import com.jembranakab.simas.utilities.App.Companion.PENYELESAIAN
import com.jembranakab.simas.utilities.App.Companion.SURAT_MASUK
import com.jembranakab.simas.utilities.DataConverter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.user_item_notifikasi.*
import java.text.SimpleDateFormat
import java.util.*

class NotifikasiAdapter(
        private val context: Context
) : RecyclerView.Adapter<NotifikasiAdapter.NotifikasiViewHolder>() {

    inner class NotifikasiViewHolder(
            override val containerView: View
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(item: Notifikasi) {
            with(DataConverter) {
                val notifDari = item.fromUnit?.let { getNamaOrgFromAll(it) }
                var textNotif = "Surat telah diproses oleh $notifDari"
                var keteranganNotif = ""
                var statusText = "Notifikasi"
                var waktu = ""
                var waktuLengkap = ""
                when (item.tipe) {
                    PENYELESAIAN -> {
                        item.keterangan?.let {
                            keteranganNotif = "\nKeterangan Penyelesaian: $it"
                        }
                        textNotif = "Surat Disposisi telah diselesaikan oleh $notifDari$keteranganNotif"
                        statusText = "Penyelesaian Disposisi"
                    }
                    DISPOSISI -> {
                        item.keterangan?.let {
                            keteranganNotif = "\nKeterangan Tujuan Disposisi: $it"
                        }
                        textNotif = "Surat telah didisposisikan oleh $notifDari$keteranganNotif"
                        statusText = "Pen-disposisi-an"
                    }
                    JAWABAN -> {
                        item.keterangan?.let {
                            keteranganNotif = "\nJawaban: $it"
                        }
                        textNotif = "Surat Disposisi telah dijawab oleh $notifDari$keteranganNotif"
                        statusText = "Jawaban Disposisi"
                    }
                    SURAT_MASUK -> {
                        item.keterangan?.let {
                            keteranganNotif = "\nKeterangan: $it"
                        }
                        textNotif = "Surat Masuk baru diterima $keteranganNotif"
                        statusText = "Surat Masuk"
                    }
                    DIKOREKSI -> {
                        item.keterangan?.let {
                            keteranganNotif = "\nKeterangan Koreksi: $it"
                        }
                        textNotif = "Koreksi Pengajuan Surat Keluar $keteranganNotif"
                        statusText = "Draft Surat Keluar"
                    }
                    DISETUJUI_BIDANG -> {
                        item.keterangan?.let {
                            keteranganNotif = "\nKeterangan Penyetujuan: $it"
                        }
                        textNotif = "Pengajuan Surat telah disetujui dan dilanjutkan $keteranganNotif"
                        statusText = "Draft Surat Keluar"
                    }
                    DISETUJUI_DINAS -> {
                        item.keterangan?.let {
                            keteranganNotif = "\nKeterangan Penyetujuan: $it"
                        }
                        textNotif = "Pengajuan Surat telah disetujui dan segera ditandatangani $keteranganNotif"
                        statusText = "Draft Surat Keluar"
                    }
                    DIAJUKAN -> {
                        item.keterangan?.let {
                            keteranganNotif = "\nKeterangan: $it"
                        }
                        textNotif = "Pengajuan Surat Keluar untuk penandatanganan $keteranganNotif"
                        statusText = "Draft Surat Keluar"
                    }
                    TELAH_DIKOREKSI -> {
                        item.keterangan?.let {
                            keteranganNotif = "\nKeterangan: $it"
                        }
                        textNotif = "Pengajuan Surat Keluar telah dikoreksi $keteranganNotif"
                        statusText = "Draft Surat Keluar"
                    }
                    DILANJUTKAN -> {
                        item.keterangan?.let {
                            keteranganNotif = "\nKeterangan: $it"
                        }
                        textNotif = "Pengajuan Surat Keluar untuk penandatanganan $keteranganNotif"
                        statusText = "Draft Surat Keluar"
                    }
                }

                // format timestamp to date string
                item.receiveAt?.let { timestamp ->
                    val timestampDate = timestamp.toDate()
                    waktuLengkap =
                            SimpleDateFormat("dd MMM yyyy h:mm a", Locale.ROOT).format(timestampDate)
                    waktu = when {
                        (Timestamp.now().seconds)- (60 * 60 * 24) < timestamp.seconds -> {
                            SimpleDateFormat("h:mm a", Locale.ROOT).format(timestampDate)
                        }
                        (Timestamp.now().seconds)- (60 * 60 * 24 * 7) < timestamp.seconds -> {
                            SimpleDateFormat("E, h:mm a", Locale.ROOT).format(timestampDate)
                        }
                        else -> {
                            SimpleDateFormat("dd MMM yyyy", Locale.ROOT).format(timestampDate)
                        }
                    }
                }

                if (item.isExpanded()) {
                    drop_arrow.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
                    item_expand_container.visibility = View.VISIBLE
                } else {
                    drop_arrow.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                    item_expand_container.visibility = View.GONE
                }

                if (item.read) {
                    background_item.setBackgroundColor(ContextCompat.getColor(context, R.color.background_item_surat_read))
                    status.typeface = Typeface.DEFAULT
                    nomor_tv.typeface = Typeface.DEFAULT
                }

                tengahTv.text = item.surat?.nomor
                nomor_tv.text = statusText
                perihal_tv.text = item.surat?.perihal
                bawah_tv.text = notifDari
                isi_ringkas_tv.text = textNotif
                status.text = waktu
                status_tv.text = waktuLengkap
            }
        }

    }

    private val differCallback = object : DiffUtil.ItemCallback<Notifikasi>() {

        override fun areItemsTheSame(oldItem: Notifikasi, newItem: Notifikasi): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notifikasi, newItem: Notifikasi): Boolean {
            return oldItem == newItem
        }

    }

    private val differ = AsyncListDiffer(this, differCallback)

    var listNotifikasi: List<Notifikasi>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    private var onBackgroundClickListener: ((Notifikasi, Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifikasiViewHolder {
        return NotifikasiViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.user_item_notifikasi,
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: NotifikasiViewHolder, position: Int) {
        val notifikasi = listNotifikasi[position]
        with(holder) {
            bind(notifikasi)
            background_item.setOnClickListener {
                onBackgroundClickListener?.let { click ->
                    click(notifikasi, position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return listNotifikasi.size
    }

    fun setAllExpanded(expanded: Boolean) {
        for (dt in listNotifikasi) {
            dt.setExpanded(expanded)
        }
    }

    fun setOnBackgroundClickListener(onItemClick: (Notifikasi, Int) -> Unit) {
        onBackgroundClickListener = onItemClick
    }

}