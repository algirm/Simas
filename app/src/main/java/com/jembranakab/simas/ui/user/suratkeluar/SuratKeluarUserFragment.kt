package com.jembranakab.simas.ui.user.suratkeluar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jembranakab.simas.R
import com.jembranakab.simas.base.BaseFragment
import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.SuratOpd
import com.jembranakab.simas.ui.operator.suratkeluar.adapter.ArsipKeluarAdapter


class SuratKeluarUserFragment : BaseFragment() {

    private var thisUnit: Int? = null

    private lateinit var resultUpdateObserver: Observer<Resource<String>>

    private lateinit var mAdapter: ArsipKeluarAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var dataList = mutableListOf<SuratOpd>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_suratkeluar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setAdapter()
        loadData()
    }

    private fun loadData() {

    }

    private fun setAdapter() {

    }

    private fun init() {
        thisUnit =
                findNavController().currentDestination!!.arguments["thisUnit"]?.defaultValue as Int

    }

    override fun onDestroyView() {
        super.onDestroyView()
//        mAdapter.setAllExpanded(false)
    }

}