package com.jembranakab.simas.model.entities

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class User() : Serializable, Parcelable {
    var uid: String? = null
    var email: String? = null
    var unit: Int? = null

    constructor(parcel: Parcel) : this() {
        uid = parcel.readString()
        email = parcel.readString()
        unit = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(email)
        parcel.writeValue(unit)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}
