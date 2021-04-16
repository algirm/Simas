package com.jembranakab.simas.ui.operator.suratkeluar.adapter

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
import kotlinx.android.synthetic.main.operator_item_surat.*

class ArsipKeluarAdapter(
        private val mList: MutableList<SuratOpd>,
        private val listener: ButtonAdapterListener,
        private val mContext: Context
) : RecyclerView.Adapter<ArsipKeluarAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.operator_item_surat, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        with(holder) {
//            setIsRecyclable(false)
            bind(mList[position])

            background_item.setOnClickListener {
                if (mList[position].isExpanded()) {
                    mList[position].setExpanded(false)
                } else {
                    mList[position].setExpanded(true)
                }
                notifyItemChanged(position)
            }

            // button listener
            button_kirim.setOnClickListener { listener.kirimOnClick(it, mList[position]) }
            button_detail.setOnClickListener { listener.detailOnClick(it, mList[position]) }
            button_tampil.setOnClickListener { listener.tampilOnClick(it, mList[position]) }
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

    inner class ItemViewHolder(override val containerView: View) :
            RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(data: SuratOpd) {
            with(DataConverter) {
                tujuan_tv.text = getNamaOrgFromTopLayer(data.getSurat()?.kepada!!)
                nomor_tv.text = data.getSurat()?.nomor
                tanggal_tv.text = data.getSurat()?.tanggal
                status.text = getStatusOp(data.status!!)
                dari_tv.text = getNamaOrgFromTopLayer(data.getSurat()?.dari!!)
                perihal_tv.text = data.getSurat()?.perihal
                status_tv.text = status.text

                if (data.status == 0) {
                    button_kirim.visibility = View.VISIBLE
                    button_detail.visibility = View.GONE
                } else {
                    button_kirim.visibility = View.GONE
                    button_detail.visibility = View.VISIBLE
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
        fun detailOnClick(v: View?, suratOpd: SuratOpd)
        fun kirimOnClick(v: View?, suratOpd: SuratOpd)
        fun tampilOnClick(v: View?, suratOpd: SuratOpd)
    }

}
