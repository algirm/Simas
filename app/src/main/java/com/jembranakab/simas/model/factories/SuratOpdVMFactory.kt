package com.jembranakab.simas.model.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jembranakab.simas.model.network.base.SuratOpdRepo

class SuratOpdVMFactory(private val repo: SuratOpdRepo): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(SuratOpdRepo::class.java).newInstance(repo)
    }

}