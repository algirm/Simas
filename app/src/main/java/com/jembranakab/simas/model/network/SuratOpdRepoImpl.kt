package com.jembranakab.simas.model.network

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.*
import com.jembranakab.simas.model.network.base.SuratOpdRepo
import com.jembranakab.simas.utilities.App.Companion.DISPOSISI
import com.jembranakab.simas.utilities.App.Companion.JAWABAN
import com.jembranakab.simas.utilities.App.Companion.PENYELESAIAN
import com.jembranakab.simas.utilities.DataConverter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class SuratOpdRepoImpl : SuratOpdRepo {
    private val suratRef = FirebaseFirestore.getInstance().collection("surat")
    private val suratOpdRef = FirebaseFirestore.getInstance().collection("suratopd")
    private val disposisiRef = FirebaseFirestore.getInstance().collection("disposisi")
    private val jawabanRef = FirebaseFirestore.getInstance().collection("jawaban")
    private val notifikasiRef = FirebaseFirestore.getInstance().collection("notifikasi")

    override suspend fun getSuratOpd(unit: Int, isOutbox: Boolean): Resource<MutableList<SuratOpd>> {
        val ref = if (isOutbox) "suratkeluar" else "suratmasuk"
        val resultList = arrayListOf<SuratOpd>()
        val opdId = DataConverter.getOpdId(unit)

        val task = suratOpdRef
                .document(opdId)
                .collection(ref)
                .whereEqualTo("at_unit", unit)
                .orderBy("last_modified", Query.Direction.DESCENDING)
                .get()
                .await()

        for (data in task) {
            resultList.add(data.toObject(SuratOpd::class.java))
        }

        if (resultList.isNotEmpty()) {
            for (data in resultList) {
                val taskSurat = suratRef.document(data.idsurat!!).get().await()
                data.setSurat(taskSurat.toObject(Surat::class.java))
            }
        }

        return Resource.Success(resultList)
    }

    override suspend fun getSuratDisposisi(unit: Int): Resource<MutableList<SuratDisposisi>> {
        val opdId = DataConverter.getOpdId(unit)
        val result = arrayListOf<SuratDisposisi>()

        val task = disposisiRef
                .document(opdId)
                .collection(unit.toString())
                .orderBy("last_modified", Query.Direction.DESCENDING)
                .get()
                .await()

        for (data in task) {
            result.add(data.toObject(SuratDisposisi::class.java))
        }

        if (result.isNotEmpty()) {
            for (data in result) {
                val taskSurat = suratRef.document(data.idsurat!!).get().await()
                data.setSurat(taskSurat.toObject(Surat::class.java))
            }
        }

        return Resource.Success(result)
    }

    override suspend fun createNew(surat: Surat, senderUnit: Int): Resource<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val opdId = (senderUnit.toString().toCharArray()[0].toString() +
                    senderUnit.toString().toCharArray()[1].toString()) + "999"

            // dummy database code buat cek koneksi untuk mencegah
            // pending kalo koneksi putus waktu ngirim
            val dummy = FirebaseFirestore.getInstance()
                    .collection("dummy")
                    .get(Source.SERVER)
                    .await()
                    .metadata
                    .hasPendingWrites()
            if (dummy) {
                /* cancel all job, mencegah task dibawah berjalan karena
                   kondisi diatas ga tercapai karena masalah koneksi dll
                */
                this.cancel()
            }

            // main code
            val taskCreateSurat = suratRef
                    .add(surat)
                    .await()
            taskCreateSurat
                    .update("id", taskCreateSurat.id)
                    .await()

            val suratOpd = SuratOpd()
            suratOpd.at_unit = senderUnit
            suratOpd.from_unit = senderUnit
            suratOpd.idsurat = taskCreateSurat.id
            suratOpd.status = 0
            suratOpd.last_modified = Timestamp.now()

            val taskSuratOpd = suratOpdRef
                    .document(opdId)
                    .collection("suratkeluar")
                    .add(suratOpd)
                    .await()
            taskSuratOpd.update("id", taskSuratOpd.id)

            Resource.Success(taskSuratOpd.id)
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    override suspend fun disposisi(kepada: List<Int>, suratDisposisi: SuratDisposisi): Resource<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val opdId = DataConverter.getOpdId(suratDisposisi.at_unit!!)
            val arrayToUnit = arrayListOf<Int>()

            if (isOffline()) throw Exception("No Internet Connection")

            suratDisposisi.last_modified = Timestamp.now()
            val thisUnit = suratDisposisi.at_unit

            // jika kepada kosong, disposisi ke sendiri
            if (kepada.isEmpty()) {
                suratDisposisi.from_unit = thisUnit
                suratDisposisi.status = 3
                suratDisposisi.selfDispose = true
                disposisiRef.document(opdId)
                        .collection(suratDisposisi.at_unit.toString())
                        .document(suratDisposisi.id!!)
                        .set(suratDisposisi)
                        .await()
            } else {
                val surat = suratRef.whereEqualTo("id", suratDisposisi.idsurat)
                        .get()
                        .await()
                        .documents[0]
                        .toObject(Surat::class.java)
                suratDisposisi.selfDispose = false
                suratDisposisi.status = if (kepada[0] > 10000) 3 else 1
                val previousFrom = suratDisposisi.from_unit
                suratDisposisi.from_unit = thisUnit

                // write to disposisi kepada
                var keteranganNotif: String
                for (org in kepada) {
                    keteranganNotif = "- ${DataConverter.getNamaOrgFromAll(org)}\n"
                    if (org > 10000)
                        suratDisposisi.selfDispose = true

                    suratDisposisi.at_unit = org
                    disposisiRef.document(opdId)
                            .collection(suratDisposisi.at_unit.toString())
                            .document(suratDisposisi.id!!)
                            .set(suratDisposisi)
                            .await()
                    // kirim notifikasi
                    val notifikasi = Notifikasi(
                            null,
                            thisUnit,
                            false,
                            surat,
                            DISPOSISI,
                            null,
                            Timestamp.now(),
                            keteranganNotif
                    )
                    kirimNotifikasi(notifikasi, org.toString())
                    arrayToUnit.add(org)
                }

                // write in pengirim
                suratDisposisi.selfDispose = false
                suratDisposisi.status = 2
                suratDisposisi.at_unit = thisUnit
                suratDisposisi.to_unit = arrayToUnit
                suratDisposisi.from_unit = previousFrom
                disposisiRef.document(opdId)
                        .collection(suratDisposisi.at_unit.toString())
                        .document(suratDisposisi.id!!)
                        .set(suratDisposisi)
                        .await()
            }

            // update status surat masuk ketika disposisi pertama kali dari org top
            if (thisUnit!! < 100) {
                suratOpdRef.document(opdId)
                        .collection("suratmasuk")
                        .document(suratDisposisi.id!!)
                        .update("status", 2)
                        .await()
            }

            Resource.Success("")
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    override suspend fun jawabDisposisi(
            thisUnit: Int,
            suratDisposisi: SuratDisposisi,
            jawaban: String): Resource<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val opdId = DataConverter.getOpdId(thisUnit)

            if (isOffline()) throw Exception("No Internet Connection")

            val jawabanDisposisi = JawabanDisposisi()
            jawabanDisposisi.id = suratDisposisi.id
            jawabanDisposisi.waktuJawab = Timestamp.now()
            jawabanDisposisi.jawaban = jawaban

            // write to jawaban database
            val ca = thisUnit.toString().toCharArray()
            if (!jawabanRef.document(suratDisposisi.id!!).get().await().exists()) {
                jawabanRef.document(suratDisposisi.id!!)
                        .set(mapOf<String, Any>())
                        .await()
            }
            var org = ""
            if (ca.size > 2) {
                for (i in ca.indices) {
                    if (i > ca.size - 2) break
                    org += ca[i].toString()
                }
            }
            if (org.isNotBlank()) {
                val map = HashMap<String, Any>()
                map[thisUnit.toString()] = jawabanDisposisi
                jawabanRef.document(suratDisposisi.id!!)
                        .update(org, map)
                        .await()
                // write to disposisi database
                disposisiRef.document(opdId)
                        .collection(thisUnit.toString())
                        .document(suratDisposisi.id!!)
                        .update("jawaban", jawabanDisposisi)
                        .await()

                // kirim notifikasi
                val surat = suratRef.whereEqualTo("id", suratDisposisi.idsurat)
                        .get()
                        .await()
                        .documents[0]
                        .toObject(Surat::class.java)
                val notifikasi = Notifikasi(
                        null,
                        thisUnit,
                        false,
                        surat,
                        JAWABAN,
                        null,
                        Timestamp.now(),
                        jawabanDisposisi.jawaban
                )
                kirimNotifikasi(notifikasi, org)
            }

            Resource.Success("")
        } catch (e: Exception) {
            Log.i("Jawab Disposisi: ", e.message.toString())
            Resource.Failure(e)
        }
    }

    override suspend fun detailDisposisi(suratDisposisi: SuratDisposisi): Resource<Map<String, Any>> {
        val map = hashMapOf<String, Any>()
        val thisUnit = suratDisposisi.at_unit
        val ca = thisUnit.toString().toCharArray()
        val atasan =
                if (ca.size > 2) {
                    var ats = ""
                    for (i in ca.indices - 1) {
                        ats += ca[i]
                    }
                    ats
                } else {
                    ca[0].toString() + ca[1].toString()
                }

        map["dari"] = DataConverter.getNamaOrgFromAll(suratDisposisi.from_unit!!)
        map["waktu"] = SimpleDateFormat("HH:mm E dd MMM yyyy", Locale.ROOT)
                .format(suratDisposisi.last_modified!!.toDate())
        map["instruksi"] = suratDisposisi.instruksi
        map["tambahan"] = suratDisposisi.tambahanInstruksi
        val arrayKepada = arrayListOf<Map<String, String>>()

        if (suratDisposisi.selfDispose == true) {
            val mapKepada = hashMapOf<String, String>()
            mapKepada["kepada"] = DataConverter.getNamaOrgFromAll(suratDisposisi.at_unit!!)
            val docJawaban = jawabanRef
                    .document(suratDisposisi.id!!)
                    .get()
                    .await()[atasan]
            if (docJawaban != null) {
                val mapOrg = docJawaban as Map<*, *>
                val mapJawaban = mapOrg[thisUnit.toString()] as Map<*, *>
                val mapPenyelesaian = mapJawaban["penyelesaian"] as Map<*, *>?
                val waktuJawab = mapJawaban["waktuJawab"] as Timestamp?
                mapKepada["waktuJawab"] =
                        if (waktuJawab != null) {
                            SimpleDateFormat("HH:mm E dd MMM yyyy", Locale.ROOT).format(waktuJawab.toDate())
                        } else {
                            ""
                        }
                mapKepada["jawaban"] = (mapJawaban["jawaban"] as String?)?: ""
                mapKepada["keterangan"] =
                        if (mapPenyelesaian != null) {
                            mapPenyelesaian["keterangan"] as String
                        } else {
                            ""
                        }
            }

            arrayKepada.add(mapKepada)
        } else {
            for (org in suratDisposisi.to_unit) {
                val mapKepada = hashMapOf<String, String>()
                mapKepada["kepada"] = DataConverter.getNamaOrgFromAll(org)
                val docJawaban = jawabanRef
                        .document(suratDisposisi.id!!)
                        .get()
                        .await()[thisUnit.toString()]
                if (docJawaban != null) {
                    val mapOrg = docJawaban as Map<*, *>
                    val mapJawaban = mapOrg[org.toString()] as Map<*, *>?
                    val mapPenyelesaian = mapJawaban?.get("penyelesaian") as Map<*, *>?
                    val waktuJawab = mapJawaban?.get("waktuJawab") as Timestamp?
                    mapKepada["waktuJawab"] =
                            if (waktuJawab != null) {
                                SimpleDateFormat("HH:mm E dd MMM yyyy", Locale.ROOT).format(waktuJawab.toDate())
                            } else {
                                ""
                            }
                    mapKepada["jawaban"] = (mapJawaban?.get("jawaban") as String?)?: ""
                    mapKepada["keterangan"] =
                            if (mapPenyelesaian != null) {
                                mapPenyelesaian["keterangan"] as String
                            } else {
                                ""
                            }
                }

                arrayKepada.add(mapKepada)
            }
        }

        map["kepada"] = arrayKepada

        return Resource.Success(map)
    }

    override suspend fun penyelesaianDisposisi(
        thisUnit: Int,
        suratDisposisi: SuratDisposisi
    ): Resource<String>  {
        val opdId = DataConverter.getOpdId(thisUnit)

        if (isOffline()) throw Exception("No Internet Connection")

        // write to jawaban database
        val ca = thisUnit.toString().toCharArray()
        if (!jawabanRef.document(suratDisposisi.id!!).get().await().exists()) {
            jawabanRef.document(suratDisposisi.id!!)
                    .set(mapOf<String, Any>())
                    .await()
        }
        var atasan = ""
        if (ca.size > 2) {
            for (i in ca.indices) {
                if (i > ca.size - 2) break
                atasan += ca[i].toString()
            }
        } else {
            atasan = ca[0].toString() + ca[1].toString()
        }
        if (atasan.isNotBlank()) {
            val map = HashMap<String, Any>()
            map[thisUnit.toString()] = suratDisposisi.jawaban!!
            if (atasan == thisUnit.toString()) {
                // jabatan teratas menindaklanjuti dan menyelesaikan sendiri
                jawabanRef.document(suratDisposisi.id!!)
                        .update(atasan, map)
                        .await()
            } else {
                jawabanRef.document(suratDisposisi.id!!)
                        .update(atasan, map)
                        .await()
            }

            // write to disposisi database
            val mapUpdate = HashMap<String, Any>()
            mapUpdate["status"] = 5
            mapUpdate["jawaban"] = suratDisposisi.jawaban!!
            disposisiRef.document(opdId)
                    .collection(thisUnit.toString())
                    .document(suratDisposisi.id!!)
                    .update(mapUpdate)
                    .await()

            // kirim notifikasi
            val surat = suratRef.whereEqualTo("id", suratDisposisi.idsurat)
                    .get()
                    .await()
                    .documents[0]
                    .toObject(Surat::class.java)
            val notifikasi = Notifikasi(
                    null,
                    thisUnit,
                    false,
                    surat,
                    PENYELESAIAN,
                    null,
                    Timestamp.now(),
                    suratDisposisi.jawaban?.penyelesaian?.get("keterangan")
            )
            kirimNotifikasi(notifikasi, atasan)
        }
        return Resource.Success("")
    }

    private suspend fun kirimNotifikasi(notifikasi: Notifikasi, receiver: String) = withContext(Dispatchers.IO) {
        val taskNotif = notifikasiRef.document(receiver)
                .collection("notifikasi")
                .add(notifikasi)
                .await()
        taskNotif.update("id", taskNotif.id).await()
    }

    override suspend fun tambahNomorSurat(surat: Surat): Resource<String> = withContext(Dispatchers.IO) {
        if (isOffline()) throw Exception("No Internet Connection")

        val taskTambah = suratRef
                .add(surat)
                .await()
        taskTambah
                .update("id", taskTambah.id)
                .await()

        return@withContext Resource.Success("")
    }

    override suspend fun getNomorSurat(thisUnit: Int): Resource<MutableList<Surat>> {
        val resultList = arrayListOf<Surat>()

        val task = suratRef
                .whereEqualTo("createBy", thisUnit)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

        for (data in task) {
            resultList.add(data.toObject(Surat::class.java))
        }

        return Resource.Success(resultList)
    }

    private suspend fun isOffline(): Boolean {
        // dummy database code buat cek koneksi untuk mencegah
        // pending kalo koneksi putus waktu ngirim
        return try {
            FirebaseFirestore.getInstance()
                    .collection("dummy")
                    .get(Source.SERVER)
                    .await()
                    .metadata
                    .hasPendingWrites()
            false
        } catch (e: Exception) {
            true
        }
    }

}