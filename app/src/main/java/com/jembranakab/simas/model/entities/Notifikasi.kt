package com.jembranakab.simas.model.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class Notifikasi(
        var id: String? = null,
        var fromUnit: Int? = null,
        var read: Boolean = false,
        var surat: Surat? = null,
        var tipe: Int? = null,
        var readAt: Timestamp? = null,
        var receiveAt: Timestamp? = null,
        var keterangan: String? = null,
        private var expanded: Boolean = false
) : Serializable {

    @Exclude
    fun isExpanded(): Boolean = expanded

    @Exclude
    fun setExpanded(bool: Boolean) {
        expanded = bool
    }

}
