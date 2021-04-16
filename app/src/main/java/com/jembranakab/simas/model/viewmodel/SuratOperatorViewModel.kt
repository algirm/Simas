package com.jembranakab.simas.model.viewmodel

import androidx.lifecycle.*
import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.Surat
import com.jembranakab.simas.model.entities.SuratOpd
import com.jembranakab.simas.model.network.base.SuratOperatorRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SuratOperatorViewModel(private val repo: SuratOperatorRepo) : ViewModel(){

    var listSuratKeluar: MediatorLiveData<Resource<MutableList<SuratOpd>>> = MediatorLiveData()
    var listSuratMasuk: MediatorLiveData<Resource<MutableList<SuratOpd>>> = MediatorLiveData()
    var listDetailKeluar: LiveData<Resource<MutableList<Map<String, String>>>> = MutableLiveData()
    val resultTambah = MutableLiveData<Resource<String>>()
    val resultUpdate = MutableLiveData<Resource<String>>()
    val resultKirimKeluar = MutableLiveData<Resource<String>>()

    fun fetchSuratKeluar(unit: Int) {
        viewModelScope.launch {
            val liveData = liveData<Resource<MutableList<SuratOpd>>>(Dispatchers.IO) {
                emit(Resource.Loading())
                try {
                    emit(repo.fetchSuratDB(unit, true))
                } catch (e: Exception) {
                    emit(Resource.Failure(e))
                }
            }
            listSuratKeluar.addSource(liveData) {
                listSuratKeluar.postValue(it)
                if (it is Resource.Success || it is Resource.Failure) {
                    listSuratKeluar.removeSource(liveData)
                }
            }
        }
    }

    fun fetchSuratMasuk(unit: Int) {
        viewModelScope.launch {
            val liveData = liveData<Resource<MutableList<SuratOpd>>>(Dispatchers.IO) {
                emit(Resource.Loading())
                try {
                    emit(repo.fetchSuratDB(unit, false))
                } catch (e: Exception) {
                    emit(Resource.Failure(e))
                }
            }
            listSuratMasuk.addSource(liveData) {
                listSuratMasuk.postValue(it)
                if (it is Resource.Success || it is Resource.Failure) {
                    listSuratMasuk.removeSource(liveData)
                }
            }
        }
    }

    fun getDetailKeluar(suratOpdId: String, penerima: ArrayList<Int>) {
        viewModelScope.launch {
            liveData<Resource<MutableList<Map<String, String>>>>(Dispatchers.IO) {
                emit(Resource.Loading())
                try {
                    emit(repo.detailPenerimaDB(suratOpdId, penerima))
                } catch (e: Exception) {
                    emit(Resource.Failure(e))
                }
            }.also { listDetailKeluar = it }
        }
    }

    fun tambahArsip(surat: Surat, isOutbox: Boolean) {
        resultTambah.value = Resource.Loading()
        viewModelScope.launch {
            if (isOutbox) resultTambah.value = repo.tambahArsipDB(surat, true)
            else resultTambah.value = repo.tambahArsipDB(surat, false)
        }
    }

    fun tambahArsipMasuk(surat: Surat, tembusan: ArrayList<Int>) {
        resultTambah.value = Resource.Loading()
        viewModelScope.launch {
            resultTambah.value = repo.tambahArsipMasukDB(surat, tembusan)
        }
    }

    fun updateArsip(suratOpd: SuratOpd, isOutbox: Boolean) {
        resultUpdate.value = Resource.Loading()
        viewModelScope.launch {
            if (isOutbox) resultUpdate.value = repo.updateArsipDB(suratOpd, true)
            else resultUpdate.value = repo.updateArsipDB(suratOpd, false)
        }
    }

    fun kirimKeluar(suratOpd: SuratOpd, terusan: ArrayList<Int>) {
        resultKirimKeluar.value = Resource.Loading()
        viewModelScope.launch {
            resultKirimKeluar.value = repo.kirimKeluarDB(suratOpd, terusan)
        }
    }

}