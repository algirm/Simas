package com.jembranakab.simas

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jembranakab.simas.model.JsonOrg
import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.Organisasi
import com.jembranakab.simas.model.factories.SubjectVMFactory
import com.jembranakab.simas.model.network.SubjectRepoImpl
import com.jembranakab.simas.model.viewmodel.SubjectViewModel
import com.jembranakab.simas.utilities.App.Companion.TAG
import com.jembranakab.simas.utilities.DataConverter
import kotlinx.android.synthetic.main.activity_login2.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {

    private val subjectViewModel by viewModels<SubjectViewModel> { SubjectVMFactory(SubjectRepoImpl()) }
    private lateinit var observer: Observer<Resource<Organisasi>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)
        keyboardAdjust()
        initActionGo()
        initButtonListener()
//        inputOrganisasi()
    }

    private fun inputOrganisasi() {
        val userRef = FirebaseFirestore.getInstance().collection("users")
        val auth = FirebaseAuth.getInstance()

        var dokumen = ""
        val jsonList = (parseMyJson() as JsonArray<*>)
            .filterIsInstance<JsonObject>()
        val result = arrayListOf<Organisasi>()
        for (data in jsonList) {
            result.add(Organisasi(data.string("nama"), data.int("unit")))
        }
        result.forEach {
            val nama = it.nama
            val unit = it.unit

            var email = ""
            val splitNama = nama?.split(" ")
            val caUnit = unit.toString().toCharArray()
            when {
                caUnit.size == 2 -> {
                    if (unit!! > 0) {
                        when(splitNama!![0]) {
                            "Badan" -> email += "badan_$unit"
                            "Sekretariat" -> email += "sekretariat_$unit"
                            "Dinas" -> email += "dinas_$unit"
                        }
                    }
                }
                caUnit.size == 3 && caUnit[0] == '-' -> {
                    when(splitNama!![2]) {
                        "Badan" -> email += "badan_${unit!!.unaryMinus()}"
                        "Sekretariat" -> email += "sekretariat_${unit!!.unaryMinus()}"
                        "Dinas" -> email += "dinas_${unit!!.unaryMinus()}"
                    }
                    email = "op$email"
                }
                caUnit.size == 3 && caUnit[0] != '-' -> {
                    if (splitNama!![0] == "Sekretaris") {
                        val opdUnit = caUnit[0].toString() + caUnit[1].toString()
                        val opd = DataConverter.getNamaOrgFromAll(opdUnit.toInt())
                        val splitOpd = opd.split(" ")
                        val opdSingkatan = when(splitOpd[0]) {
                            "Badan" ->  "bad"
                            "Dinas" -> "din"
                            "Sekretariat" -> "set"
                            else -> "opd"
                        }

                        email += "sek$opdSingkatan" + "_" + unit
                    } else {
                        val singkatan = when(splitNama[0]) {
                            "Bidang" -> "bid"
                            "Bagian" -> "bag"
                            else -> "???"
                        }

                        email += singkatan + "_" + unit
                    }
                }
                caUnit.size > 3 -> {
                    val singkatan = when {
                        nama!!.contains("Sekretaris") -> "subagsek"
                        splitNama!![0] == "Sub" && splitNama[1] == "Bidang"-> "subid"
                        splitNama[0] == "Sub" && splitNama[1] == "Bagian"-> "subag"
                        splitNama[0] == "Seksi" -> "seksi"
                        else -> "?????"
                    }

                    email += singkatan + "_" + unit
                }
            }
            val emailSplit = email.split("_")
            val password = emailSplit[0] + emailSplit[1]
//            var password = emailSplit[0]
//            if (emailSplit.size > 1) password += emailSplit[1]

            email += "@simaskab.go.id"
            dokumen += "$nama : $email \"$password\"\n"

            val map = HashMap<String, Any>()
//            GlobalScope.launch {
//                val taskAuth = auth.createUserWithEmailAndPassword(email, password).await()
//                val uid = taskAuth.user?.uid
//                map["uid"] = uid!!
//                map["unit"] = unit!!
//                userRef.document(uid).set(map).await()
//                auth.signOut()
//            }
        }
        println(dokumen)
    }

    private fun parseMyJson(): Any? {
        val cls = Parser::class.java
        return cls.getResourceAsStream("/assets/input-organisasi.json")?.let { inputStream ->
            return Parser.default().parse(inputStream)
        }
    }

    private fun login() {
        setLoadingBar(true)
        var email = login_id.text.toString()
        val password = login_pw.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            if (email.isEmpty()) login_id.error = "Kosong" else login_pw.error = "Kosong"
            setLoadingBar(false)
            return
        }

        if (email.isNotEmpty() && !email.contains("@")) {
            email += "@simaskab.go.id"
        }

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendUserToMainActivity()
                } else {
                    val error = task.exception?.message
                    Toast.makeText(
                        this,
                        "Login Gagal: ${error ?: "Something Wrong"}",
                        Toast.LENGTH_SHORT
                    ).show()
                    setLoadingBar(false)
                }
            }
    }

    private fun sendUserToMainActivity() {
        observer = Observer<Resource<Organisasi>> {
            if (it is Resource.Success) {
                val accessCode = getUserAccessCode(it.data.unit)
                if (accessCode == 0) {
                    val intent = Intent(this, MainActivityOp::class.java)
                    intent.putExtra("thisOrg", it.data.nama)
                    intent.putExtra("thisUnit", it.data.unit)
                    intent.putExtra("accessCode", accessCode)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, MainActivityUser::class.java)
                    intent.putExtra("thisOrg", it.data.nama)
                    intent.putExtra("thisUnit", it.data.unit)
                    intent.putExtra("accessCode", accessCode)
                    startActivity(intent)
                }
                finish()
                setLoadingBar(false)
            } else if (it is Resource.Failure) {
                val text = getString(R.string.no_connection_text)
                Toast.makeText(this, text, Toast.LENGTH_LONG).show()
                setLoadingBar(false)
            }
        }
        subjectViewModel.thisOrgMediatorLiveData.observe(this, observer)
    }

    private fun setLoadingBar(bool: Boolean) {
        if (bool) progressBar.visibility = View.VISIBLE
        else progressBar.visibility = View.INVISIBLE
    }

    private fun initButtonListener() {
        login_button.setOnClickListener { login() }
    }

    private fun initActionGo() {
        login_pw.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_GO) {
                login()
                val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                im.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun keyboardAdjust() {
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        // keyboard adjust
        val rootView = window.decorView.rootView
        val rect = Rect()
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            rootView.getWindowVisibleDisplayFrame(rect)
            val isKeyboardUp = rootView.height - (rect.bottom - rect.top) > rootView.height / 3
//            logo.visibility = if (isKeyboardUp) View.GONE else View.VISIBLE
//            simas_panjang.visibility = if (isKeyboardUp) View.GONE else View.VISIBLE
        }
    }

    /**
     * User Access Code
     * Code 0 = Admin/Operator
     * Code 1 = Top Layer of Organization
     * Code 2 = Mid Layer of Organization
     * Code 3 = Bottom Layer of Organization
     **/
    private fun getUserAccessCode(unit: Int?): Int {
        var code = -99999
        when (unit) {
            in -99..0 -> code = 0
            in 1..99 -> code = 1
//            in 10..99 -> code = 1
            in 100..9999 -> code = 2
            in 10000..99999 -> code = 3
        }
        return code
    }

}