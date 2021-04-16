package com.jembranakab.simas.model.entities

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.io.Serializable

class Surat(
        var createBy: Int? = null,
        var createdAt: Timestamp? = null,
        var nomor: String? = null,
        var perihal: String? = null,
        var id: String? = null,
        var kategori: Int?= null,
        var perincian: String? = null,
        var kepada: Int? = null,
        var dari: Int? = null,
        var tanggal: String? = null,
        var lampiran: String? = null,
        var pdfUrl: String? = null,
        var namaPdf: String? = null,
        var namaDinasLuar: String? = null
) : Serializable, Parcelable {

    private var expanded = false

    constructor(parcel: Parcel) : this(
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readParcelable(Timestamp::class.java.classLoader),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readString(),
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
        expanded = parcel.readByte() != 0.toByte()
    }

    @Exclude
    fun isExpanded(): Boolean = expanded

    @Exclude
    fun setExpanded(bool: Boolean) {
        expanded = bool
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(createBy)
        parcel.writeParcelable(createdAt, flags)
        parcel.writeString(nomor)
        parcel.writeString(perihal)
        parcel.writeString(id)
        parcel.writeValue(kategori)
        parcel.writeString(perincian)
        parcel.writeValue(kepada)
        parcel.writeValue(dari)
        parcel.writeString(tanggal)
        parcel.writeString(lampiran)
        parcel.writeString(pdfUrl)
        parcel.writeString(namaPdf)
        parcel.writeString(namaDinasLuar)
        parcel.writeByte(if (expanded) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Surat> {
        override fun createFromParcel(parcel: Parcel): Surat {
            return Surat(parcel)
        }

        override fun newArray(size: Int): Array<Surat?> {
            return arrayOfNulls(size)
        }
    }

}

