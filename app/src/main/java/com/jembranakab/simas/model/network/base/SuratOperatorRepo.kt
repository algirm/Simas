package com.jembranakab.simas.model.network.base

import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.Surat
import com.jembranakab.simas.model.entities.SuratOpd

interface SuratOperatorRepo {
    suspend fun tambahArsipDB(surat: Surat, isOutbox: Boolean): Resource<String>
    suspend fun tambahArsipMasukDB(surat: Surat, tembusan: ArrayList<Int>): Resource<String>
    suspend fun fetchSuratDB(unit: Int, isOutbox: Boolean): Resource<MutableList<SuratOpd>>
    suspend fun updateArsipDB(suratOpd: SuratOpd, isOutbox: Boolean): Resource<String>
    suspend fun kirimKeluarDB(suratOpd: SuratOpd, terusan: ArrayList<Int>): Resource<String>
    suspend fun detailPenerimaDB(suratOpdId: String, penerima: ArrayList<Int>): Resource<MutableList<Map<String, String>>>
}