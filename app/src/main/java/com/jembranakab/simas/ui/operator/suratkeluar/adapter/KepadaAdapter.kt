package com.jembranakab.simas.ui.operator.suratkeluar.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.jembranakab.simas.model.entities.Organisasi

class KepadaAdapter(
        mContext: Context,
        private val list: List<Organisasi>
) : ArrayAdapter<Organisasi>(
        mContext,
        android.R.layout.simple_spinner_item,
        list
) {

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var cView = convertView

        if (cView == null) {
            cView = LayoutInflater.from(parent.context).inflate(
                    android.R.layout.simple_spinner_dropdown_item,
                    parent,
                    false
            )
        }
        val tv: TextView = cView!!.findViewById(android.R.id.text1)
        tv.text = list[position].nama

        return cView
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var cView = convertView

        if (cView == null) {
            cView = LayoutInflater.from(parent.context).inflate(
                    android.R.layout.simple_spinner_item,
                    parent,
                    false
            )
        }
        val tv: TextView = cView!!.findViewById(android.R.id.text1)
        tv.text = list[position].nama

        return cView
    }

    override fun getItem(position: Int): Organisasi {
        return list[position]
    }

    override fun getCount(): Int {
        return list.size
    }

}