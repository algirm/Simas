package com.jembranakab.simas.model.network

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.User
import com.jembranakab.simas.model.network.base.ISubjectRepo
import kotlinx.coroutines.tasks.await

class SubjectRepoImpl : ISubjectRepo {
    private val userRef = FirebaseFirestore.getInstance().collection("users")


    override suspend fun getThisUserDB(): Resource<User> {
        val userResult = try {
            userRef
                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                .get()
                .await()
                .toObject(User::class.java)!!
        } catch (e: Exception) {
            return Resource.Failure(e)
        }

        return Resource.Success(userResult)
    }

    override suspend fun getUsersDB(): Resource<MutableList<User>> {
        var resultList = userRef.get(Source.CACHE).await()
        if (resultList.isEmpty) {
            Log.d("getUsers", "from server")
            resultList = userRef.get(Source.SERVER).await()
        } else { //check cache is updated
            Log.d("getUsers", "from cache")
        }

        val eventList = mutableListOf<User>()
        for (document in resultList) {
            eventList.add(document.toObject(User::class.java))
        }
        return Resource.Success(eventList)
    }


}