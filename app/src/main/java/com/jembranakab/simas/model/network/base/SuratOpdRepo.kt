package com.jembranakab.simas.model.network.base

import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

interface SuratOpdRepo {
    suspend fun getSuratOpd(unit: Int, isOutbox: Boolean): Resource<MutableList<SuratOpd>>
    suspend fun getSuratDisposisi(unit: Int): Resource<MutableList<SuratDisposisi>>
    suspend fun createNew(surat: Surat, senderUnit: Int): Resource<String>
    suspend fun disposisi(kepada: List<Int>, suratDisposisi: SuratDisposisi): Resource<String>
    suspend fun jawabDisposisi(thisUnit: Int, suratDisposisi: SuratDisposisi, jawaban: String): Resource<String>
    suspend fun penyelesaianDisposisi(thisUnit: Int, suratDisposisi: SuratDisposisi): Resource<String>
    suspend fun detailDisposisi(suratDisposisi: SuratDisposisi): Resource<Map<String, Any>>
    suspend fun tambahNomorSurat(surat: Surat): Resource<String>
    suspend fun getNomorSurat(thisUnit: Int): Resource<MutableList<Surat>>
}