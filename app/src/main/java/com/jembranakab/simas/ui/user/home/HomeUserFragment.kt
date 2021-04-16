package com.jembranakab.simas.ui.user.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jembranakab.simas.R
import com.jembranakab.simas.base.BaseFragment
import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.Notifikasi
import com.jembranakab.simas.ui.general.adapter.NotifikasiAdapter
import com.jembranakab.simas.utilities.App.Companion.TAG
import kotlinx.android.synthetic.main.user_home.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.tasks.await

@ExperimentalCoroutinesApi
class HomeUserFragment : BaseFragment() {

    private val notifikasiRef = FirebaseFirestore.getInstance().collection("notifikasi")

    private var thisUnit: Int? = null

    private lateinit var notifikasiAdapter: NotifikasiAdapter

    private val currentExpanded = mutableListOf<String>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        loadNotifikasi()
    }

    private fun loadNotifikasi() {
        lifecycleScope.launchWhenResumed {
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
            callback.collect {
                when (it) {
                    is Resource.Failure -> {
                        toastError(it.throwable)
                        progressBarHome.hide()
                        kosong_tv.visibility = View.GONE
                    }
                    is Resource.Loading -> {
                        progressBarHome.show()
                        kosong_tv.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        if (it.data.isEmpty()) {
                            kosong_tv.visibility = View.VISIBLE
                        } else {
                            for (notifikasi in it.data) {
                                if (currentExpanded.contains(notifikasi.id)) {
                                    notifikasi.setExpanded(true)
                                }
                            }
                            notifikasiAdapter.listNotifikasi = it.data
                            kosong_tv.visibility = View.GONE
                        }
                        progressBarHome.hide()
                    }
                }
            }
        }
    }

    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.layoutPosition
            deleteNotification(notifikasiAdapter.listNotifikasi[position].id.toString())
        }
    }

    private fun init() {
        thisUnit =
                findNavController().currentDestination!!.arguments["thisUnit"]?.defaultValue as Int
        rvNotifikasi.apply {
            notifikasiAdapter = NotifikasiAdapter(context)
            adapter = notifikasiAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(this)
        }
        notifikasiAdapter.setOnBackgroundClickListener { notifikasi, position ->
            val notifId = notifikasi.id.toString()
            if (!notifikasi.read) {
                updateNotification(notifId)
                currentExpanded.add(notifId)
                return@setOnBackgroundClickListener
            }
            if (notifikasi.isExpanded()) {
                notifikasi.setExpanded(false)
            } else {
                notifikasi.setExpanded(true)
            }
            if (!notifikasi.isExpanded() && currentExpanded.contains(notifId)) {
                currentExpanded.remove(notifId)
            } else {
                currentExpanded.add(notifId)
            }
            notifikasiAdapter.notifyItemChanged(position)
        }
    }

    private fun updateNotification(id: String) = lifecycleScope.launchWhenResumed {
        try {
            val mapRead = HashMap<String, Any>()
            mapRead["read"] = true
            mapRead["readAt"] = Timestamp.now()
            notifikasiRef.document(thisUnit.toString())
                    .collection("notifikasi")
                    .document(id)
                    .update(mapRead)
                    .await()
        } catch (e: Exception) {
            Log.e(TAG, "updateNotification: error occred: ${e.message}", e)
        }
    }

    private fun deleteNotification(id: String) = lifecycleScope.launchWhenResumed {
        try {
            notifikasiRef.document(thisUnit.toString())
                    .collection("notifikasi")
                    .document(id)
                    .delete()
                    .await()
        } catch (e: Exception) {
            Log.e(TAG, "deleteNotification: error occred: ${e.message}", e)
        }
    }

    /*private fun clearNotification() = lifecycleScope.launchWhenResumed {
        try {
            notifikasiRef.document(thisUnit.toString())
                    .collection("notifikasi")
                    .document()
                    .delete()
                    .await()
        } catch (e: Exception) {
            Log.e(TAG, "deleteNotification: error occred: ${e.message}", e)
        }
    }*/

}