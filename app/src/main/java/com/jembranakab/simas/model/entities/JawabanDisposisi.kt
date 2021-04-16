package com.jembranakab.simas.model.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.io.Serializable

class JawabanDisposisi : Serializable {

    var id: String? = null
    var jawaban: String? = null
    var penyelesaian: Map<String, String>? = null
    var waktuJawab: Timestamp? = null

}