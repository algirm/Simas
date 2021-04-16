package com.jembranakab.simas.model.entities

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class Organisasi(var nama: String?, var unit: Int?) : Serializable, Parcelable {
//    val nama: String? = string
//    val unit: Int? = int

    var isChecked = false

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readValue(Int::class.java.classLoader) as? Int) {
        isChecked = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nama)
        parcel.writeValue(unit)
        parcel.writeByte(if (isChecked) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Organisasi> {
        override fun createFromParcel(parcel: Parcel): Organisasi {
            return Organisasi(parcel)
        }

        override fun newArray(size: Int): Array<Organisasi?> {
            return arrayOfNulls(size)
        }
    }
}