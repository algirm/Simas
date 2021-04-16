package com.jembranakab.simas

import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jembranakab.simas.base.BaseActivity
import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.Notifikasi
import com.jembranakab.simas.model.entities.SuratDisposisi
import com.jembranakab.simas.model.factories.SuratOpdVMFactory
import com.jembranakab.simas.model.network.SuratOpdRepoImpl
import com.jembranakab.simas.model.viewmodel.SuratOpdViewModel
import com.jembranakab.simas.ui.general.dialog.ConfirmDialogFragment
import com.jembranakab.simas.ui.general.dialog.ConfirmDialogListener
import com.jembranakab.simas.utilities.App
import com.jembranakab.simas.utilities.App.Companion.DISPOSISI
import com.jembranakab.simas.utilities.App.Companion.EXIT
import com.jembranakab.simas.utilities.App.Companion.JAWABAN
import com.jembranakab.simas.utilities.App.Companion.LOGOUT
import com.jembranakab.simas.utilities.App.Companion.NAVIGATE_UP
import com.jembranakab.simas.utilities.App.Companion.PENYELESAIAN
import com.jembranakab.simas.utilities.DataConverter
import kotlinx.android.synthetic.main.activity_main_user.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.android.synthetic.main.user_home.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect

@ExperimentalCoroutinesApi
class MainActivityUser : BaseActivity(), ConfirmDialogListener {

    private val suratOpdVM by viewModels<SuratOpdViewModel> {
        SuratOpdVMFactory(SuratOpdRepoImpl())
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var confirmDialog: ConfirmDialogFragment

    private var thisUnit: Int? = null
    private var thisOrg: String? = null
    private var accessCode: Int? = null
    private var notificationTresholdTime: Timestamp? = null
    private var nid = 0

    private val notifikasiRef = FirebaseFirestore.getInstance().collection("notifikasi")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_user)
        init()
        setDrawerHeader()
        initNavigation()
        loadData()
        refreshDataWhenGetNewNotification()
//        listenNotification()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            navController.navigate(R.id.nav_user_disposisi)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            Toast.makeText(this, intent.getStringExtra("notification"), Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshDataWhenGetNewNotification() = lifecycleScope.launchWhenResumed {
        val callback = callbackFlow<Resource<List<Notifikasi>>> {
            val subscription = notifikasiRef.document(thisUnit.toString())
                    .collection("notifikasi")
                    .orderBy("receiveAt", Query.Direction.DESCENDING)
                    .addSnapshotListener { value, error ->
                        value?.let { snapshot ->
                            val result = arrayListOf<Notifikasi>()
                            snapshot.documents.forEach {
                                result.add(it.toObject(Notifikasi::class.java)!!)
                            }
                            offer(Resource.Success(result))
                        }
                        error?.let {
                            offer(Resource.Failure(error))
                        }
                    }
            awaitClose { subscription.remove() }
        }
        callback.collect { resource ->
            if (resource is Resource.Success) {
                if (resource.data.isNotEmpty()) {
                    val notifikasi = resource.data.first()
                    val notifDari = notifikasi.fromUnit?.let { DataConverter.getNamaOrgFromAll(it) }
                    var statusText = "Notifikasi"
                    var keteranganNotif = ""
                    var textNotif = ""
                    when (notifikasi.tipe) {
                        PENYELESAIAN -> {
                            notifikasi.keterangan?.let {
                                keteranganNotif = "\nKeterangan Penyelesaian: $it"
                            }
                            textNotif = "Surat Disposisi telah diselesaikan oleh $notifDari$keteranganNotif"
                            statusText = "Penyelesaian Disposisi"
                        }
                        DISPOSISI -> {
                            notifikasi.keterangan?.let {
                                keteranganNotif = "\nKeterangan Tujuan Disposisi: $it"
                            }
                            textNotif = "Surat telah didisposisikan oleh $notifDari$keteranganNotif"
                            statusText = "Pen-disposisi-an"
                        }
                        JAWABAN -> {
                            notifikasi.keterangan?.let {
                                keteranganNotif = "\nJawaban: $it"
                            }
                            textNotif = "Surat Disposisi telah dijawab oleh $notifDari$keteranganNotif"
                            statusText = "Jawaban Disposisi"
                        }
                        App.SURAT_MASUK -> {
                            notifikasi.keterangan?.let {
                                keteranganNotif = "\nKeterangan: $it"
                            }
                            textNotif = "Surat Masuk baru diterima $keteranganNotif"
                            statusText = "Surat Masuk"
                        }
                    }

                    if (notifikasi.receiveAt?.seconds!! > notificationTresholdTime?.seconds!!) {
                        // refreshing data suratmasuk dan disposisi
                        suratOpdVM.getSuratMasuk(thisUnit!!)
                        suratOpdVM.getSuratDisposisi(thisUnit!!)

                        // update local variable for notif stuff
                        notificationTresholdTime = Timestamp.now()
                        nid++

                        // make notification
                        val builder = NotificationCompat.Builder(this@MainActivityUser, "notifikasi")
                                .setSmallIcon(R.drawable.ic_launcher_foreground2)
                                .setContentTitle(statusText)
                                .setContentText(textNotif)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setAutoCancel(true)
                        NotificationManagerCompat.from(this@MainActivityUser).notify(nid, builder.build())
                    }
                }
            }
        }
    }

    /*private fun listenNotification() {
        suratOpdVM.listenJawaban("bpN7PDaYbRuHau90KisQ")
        suratOpdVM.jawabanListener.observe(this, {
            if (it is Resource.Success) {
                val intent = Intent(this, SplashActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
                intent.putExtra("notification", "test")
                val notifSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val builder = NotificationCompat.Builder(this, "")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(it.data.id)
                    .setContentText(it.data.jawaban)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSound(notifSound)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                NotificationManagerCompat.from(this).notify(1, builder.build())
            }
        })
    }*/

    private fun loadData() {
        with(suratOpdVM) {
            getNomorSurat(thisUnit!!)
            getSuratKeluar(thisUnit!!)
            getSuratMasuk(thisUnit!!)
            getSuratDisposisi(thisUnit!!)
            listNomorSurat.observe(this@MainActivityUser, {
                if (it is Resource.Success) {
                    nav_view_user.menu.findItem(R.id.nav_user_nomorsurat).apply {
                        title = "Nomor Surat (${it.data.size})"
                    }
                }
            })
            listSuratKeluar.observe(this@MainActivityUser, {
                if (it is Resource.Success) {
                    nav_view_user.menu.findItem(R.id.nav_user_suratkeluar).apply {
                        title = "Surat Keluar (${it.data.size})"
                    }
                }
            })
            if (accessCode!! > 1) {
                listSuratDisposisi.observe(this@MainActivityUser, {
                    if (it is Resource.Success) {
                        nav_view_user.menu.findItem(R.id.nav_user_suratmasuk).apply {
                            title = "Surat Masuk (${it.data.size})"
                        }
                        nav_view_user.menu.findItem(R.id.nav_user_disposisi).apply {
                            title = "Disposisi (${it.data.size})"
                        }
                    }
                })
            } else {
                listSuratMasuk.observe(this@MainActivityUser, {
                    if (it is Resource.Success) {
                        nav_view_user.menu.findItem(R.id.nav_user_suratmasuk).apply {
                            title = "Surat Masuk (${it.data.size})"
                        }
                    }
                })
                listSuratDisposisi.observe(this@MainActivityUser, {
                    if (it is Resource.Success) {
                        nav_view_user.menu.findItem(R.id.nav_user_disposisi).apply {
                            title = "Disposisi (${it.data.size})"
                        }
                    }
                })
            }
        }
    }

    private fun initNavigation() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_user_beranda,
                R.id.nav_user_nomorsurat,
                R.id.nav_user_suratkeluar,
                R.id.nav_user_suratmasuk,
                R.id.nav_user_disposisi
        ), drawer_layout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navController.setGraph(R.navigation.user_navigation)
        nav_view_user.setupWithNavController(navController)

        // navigation listener
        with(nav_view_user.menu) {
            findItem(R.id.nav_user_logout).setOnMenuItemClickListener {
                if (!it.isChecked) {
                    showConfirmDialog("Ganti Akun", LOGOUT)
                    return@setOnMenuItemClickListener true
                }
                return@setOnMenuItemClickListener false
            }
            findItem(R.id.nav_user_exit).setOnMenuItemClickListener {
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
        val navArgument2 = NavArgument.Builder()
                .setType(NavType.IntType)
                .setDefaultValue(accessCode)
                .build()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_user_nomorsurat, R.id.nav_user_suratkeluar, R.id.nav_user_beranda,
                R.id.nav_user_suratmasuk, R.id.nav_user_disposisi -> {
                    drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    destination.addArgument("thisUnit", navArgument)
                    destination.addArgument("accessCode", navArgument2)
                }
                R.id.nav_user_tambah_nomorsurat -> {
                    drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    destination.addArgument("thisUnit", navArgument)
                    destination.addArgument("accessCode", navArgument2)
                }
                else -> drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        }
    }

    private fun setDrawerHeader() {
        val headerView = nav_view_user.getHeaderView(0)
        headerView.namaOrg.text = thisOrg
    }

    private fun init() {
        confirmDialog = ConfirmDialogFragment.newInstance()
        thisUnit = intent.extras?.getInt("thisUnit")
        thisOrg = intent.extras?.getString("thisOrg")
        accessCode = intent.extras?.getInt("accessCode")
        notificationTresholdTime = Timestamp.now()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun disposisi(bundle: Bundle) {
        val kepada = bundle.getIntegerArrayList("kepada") as ArrayList<Int>
        val suratDisposisi = bundle.getSerializable("suratDisposisi") as SuratDisposisi
        suratOpdVM.disposisi(kepada, suratDisposisi)
        suratOpdVM.resultDisposisi.observe(this, {
            if (it !is Resource.Loading) {
                suratOpdVM.resultDisposisi.removeObservers(this)
            }
            when (it) {
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    suratOpdVM.getSuratDisposisi(thisUnit!!)
                    suratOpdVM.getSuratMasuk(thisUnit!!)
                    snackLong(getString(R.string.berhasil))
                }
                is Resource.Failure -> {
                    toastError(it.throwable)
                }
            }
        })
    }

    private fun jawabDisposisi(bundle: Bundle) {
        suratOpdVM.jawabDisposisi(
                bundle.getInt("thisUnit"),
                bundle.getSerializable("suratDisposisi") as SuratDisposisi,
                bundle.getString("jawaban")!!
        )
        suratOpdVM.resultJawab.observe(this, {
            if (it !is Resource.Loading) {
                suratOpdVM.resultJawab.removeObservers(this)
            }
            if (it is Resource.Success) {
                suratOpdVM.getSuratDisposisi(thisUnit!!)
            }
        })
    }

    private fun penyelesaianDisposisi(bundle: Bundle) {
        suratOpdVM.penyelesaianDisposisi(
            thisUnit!!,
            bundle.getSerializable("suratDisposisi") as SuratDisposisi,
        )
        suratOpdVM.resultPenyelesaian.observe(this, {
            if (it !is Resource.Loading) {
                suratOpdVM.resultPenyelesaian.removeObservers(this)
            }
            if (it is Resource.Success) {
                suratOpdVM.getSuratDisposisi(thisUnit!!)
                snackLong(getString(R.string.dispos_selesai))
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
            DISPOSISI -> disposisi(bundle!!)
            JAWABAN -> jawabDisposisi(bundle!!)
            PENYELESAIAN -> penyelesaianDisposisi(bundle!!)
            else -> {
                toastError(null)
                dialog.dismiss()
            }
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}