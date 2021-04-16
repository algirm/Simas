package com.jembranakab.simas.model.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.io.Serializable

class SuratOpd : Serializable {
    /**
     *
     * status code
     * 0 = belum dikirim/masih berada di unit terbawah
     * 1 = diterima/belum didisposisi
     * 2 = telah didisposisi
     * 3 = koreksi
     * 4 = telah di ttd
     *
     */
    var from_unit: Int? = null
    var at_unit: Int? = null
    var status: Int? = null
    var id: String? = null
    var idsurat: String? = null
    var last_modified: Timestamp? = null
    var isForward: Boolean = false
    var penerima = arrayListOf<Int>()

    private var surat: Surat? = null

    private var expanded = false

    @Exclude
    fun getSurat(): Surat? {
        return this.surat
    }

    @Exclude
    fun setSurat(surat: Surat?) {
        this.surat = surat
    }

    @Exclude
    fun isExpanded(): Boolean = expanded

    @Exclude
    fun setExpanded(bool: Boolean) {
        expanded = bool
    }

}