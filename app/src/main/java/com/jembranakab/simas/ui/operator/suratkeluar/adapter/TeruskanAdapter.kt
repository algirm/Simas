package com.jembranakab.simas.ui.operator.suratkeluar.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.jembranakab.simas.R
import com.jembranakab.simas.model.entities.Organisasi
import com.jembranakab.simas.ui.general.adapter.CheckboxCallback
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_checkbox_list.*


class TeruskanAdapter(
        mContext: Context,
        private val list: List<Organisasi>,
        private val callback: CheckboxCallback
) : ArrayAdapter<Organisasi>(mContext, R.layout.item_checkbox_list, list) {

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(p0: Int): Organisasi {
        return list[p0]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var cView = convertView
        val holder: ItemViewHolder

        if (cView == null) {
            cView = LayoutInflater.from(parent.context).inflate(R.layout.item_checkbox_list, parent, false)
            holder = ItemViewHolder(cView)
            cView.tag = holder
        } else {
            holder = cView.tag as ItemViewHolder
        }

        with(holder) {
            container_item_list.setOnClickListener {
                checkbox_item.isChecked = !checkbox_item.isChecked
            }

            checkbox_item.setOnCheckedChangeListener { _, b ->
                list[position].isChecked = b
                callback.checkCountCheck()
                if (!b) {
                    callback.setHeaderCheckbox(false)
                }
            }

            bind(list[position])
        }

        return cView!!
    }

    inner class ItemViewHolder(override val containerView: View?) : LayoutContainer {

        fun bind(org: Organisasi) {
            text_item_checkbox.text = org.nama
            checkbox_item.isChecked = org.isChecked
        }

    }

}
