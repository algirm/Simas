package com.jembranakab.simas.ui.user.suratmasuk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.jembranakab.simas.R
import com.jembranakab.simas.model.entities.SuratDisposisi
import com.jembranakab.simas.utilities.DataConverter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.user_item_suratdisposisi.*

class DisposisiAdapter(
        private val accessCode: Int,
        private val mList: MutableList<SuratDisposisi>,
        private val listener: ButtonAdapterListener,
        private val mContext: Context
) : RecyclerView.Adapter<DisposisiAdapter.DisposisiViewHolder>() {

    inner class DisposisiViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(data: SuratDisposisi) {
            with(DataConverter) {
                nomor_tv.text = data.getSurat()!!.nomor
                perihal_tv.text = data.getSurat()!!.perihal
                atas_tv.text = if (data.getSurat()!!.dari == 99) {
                    data.getSurat()!!.namaDinasLuar
                } else {
                    getNamaOrgFromTopLayer(data.getSurat()!!.dari!!)
                }
                bawah_tv.text = getNamaOrgFromAll(data.from_unit!!)
                tanggal_tv.text = data.getSurat()!!.tanggal
                isi_ringkas_tv.text = data.isiRingkas
                instruksi_tv.text = data.instruksi
                tambahan_tv.text = data.tambahanInstruksi

                status.text = getStatusDisposisi(data.status!!)
                status_tv.text = status.text
                when (data.status) {
                    1 -> {
                        button_penyelesaian.visibility = View.GONE
                        button_jawaban.visibility = View.GONE
                        button_disposisi.visibility = View.VISIBLE
                    }
                    2 -> {
                        button_penyelesaian.visibility = View.GONE
                        button_jawaban.visibility = View.VISIBLE
                        button_disposisi.visibility = View.GONE
                        if (accessCode == 1) {
                            button_jawaban.visibility = View.GONE
                        }
                        if (data.jawaban != null) {
                            button_penyelesaian.visibility = View.VISIBLE
                            button_jawaban.visibility = View.GONE
                        }
                    }
                    3 -> {
                        if (data.jawaban != null) {
                            button_penyelesaian.visibility = View.VISIBLE
                            button_jawaban.visibility = View.GONE
                        } else {
                            button_penyelesaian.visibility = View.GONE
                            button_jawaban.visibility = View.VISIBLE
                        }
                        button_disposisi.visibility = View.GONE
                        if (accessCode == 1 && data.selfDispose == true) {
                            button_penyelesaian.visibility = View.VISIBLE
                            button_jawaban.visibility = View.GONE
                        }
                    }
                    5 -> {
                        button_penyelesaian.visibility = View.GONE
                        button_jawaban.visibility = View.GONE
                        button_disposisi.visibility = View.GONE
                    }
                }

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

    interface ButtonAdapterListener {
        fun tampilOnClick(v: View?, suratDisposisi: SuratDisposisi)
        fun detailOnClick(v: View?, suratDisposisi: SuratDisposisi)
        fun disposisiOnClick(v: View?, suratDisposisi: SuratDisposisi)
        fun jawabanOnClick(v: View?, suratDisposisi: SuratDisposisi)
        fun penyelesaianOnClick(v: View?, suratDisposisi: SuratDisposisi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisposisiViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_item_suratdisposisi, parent, false)
        return DisposisiViewHolder(view)
    }

    override fun onBindViewHolder(holder: DisposisiViewHolder, position: Int) {
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

            button_tampil.setOnClickListener {
                listener.tampilOnClick(it, mList[position])
            }
            button_detail.setOnClickListener {
                listener.detailOnClick(it, mList[position])
            }
            button_disposisi.setOnClickListener {
                listener.disposisiOnClick(it, mList[position])
            }
            button_jawaban.setOnClickListener {
                listener.jawabanOnClick(it, mList[position])
            }
            button_penyelesaian.setOnClickListener {
                listener.penyelesaianOnClick(it, mList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun getItem(position: Int): SuratDisposisi {
        return mList[position]
    }

    fun setAllExpanded(expanded: Boolean) {
        for (dt in mList) {
            dt.setExpanded(expanded)
        }
    }

    fun setExpanded(position: Int, expanded: Boolean) {
        mList[position].setExpanded(expanded)
        notifyItemChanged(position, mList[position])
    }

}