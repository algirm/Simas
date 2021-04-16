package com.jembranakab.simas.ui.operator.suratmasuk.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jembranakab.simas.R
import com.jembranakab.simas.model.entities.Organisasi
import com.jembranakab.simas.ui.general.adapter.CheckboxCallback
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_checkbox_list.*

class TembusanAdapter(
        private val list: List<Organisasi>,
        private val callback: CheckboxCallback
) : RecyclerView.Adapter<TembusanAdapter.TembusanViewHolder>() {

    inner class TembusanViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(org: Organisasi) {
            text_item_checkbox.text = org.nama
            checkbox_item.isChecked = org.isChecked
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TembusanViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_checkbox_list, parent, false)
        return TembusanViewHolder(view)
    }

    override fun onBindViewHolder(holder: TembusanViewHolder, position: Int) {
        with(holder) {
            setIsRecyclable(false)
            bind(list[position])
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
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getItem(position: Int): Organisasi {
        return list[position]
    }

}