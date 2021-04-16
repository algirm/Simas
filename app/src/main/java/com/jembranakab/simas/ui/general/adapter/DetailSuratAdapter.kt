package com.jembranakab.simas.ui.general.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.jembranakab.simas.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.operator_detail_arsip_keluar_item_list.*

class DetailSuratAdapter(
        mContext: Context,
        private val dataList: List<Map<String, String>>
) : ArrayAdapter<Map<String, String>>(
        mContext,
        R.layout.operator_detail_arsip_keluar_item_list,
        dataList
) {

    inner class DetailViewHolder(override val containerView: View?) : LayoutContainer {

        fun bind(data: Map<String, String>) {
            penerima_detail.text = data["penerima"]
            status_detail.text = data["status"]
        }

    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var cv = convertView
        val holder: DetailViewHolder

        if (cv == null) {
            cv = LayoutInflater.from(parent.context).inflate(
                    R.layout.operator_detail_arsip_keluar_item_list,
                    parent,
                    false
            )
            holder = DetailViewHolder(cv)
            cv.tag = holder
        } else {
            holder = cv.tag as DetailViewHolder
        }

        holder.bind(dataList[position])
        holder.nomor_detail.text = (position + 1).toString()

        return cv!!
    }

    override fun getCount(): Int {
        return dataList.size
    }

}