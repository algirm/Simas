package com.jembranakab.simas.model.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jembranakab.simas.model.network.base.SuratOperatorRepo

class SuratOperatorVMFactory(private val repo: SuratOperatorRepo): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(SuratOperatorRepo::class.java).newInstance(repo)
    }

}