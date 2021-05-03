package com.jembranakab.simas.model.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.DraftSurat
import com.jembranakab.simas.model.entities.SuratDisposisi
import com.jembranakab.simas.model.entities.SuratOpd
import com.jembranakab.simas.model.network.base.SuratOpdRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SuratOpdViewModel(private val repo: SuratOpdRepo) : ViewModel(){

    val listNomorSurat = MediatorLiveData<Resource<MutableList<DraftSurat>>>()
    val listSuratKeluar = MediatorLiveData<Resource<MutableList<SuratOpd>>>()
    val listSuratMasuk = MediatorLiveData<Resource<MutableList<SuratOpd>>>()
    val listSuratDisposisi = MediatorLiveData<Resource<MutableList<SuratDisposisi>>>()
    val resultDisposisi = MutableLiveData<Resource<String>>()
    val resultJawab = MutableLiveData<Resource<String>>()
    val resultPenyelesaian = MutableLiveData<Resource<String>>()
    var resultDetailDisposisi: LiveData<Resource<Map<String, Any>>> = MutableLiveData()
    val resultTambahNomor = MutableLiveData<Resource<String>>()

    val listDraftSurat = MediatorLiveData<Resource<List<DraftSurat>>>()
    val resultDraftSurat = MutableLiveData<Resource<String>>()
    val resultKoreksiDraftSurat = MutableLiveData<Resource<String>>()
    val resultSetujuDraftSurat = MutableLiveData<Resource<String>>()

    fun koreksiDraftSurat(draftSurat: DraftSurat) = viewModelScope.launch {
        try {
            resultKoreksiDraftSurat.value = Resource.Loading()
            resultKoreksiDraftSurat.postValue(repo.koreksiDraftSurat(draftSurat))
        } catch (e: Exception) {
            resultKoreksiDraftSurat.value = Resource.Failure(e)
        }
    }

    fun setujuDraftSurat(draftSurat: DraftSurat, thisUnit: Int) = viewModelScope.launch {
        try {
            resultSetujuDraftSurat.value = Resource.Loading()
            resultSetujuDraftSurat.postValue(repo.setujuDraftSurat(draftSurat, thisUnit))
        } catch (e: Exception) {
            resultSetujuDraftSurat.value = Resource.Failure(e)
        }
    }

    fun draftSurat(draftSurat: DraftSurat, tipe: Int) = viewModelScope.launch {
        try {
            resultDraftSurat.value = Resource.Loading()
            resultDraftSurat.postValue(repo.draftSurat(draftSurat, tipe))
        } catch (e: Exception) {
            resultDraftSurat.value = Resource.Failure(e)
        }
    }

    fun getDraftSurat(thisUnit: Int) = viewModelScope.launch {
        val liveData =
            liveData(Dispatchers.IO) {
                emit(Resource.Loading())
                try {
                    emit(repo.getDraftSurat(thisUnit))
                } catch (e: Exception) {
                    emit(Resource.Failure(e))
                }
            }
        listDraftSurat.addSource(liveData) { listDraftSurat.postValue(it) }
    }

    fun getNomorSurat(unit: Int) {
        viewModelScope.launch {
            val liveData =
                liveData(Dispatchers.IO) {
                    emit(Resource.Loading())
                    try {
                        emit(repo.getNomorSurat(unit))
                    } catch (e: Exception) {
                        emit(Resource.Failure(e))
                    }
                }
            listNomorSurat.addSource(liveData) { listNomorSurat.postValue(it) }
        }
    }

    fun getSuratKeluar(unit: Int) {
        viewModelScope.launch {
            val tempLiveData =
                liveData(Dispatchers.IO) {
                    emit(Resource.Loading())
                    try {
                        emit(repo.getSuratOpd(unit, true))
                    } catch (e: Exception) {
                        emit(Resource.Failure(e))
                    }
                }
            listSuratKeluar.addSource(tempLiveData) { listSuratKeluar.postValue(it) }
        }
    }

    fun getSuratMasuk(unit: Int) {
        viewModelScope.launch {
            val tempLiveData =
                    liveData(Dispatchers.IO) {
                        emit(Resource.Loading())
                        try {
                            emit(repo.getSuratOpd(unit, false))
                        } catch (e: Exception) {
                            emit(Resource.Failure(e))
                        }
                    }
            listSuratMasuk.addSource(tempLiveData) { listSuratMasuk.postValue(it) }
        }
    }

    fun disposisi(kepada: ArrayList<Int>, suratDisposisi: SuratDisposisi) {
        resultDisposisi.value = Resource.Loading()
        viewModelScope.launch {
            resultDisposisi.value = repo.disposisi(kepada, suratDisposisi)
        }
    }

    fun getSuratDisposisi(unit: Int) {
        viewModelScope.launch {
            val liveData =
                    liveData(Dispatchers.IO) {
                        emit(Resource.Loading())
                        try {
                            emit(repo.getSuratDisposisi(unit))
                        } catch (e: Exception) {
                            emit(Resource.Failure(e))
                        }
                    }
            listSuratDisposisi.addSource(liveData) {listSuratDisposisi.postValue(it)}
        }
    }

    fun jawabDisposisi(thisUnit: Int, suratDisposisi: SuratDisposisi, jawaban: String) {
        resultJawab.value = Resource.Loading()
        viewModelScope.launch {
            resultJawab.value = repo.jawabDisposisi(thisUnit, suratDisposisi, jawaban)
        }
    }

    fun penyelesaianDisposisi(thisUnit: Int, suratDisposisi: SuratDisposisi) {
        resultPenyelesaian.value = Resource.Loading()
        viewModelScope.launch {
            try {
                resultPenyelesaian.value = repo.penyelesaianDisposisi(thisUnit, suratDisposisi)
            } catch (e: Exception) {
                resultPenyelesaian.value = Resource.Failure(e)
            }
        }
    }

    fun getDetailDisposisi(suratDisposisi: SuratDisposisi) {
        viewModelScope.launch {
            liveData(Dispatchers.IO) {
                emit(Resource.Loading())
                try {
                    emit(repo.detailDisposisi(suratDisposisi))
                } catch (e: Exception) {
                    Log.i("Detail Disposisi: ", e.message.toString())
                    emit(Resource.Failure(e))
                }
            }.also { resultDetailDisposisi = it }
        }
    }

    fun tambahNomorSurat(draftSurat: DraftSurat) {
        resultTambahNomor.value = Resource.Loading()
        viewModelScope.launch {
            try {
                resultTambahNomor.value = repo.tambahNomorSurat(draftSurat)
            } catch (e: Exception) {
                resultTambahNomor.value = Resource.Failure(e)
            }
        }
    }

}