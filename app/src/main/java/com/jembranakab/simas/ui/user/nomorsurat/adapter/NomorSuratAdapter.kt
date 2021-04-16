package com.jembranakab.simas.ui.user.nomorsurat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.jembranakab.simas.R
import com.jembranakab.simas.model.entities.Surat
import com.jembranakab.simas.utilities.DataConverter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.user_item_nomorsurat.*

class NomorSuratAdapter(
        private val mList: MutableList<Surat>,
        private val listener: ButtonAdapterListener,
        private val mContext: Context
) : RecyclerView.Adapter<NomorSuratAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(data: Surat) {
            with(DataConverter) {
                nomor_tv.text = data.nomor
                perihal_tv.text = data.perihal
                atas_tv.text =
                        if (data.kepada == 99) {
                            data.namaDinasLuar
                        } else {
                            getNamaOrgFromTopLayer(data.kepada!!)
                        }
                bawah_tv.text = getNamaOrgFromTopLayer(data.dari!!)
                asalsurat_tv.text = getNamaOrgFromAll(data.createBy!!)
                tanggal_tv.text = data.tanggal
                penandatangan_tv.text = bawah_tv.text

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
                .inflate(R.layout.user_item_nomorsurat, parent, false)
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

            button_hapus.setOnClickListener {
                listener.hapusOnClick(it, mList[position])
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
        fun hapusOnClick(v: View?, surat: Surat)
    }

}