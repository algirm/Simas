package com.jembranakab.simas

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.jembranakab.simas.base.BaseActivity
import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.SuratOpd
import com.jembranakab.simas.model.factories.SuratOperatorVMFactory
import com.jembranakab.simas.model.network.SuratOperatorRepoImpl
import com.jembranakab.simas.ui.general.dialog.ConfirmDialogFragment
import com.jembranakab.simas.ui.general.dialog.ConfirmDialogListener
import com.jembranakab.simas.utilities.App.Companion.EXIT
import com.jembranakab.simas.utilities.App.Companion.KIRIM_ARSIP_KELUAR
import com.jembranakab.simas.utilities.App.Companion.LOGOUT
import com.jembranakab.simas.utilities.App.Companion.NAVIGATE_UP
import com.jembranakab.simas.model.viewmodel.SuratOperatorViewModel
import kotlinx.android.synthetic.main.activity_main_op.*
import kotlinx.android.synthetic.main.nav_header_main.view.*

class MainActivityOp : BaseActivity(), ConfirmDialogListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var confirmDialog: ConfirmDialogFragment

    private var thisUnit: Int? = null
    private var thisOrg: String? = null

    private val suratOperatorVM by viewModels<SuratOperatorViewModel> {
        SuratOperatorVMFactory(SuratOperatorRepoImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_op)
        init()
        initNavigation()
        setDrawerHeader()
        init()
        loadData()
    }

    private fun loadData() {
        with(suratOperatorVM) {
            fetchSuratKeluar(thisUnit!!)
            fetchSuratMasuk(thisUnit!!)
            listSuratKeluar.observe(this@MainActivityOp, {
                if (it is Resource.Success) {
                    nav_view_op.menu.findItem(R.id.nav_arsip_suratkeluar).apply {
                        title = "Arsip Surat Keluar (${it.data.size})"
                    }
                }
            })
            listSuratMasuk.observe(this@MainActivityOp, {
                if (it is Resource.Success) {
                    nav_view_op.menu.findItem(R.id.nav_arsip_suratmasuk).apply {
                        title = "Arsip Surat Masuk (${it.data.size})"
                    }
                }
            })
        }

    }

    private fun initNavigation() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_beranda, R.id.nav_arsip_suratkeluar, R.id.nav_arsip_suratmasuk), drawer_layout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navController.setGraph(R.navigation.operator_navigation)
        nav_view_op.setupWithNavController(navController)

        // navigation listener
        with(nav_view_op.menu) {
            findItem(R.id.nav_logout).setOnMenuItemClickListener {
                if (!it.isChecked) {
                    showConfirmDialog("Ganti Akun", LOGOUT)
                    return@setOnMenuItemClickListener true
                }
                return@setOnMenuItemClickListener false
            }
            findItem(R.id.nav_exit).setOnMenuItemClickListener {
                if (!it.isChecked) {
                    showConfirmDialog("Keluar Aplikasi", EXIT)
                    return@setOnMenuItemClickListener true
                }
                return@setOnMenuItemClickListener false
            }
        }

        val navArgument = NavArgument.Builder()
                .setType(NavType.IntType)
                .setDefaultValue(thisUnit)
                .build()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_arsip_suratkeluar, R.id.nav_arsip_suratmasuk -> {
                    drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    destination.addArgument("thisUnit", navArgument)
                }
                R.id.nav_tambaharsip_keluar, R.id.nav_tambaharsip_masuk -> {
                    drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    destination.addArgument("thisUnit", navArgument)
                }
                else -> drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        }
    }

    private fun setDrawerHeader() {
        val headerView = nav_view_op.getHeaderView(0)
        headerView.jembranakab_tv.text = thisUnit.toString()
        headerView.namaOrg.text = thisOrg
    }

    private fun init() {
        confirmDialog = ConfirmDialogFragment.newInstance()
        thisUnit = intent.extras?.getInt("thisUnit")
        thisOrg = intent.extras?.getString("thisOrg")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun kirimArsipKeluar(bundle: Bundle) {
        val suratUtama = bundle.getSerializable("suratUtama") as SuratOpd
        val terusan = bundle.getIntegerArrayList("terusan")
        suratOperatorVM.kirimKeluar(suratUtama, terusan ?: arrayListOf())
        suratOperatorVM.resultKirimKeluar.observe(this, {
            if (it !is Resource.Loading) {
                suratOperatorVM.resultKirimKeluar.removeObservers(this)
            }
            when (it) {
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    suratOperatorVM.fetchSuratKeluar(thisUnit!!)
                    snackLong(getString(R.string.berhasil_dikirim))
                }
                is Resource.Failure -> {
//                    suratOperatorVM.fetchSuratKeluar(thisUnit!!)
                    toastError(it.throwable)
                }
            }
        })
    }

    private fun showConfirmDialog(title: String, event: Int) {
        val args = Bundle()
        args.putString("header", title)
        args.putInt("eventCode", event)
        confirmDialog.arguments = args
        confirmDialog.show(supportFragmentManager, getString(R.string.confirm_dialog))
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, eventCode: Int?, bundle: Bundle?) {
        when (eventCode) {
            LOGOUT -> logout()
            EXIT -> finish()
            NAVIGATE_UP -> navController.navigateUp()
            KIRIM_ARSIP_KELUAR -> kirimArsipKeluar(bundle!!)
            else -> {
                toastError(null)
                dialog.dismiss()
            }
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

}