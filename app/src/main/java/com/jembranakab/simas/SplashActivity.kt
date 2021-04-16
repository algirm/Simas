package com.jembranakab.simas

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.Organisasi
import com.jembranakab.simas.model.factories.SubjectVMFactory
import com.jembranakab.simas.model.network.SubjectRepoImpl
import com.jembranakab.simas.model.viewmodel.SubjectViewModel
import com.jembranakab.simas.utilities.App.Companion.OPERATOR
import com.jembranakab.simas.utilities.App.Companion.ORG_BOTTOM_LAYER
import com.jembranakab.simas.utilities.App.Companion.ORG_MID_LAYER
import com.jembranakab.simas.utilities.App.Companion.ORG_TOP_LAYER

class SplashActivity : AppCompatActivity() {

    private val subjectViewModel by viewModels<SubjectViewModel> { SubjectVMFactory(SubjectRepoImpl()) }
    private lateinit var handler:Handler
    private lateinit var runnable: Runnable
    private lateinit var observer: Observer<Resource<Organisasi>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runnable = Runnable { checkLogin() }
        handler = Handler()
        handler.post(runnable)
    }

    private fun checkLogin() {
        val user = FirebaseAuth.getInstance().currentUser

        observer = Observer<Resource<Organisasi>> {
            if (it is Resource.Success) {
                val accessCode = getUserAccessCode(it.data.unit)
                if (accessCode == OPERATOR) {
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
            } else if (it is Resource.Failure) {
                val text = getString(R.string.no_connection_text)
                Snackbar.make(
                        findViewById(android.R.id.content),
                        text,
                        Snackbar.LENGTH_INDEFINITE
                ).show()
            }
        }

        if (user != null) {
            subjectViewModel.thisOrgMediatorLiveData.observe(this, observer)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
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
            in -99..0 -> code = OPERATOR
            in 1..99 -> code = ORG_TOP_LAYER
//            in 10..99 -> code = 1
            in 100..9999 -> code = ORG_MID_LAYER
            in 10000..99999 -> code = ORG_BOTTOM_LAYER
        }
        return code
    }

    override fun onBackPressed() {
        super.onBackPressed()
        handler.removeCallbacks(runnable)
        subjectViewModel.thisOrgMediatorLiveData.removeObserver(observer)
    }

}