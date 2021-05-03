package com.jembranakab.simas.model.entities

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.io.Serializable

/**
 * status code :
 * - 0 = Belum DiProses
 */

data class DraftSurat(
    var id: String? = null,
    var surat: Surat? = null,
    var status: Int? = null,
    var pengirim: Int? = null,
    var asalDraft: Int? = null,
    var penerima: Int? = null,
    var lastModified: Timestamp? = null,
    var keterangan: String? = null,
    private var expanded: Boolean = false
) : Serializable, Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable(Surat::class.java.classLoader),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    @Exclude
    fun isExpanded(): Boolean = expanded

    @Exclude
    fun setExpanded(bool: Boolean) {
        expanded = bool
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeParcelable(surat, flags)
        parcel.writeValue(status)
        parcel.writeValue(pengirim)
        parcel.writeValue(asalDraft)
        parcel.writeValue(penerima)
        parcel.writeParcelable(lastModified, flags)
        parcel.writeString(keterangan)
        parcel.writeByte(if (expanded) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DraftSurat> {
        override fun createFromParcel(parcel: Parcel): DraftSurat {
            return DraftSurat(parcel)
        }

        override fun newArray(size: Int): Array<DraftSurat?> {
            return arrayOfNulls(size)
        }
    }

}