package com.jembranakab.simas.ui.user.suratmasuk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.jembranakab.simas.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.user_detail_disposisi_kepada_item.*

class DetailKepadaDisposisiAdapter(
        mContext: Context,
        private val mList: List<Map<String, String>>
) : ArrayAdapter<Map<String, String>>(
        mContext,
        R.layout.user_detail_disposisi_kepada_item,
        mList
) {

    inner class DetailViewHolder(override val containerView: View?) : LayoutContainer {

        fun bind(data: Map<String, String>) {
            kepada_detail_disposisi.text = data["kepada"]
            waktujawab_detail_disposisi.text = data["waktuJawab"]
            jawaban_detail_disposisi.text = data["jawaban"]
            keterangan_detail_disposisi.text = data["keterangan"]
        }

    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var cv = convertView
        val holder: DetailViewHolder

        if (cv == null) {
            cv = LayoutInflater.from(parent.context).inflate(
                    R.layout.user_detail_disposisi_kepada_item,
                    parent,
                    false
            )
            holder = DetailViewHolder(cv)
            cv.tag = holder
        } else {
            holder = cv.tag as DetailViewHolder
        }

        holder.bind(mList[position])
        "${(position + 1)}.".also { holder.nomor_detail_item_kepada.text = it }

        return cv!!
    }

    override fun getCount(): Int {
        return mList.size
    }

}