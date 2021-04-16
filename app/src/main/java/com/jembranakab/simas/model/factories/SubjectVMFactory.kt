package com.jembranakab.simas.model.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jembranakab.simas.model.network.base.ISubjectRepo

class SubjectVMFactory(private val useCase: ISubjectRepo) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(ISubjectRepo::class.java).newInstance(useCase)
    }

}