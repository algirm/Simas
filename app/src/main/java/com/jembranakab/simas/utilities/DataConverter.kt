package com.jembranakab.simas.utilities

import com.jembranakab.simas.model.JsonOrg
import com.jembranakab.simas.model.entities.SuratDisposisi
import com.jembranakab.simas.model.entities.SuratOpd

class DataConverter {

    companion object {

        private val orgTopLayer = JsonOrg.getAllTopLayer()
        private val orgAll = JsonOrg.getAllOrg()

        fun getAtasanUnitExceptTopUnit(thisUnit: Int): Int {
            var atasan = ""
            val thisUnitCharArray = thisUnit.toString().toCharArray()
            for (i in 0 until thisUnitCharArray.size - 1) {
                atasan += thisUnitCharArray[i]
            }
            return atasan.toInt()
        }

        fun getKategori(kategori: Int): String {
            return when (kategori) {
                0 -> "Biasa"
                1 -> "Segera"
                2 -> "Rahasia"
                else -> ""
            }
        }

        fun getStatusOp(status: Int): String {
            return when (status) {
                0 -> "Belum Diproses"
                1 -> "Telah Terkirim"
                2 -> "Telah Didisposisi"
                else -> "Sudah Diproses"
            }
        }

        fun getStatus(status: Int): String {
            return when (status) {
                0 -> "Belum Diproses"
                1 -> "Belum Didisposisi"
                2 -> "Telah Didisposisi"
                else -> "Sudah Diproses"
            }
        }

        fun getStatusDisposisi(status: Int): String {
            return when (status) {
                1 -> "Belum Diproses"
                2 -> "Didisposisi Ke Bawah"
                3 -> "Belum Selesai"
                4 -> "Sudah Dijawab"
                5 -> "Sudah Selesai"
                else -> "Sudah Diproses"
            }
        }

        fun getNamaOrgFromTopLayer(unit: Int): String {
            for (data in orgTopLayer) {
                if (unit == data.unit) {
                    return data.nama.toString()
                }
            }
            return ""
        }

        fun getNamaOrgFromAll(unit: Int): String {
            for (data in orgAll) {
                if (unit == data.unit) {
                    return data.nama.toString()
                }
            }
            return ""
        }

        fun getOpdId(unit: Int): String {
            val opdId: String
            val ca = unit.toString().toCharArray()
            opdId = if (ca[0].toString() == "-") {
                if (ca.size > 2) {
                    "${ca[1]}${ca[2]}999"
                } else {
                    "${ca[1]}999"
                }
            } else {
                if (ca.size > 1) {
                    "${ca[0]}${ca[1]}999"
                } else {
                    "${ca[0]}999"
                }
            }
            return opdId
        }

        fun getOrgTopOfThisUnit(unit: Int): Int {
            val ca = unit.toString().toCharArray()
            return (ca[0].toString() + ca[1].toString()).toInt()
        }

        fun convertToSuratDisposisi(suratOpd: SuratOpd): SuratDisposisi {
            val suratDisposisi = SuratDisposisi()
            suratDisposisi.at_unit = suratOpd.at_unit
            suratDisposisi.from_unit = suratOpd.from_unit
            suratDisposisi.id = suratOpd.id
            suratDisposisi.idsurat = suratOpd.idsurat
            suratDisposisi.last_modified = suratOpd.last_modified
            suratDisposisi.status = suratOpd.status
            suratDisposisi.setSurat(suratOpd.getSurat())
            return suratDisposisi
        }

    }

}