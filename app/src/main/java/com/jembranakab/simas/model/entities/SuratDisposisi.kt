package com.jembranakab.simas.model.entities

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.io.Serializable

class SuratDisposisi() : Serializable, Parcelable {
    /**
     *
     * status code
     * 0 = belum dikirim/masih berada di unit terbawah
     * 1 = diterima/belum didisposisi
     * 2 = didisposisi ke bawahan
     * 3 = disposisi diterima
     * 4 = sudah dijawab
     * 5 = sudah selesai
     *
     */
    var from_unit: Int? = null
    var at_unit: Int? = null
    var to_unit = arrayListOf<Int>()
    var selfDispose: Boolean? = null
    var status: Int? = null
    var id: String? = null
    var idsurat: String? = null
    var last_modified: Timestamp? = null
    var isiRingkas: String = ""
    var instruksi: String = ""
    var tambahanInstruksi: String = ""
    var jawaban: JawabanDisposisi? = null

    private var surat: Surat? = null

    private var expanded = false

    constructor(parcel: Parcel) : this() {
        from_unit = parcel.readValue(Int::class.java.classLoader) as? Int
        at_unit = parcel.readValue(Int::class.java.classLoader) as? Int
        selfDispose = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        status = parcel.readValue(Int::class.java.classLoader) as? Int
        id = parcel.readString()
        idsurat = parcel.readString()
        last_modified = parcel.readParcelable(Timestamp::class.java.classLoader)
        isiRingkas = parcel.readString().toString()
        instruksi = parcel.readString().toString()
        tambahanInstruksi = parcel.readString().toString()
        expanded = parcel.readByte() != 0.toByte()
    }

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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(from_unit)
        parcel.writeValue(at_unit)
        parcel.writeValue(selfDispose)
        parcel.writeValue(status)
        parcel.writeString(id)
        parcel.writeString(idsurat)
        parcel.writeParcelable(last_modified, flags)
        parcel.writeString(isiRingkas)
        parcel.writeString(instruksi)
        parcel.writeString(tambahanInstruksi)
        parcel.writeByte(if (expanded) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SuratDisposisi> {
        override fun createFromParcel(parcel: Parcel): SuratDisposisi {
            return SuratDisposisi(parcel)
        }

        override fun newArray(size: Int): Array<SuratDisposisi?> {
            return arrayOfNulls(size)
        }
    }

}