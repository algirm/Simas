package com.jembranakab.simas.ui.user.suratmasuk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.jembranakab.simas.R
import com.jembranakab.simas.model.entities.SuratOpd
import com.jembranakab.simas.utilities.DataConverter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.user_item_suratmasuk.*

class SuratMasukAdapter(
        private val accessCode: Int,
        private val mList: MutableList<SuratOpd>,
        private val listener: ButtonAdapterListener,
        private val mContext: Context
) : RecyclerView.Adapter<SuratMasukAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(data: SuratOpd) {
            with(DataConverter) {
                "Dari:".also { text_atas.text = it }
                atas_tv.text =
                        if (data.getSurat()!!.dari == 99) {
                            data.getSurat()!!.namaDinasLuar
                        } else {
                            getNamaOrgFromTopLayer(data.getSurat()!!.dari!!)
                        }
                nomor_tv.text = data.getSurat()!!.nomor
                tanggal_tv.text = data.getSurat()!!.tanggal
                status.text = getStatus(data.status!!)
                status_tv.text = status.text
                perihal_tv.text = data.getSurat()!!.perihal

                if (data.status == 1) {
                    button_disposisi.visibility = View.VISIBLE
                    button_detail.visibility = View.GONE
                } else {
                    button_disposisi.visibility = View.GONE
                    button_detail.visibility = View.VISIBLE
                }

                if (data.isForward) {
                    "Terusan/Tembusan Dari:".also { text_bawah.text = it }
                    bawah_tv.text = getNamaOrgFromTopLayer(data.from_unit!!)
                } else {
                    "Tujuan:".also { text_bawah.text = it }
                    bawah_tv.text = getNamaOrgFromTopLayer(data.getSurat()!!.kepada!!)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_item_suratmasuk, parent, false)
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

            button_tampil.setOnClickListener {
                listener.tampilOnClick(it, mList[position])
            }
            button_disposisi.setOnClickListener {
                listener.disposisiOnClick(it, mList[position])
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
        fun tampilOnClick(v: View?, suratOpd: SuratOpd)
        fun detailOnClick(v: View?, suratOpd: SuratOpd)
        fun disposisiOnClick(v: View?, suratOpd: SuratOpd)
    }

}