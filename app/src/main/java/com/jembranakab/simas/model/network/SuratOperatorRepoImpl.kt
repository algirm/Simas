package com.jembranakab.simas.model.network

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.Notifikasi
import com.jembranakab.simas.model.entities.Surat
import com.jembranakab.simas.model.entities.SuratOpd
import com.jembranakab.simas.model.network.base.SuratOperatorRepo
import com.jembranakab.simas.utilities.App
import com.jembranakab.simas.utilities.DataConverter
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class SuratOperatorRepoImpl : SuratOperatorRepo {

    private val suratRef = FirebaseFirestore.getInstance().collection("surat")
    private val suratOpdRef = FirebaseFirestore.getInstance().collection("suratopd")
    private val notifikasiRef = FirebaseFirestore.getInstance().collection("notifikasi")

    override suspend fun tambahArsipDB(surat: Surat, isOutbox: Boolean): Resource<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val opdId = DataConverter.getOpdId(surat.dari!!)

            if (isOffline()) throw Exception("No Internet Connection")

            val taskCreateSurat = suratRef
                    .add(surat)
                    .await()
            taskCreateSurat
                    .update("id", taskCreateSurat.id)
                    .await()

            val suratOpd = SuratOpd()
            suratOpd.at_unit = surat.dari!!.unaryMinus()
            suratOpd.from_unit = surat.dari
            suratOpd.idsurat = taskCreateSurat.id
            suratOpd.status = if (isOutbox) 0 else 1
            suratOpd.last_modified = Timestamp.now()

            val arsipRef = if (isOutbox) "arsipkeluar" else "arsipmasuk"

            val taskSuratOpd = suratOpdRef
                    .document(opdId)
                    .collection(arsipRef)
                    .add(suratOpd)
                    .await()
            taskSuratOpd
                    .update("id", taskSuratOpd.id)
                    .await()

            Resource.Success(taskSuratOpd.id)
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    override suspend fun tambahArsipMasukDB(surat: Surat, tembusan: ArrayList<Int>): Resource<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val opdIdTujuan = DataConverter.getOpdId(surat.kepada!!)

            if (isOffline()) throw Exception("No Internet Connection")

            // surat untuk notifikasi

            val taskCreateSurat = suratRef
                    .add(surat)
                    .await()
            taskCreateSurat
                    .update("id", taskCreateSurat.id)
                    .await()

            val suratOpd = SuratOpd()
            suratOpd.at_unit = surat.kepada!!.unaryMinus()
            suratOpd.from_unit = surat.kepada
            suratOpd.idsurat = taskCreateSurat.id
            suratOpd.status = 1
            suratOpd.last_modified = Timestamp.now()
            suratOpd.isForward = false

            val taskSuratOp = suratOpdRef
                    .document(opdIdTujuan)
                    .collection("arsipmasuk")
                    .add(suratOpd)
                    .await()
            taskSuratOp
                    .update("id", taskSuratOp.id)
                    .await()

            suratOpd.at_unit = surat.kepada
            suratOpd.id = taskSuratOp.id

            // kirim ke tujuan utama
            suratOpdRef.document(opdIdTujuan)
                    .collection("suratmasuk")
                    .document(taskSuratOp.id)
                    .set(suratOpd)
                    .await()

            // kirim notifikasi
            val notifikasi = Notifikasi(
                    null,
                    suratOpd.from_unit,
                    false,
                    surat,
                    App.SURAT_MASUK,
                    null,
                    Timestamp.now()
            )
            kirimNotifikasi(notifikasi, suratOpd.at_unit.toString())

            if (tembusan.isNotEmpty()) {
                for (org in tembusan) {
                    val opdTembusan = DataConverter.getOpdId(org)
                    suratOpd.at_unit = org
                    suratOpd.isForward = true

                    suratOpdRef.document(opdTembusan)
                            .collection("suratmasuk")
                            .document(taskSuratOp.id)
                            .set(suratOpd)
                            .await()

                    // kirim notifikasi
                    val notifikasiTembusan = Notifikasi(
                            null,
                            suratOpd.from_unit,
                            false,
                            surat,
                            App.SURAT_MASUK,
                            null,
                            Timestamp.now(),
                            "Tembusan"
                    )
                    kirimNotifikasi(notifikasiTembusan, org.toString())
                }
            }

            val penerima = arrayListOf<Int>()
            penerima.add(surat.kepada!!)
            penerima.addAll(tembusan)
            val taskUpdatePengirim = suratOpdRef.document(opdIdTujuan)
                    .collection("arsipmasuk")
                    .document(taskSuratOp.id)
            taskUpdatePengirim
                    .update("penerima", penerima)
                    .await()

            Resource.Success("Sukses Tambah Arsip Masuk")
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    override suspend fun fetchSuratDB(unit: Int, isOutbox: Boolean): Resource<MutableList<SuratOpd>> {
        val resultList = arrayListOf<SuratOpd>()
        val opdId = DataConverter.getOpdId(unit)
        val ref = if (isOutbox) "arsipkeluar" else "arsipmasuk"

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

    override suspend fun updateArsipDB(suratOpd: SuratOpd, isOutbox: Boolean): Resource<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val arsipRef = if (isOutbox) "arsipkeluar" else "arsipmasuk"
            val opdId = DataConverter.getOpdId(suratOpd.at_unit!!)

            if (isOffline()) throw Exception("No Internet Connection")

            suratOpdRef.document(opdId)
                    .collection(arsipRef)
                    .document(suratOpd.id!!)
                    .set(suratOpd)
                    .await()

            Resource.Success("Sukses")
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    override suspend fun kirimKeluarDB(
            suratOpd: SuratOpd,
            terusan: ArrayList<Int>
    ): Resource<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val opdIdPengirim = DataConverter.getOpdId(suratOpd.at_unit!!)
            val opdIdTujuan = DataConverter.getOpdId(suratOpd.getSurat()!!.kepada!!)

            if (isOffline()) throw Exception("No Internet Connection")

            // surat untuk notifikasi
            val surat = suratRef.whereEqualTo("id", suratOpd.idsurat)
                    .get()
                    .await()
                    .documents[0]
                    .toObject(Surat::class.java)

            // task kirim ke tujuan surat
            suratOpd.status = 1
            suratOpd.at_unit = suratOpd.getSurat()!!.kepada
            suratOpd.isForward = false

            suratOpdRef
                    .document(opdIdTujuan)
                    .collection("suratmasuk")
                    .document(suratOpd.id!!)
                    .set(suratOpd)
                    .await()

            // kirim notifikasi
            val notifikasi = Notifikasi(
                    null,
                    suratOpd.from_unit,
                    false,
                    surat,
                    App.SURAT_MASUK,
                    null,
                    Timestamp.now()
            )
            kirimNotifikasi(notifikasi, suratOpd.at_unit.toString())

            // task kirim ke terusan jika ada
            if (terusan.isNotEmpty()) {
                for (org in terusan) {
                    val opdIdTerusan = DataConverter.getOpdId(org)
                    suratOpd.at_unit = org
                    suratOpd.isForward = true

                    suratOpdRef.document(opdIdTerusan)
                            .collection("suratmasuk")
                            .document(suratOpd.id!!)
                            .set(suratOpd)
                            .await()

                    // kirim notifikasi
                    val notifikasiTerusan = Notifikasi(
                            null,
                            suratOpd.from_unit,
                            false,
                            surat,
                            App.SURAT_MASUK,
                            null,
                            Timestamp.now(),
                            "Terusan"
                    )
                    kirimNotifikasi(notifikasiTerusan, org.toString())
                }
            }

            // task update status dan penerima surat di opd pengirim
            val penerima = arrayListOf<Int>()
            penerima.add(suratOpd.getSurat()!!.kepada!!)
            penerima.addAll(terusan)
            val taskUpdatePengirim = suratOpdRef.document(opdIdPengirim)
                    .collection("arsipkeluar")
                    .document(suratOpd.id!!)
            taskUpdatePengirim
                    .update("penerima", penerima)
                    .await()
            taskUpdatePengirim
                    .update("status", 1)
                    .await()
            taskUpdatePengirim
                    .update("last_modified", Timestamp.now())
                    .await()

            Resource.Success("Sukses Kirim Surat Keluar")
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    override suspend fun detailPenerimaDB(suratOpdId: String, penerima: ArrayList<Int>)
            : Resource<MutableList<Map<String, String>>> {
        val resultList = arrayListOf<Map<String, String>>()

        for (p in penerima) {
            val opdId = DataConverter.getOpdId(p)
            val task = suratOpdRef
                    .document(opdId)
                    .collection("suratmasuk")
                    .document(suratOpdId)
                    .get()
                    .await()
            val map = HashMap<String, String>()
            map["penerima"] = DataConverter.getNamaOrgFromTopLayer(p)
            map["status"] = DataConverter.getStatus(task.get("status", Int::class.java)!!)
            resultList.add(map)
        }

        return Resource.Success(resultList)
    }

    private suspend fun kirimNotifikasi(notifikasi: Notifikasi, receiver: String) = withContext(Dispatchers.IO) {
        val taskNotif = notifikasiRef.document(receiver)
                .collection("notifikasi")
                .add(notifikasi)
                .await()
        taskNotif.update("id", taskNotif.id).await()
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