package com.jembranakab.simas.ui.user.suratmasuk.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jembranakab.simas.R
import com.jembranakab.simas.model.entities.Organisasi
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_checkbox_list.*

class KepadaAdapter(private val list: List<Organisasi>) : RecyclerView.Adapter<KepadaAdapter.KepadaViewHolder>() {

    inner class KepadaViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bind(org: Organisasi) {
            text_item_checkbox.text = org.nama
            checkbox_item.isChecked = org.isChecked
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KepadaViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_checkbox_list, parent, false)
        return KepadaViewHolder(view)
    }

    override fun onBindViewHolder(holder: KepadaViewHolder, position: Int) {
        with(holder) {
            bind(list[position])
            container_item_list.setOnClickListener {
                checkbox_item.isChecked = !checkbox_item.isChecked
            }
            checkbox_item.setOnCheckedChangeListener { _, b ->
                list[position].isChecked = b
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getItem(position: Int): Organisasi {
        return list[position]
    }

    fun setAllCheck(boolean: Boolean) {
        for (org in list) {
            org.isChecked = boolean
        }
    }

}