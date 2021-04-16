package com.jembranakab.simas.model.viewmodel

import androidx.lifecycle.*
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.Organisasi
import com.jembranakab.simas.model.entities.User
import com.jembranakab.simas.model.network.base.ISubjectRepo
import kotlinx.coroutines.*

class SubjectViewModel(private val useCase: ISubjectRepo) : ViewModel() {
    var thisUserMediatorLiveData: MediatorLiveData<Resource<User>> = MediatorLiveData()
    val thisOrgMediatorLiveData: MediatorLiveData<Resource<Organisasi>> = MediatorLiveData()

    init {
        viewModelScope.launch {
            // Get This User Data
            liveData<Resource<User>> {
                emit(Resource.Loading())
                try {
                    emit(useCase.getThisUserDB())
                } catch (e: Exception) {
                    emit(Resource.Failure(e))
                }
            }.also { data ->
                thisUserMediatorLiveData.addSource(data) { thisUserMediatorLiveData.postValue(it) }
            }

            // get this org data

            thisOrgMediatorLiveData.addSource(thisUserMediatorLiveData) { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        thisOrgMediatorLiveData.postValue(Resource.Loading())
                    }

                    is Resource.Success -> {
                        thisOrgMediatorLiveData.removeSource(thisUserMediatorLiveData)

                        try {
                            val namaOrg = (parseMyJson() as JsonArray<*>)
                                .filterIsInstance<JsonObject>()
                                .filter { it.int("unit")!! == resource.data.unit }
                            val thisOrg = Organisasi(
                                namaOrg[0].string("nama"),
                                namaOrg[0].int("unit")
                            )
//                                thisOrg.nama = namaOrg[0].string("nama")
//                                thisOrg.unit = namaOrg[0].int("unit")
                            thisOrgMediatorLiveData.postValue(Resource.Success(thisOrg))

                        } catch (e: Exception) {
                            thisOrgMediatorLiveData.postValue(Resource.Failure(e))
                        }

                    }

                    is Resource.Failure -> {
                        thisOrgMediatorLiveData.postValue(Resource.Failure(resource.throwable))
                    }
                }
            }
        }
    }

    suspend fun getThisOrgData(): LiveData<Resource<Organisasi>> = withContext(Dispatchers.Main) {
        val mediatorLiveData: MediatorLiveData<Resource<Organisasi>> = MediatorLiveData()
        mediatorLiveData.addSource(thisUserMediatorLiveData) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    mediatorLiveData.postValue(Resource.Loading())
                }

                is Resource.Success -> {
                    mediatorLiveData.removeSource(thisUserMediatorLiveData)

                    try {
                        val namaOrg = (parseMyJson() as JsonArray<*>)
                            .filterIsInstance<JsonObject>()
                            .filter { it.int("unit")!! == resource.data.unit }
                        val thisOrg = Organisasi(
                            namaOrg[0].string("nama"),
                            namaOrg[0].int("unit")
                        )
//                        thisOrg.nama = namaOrg[0].string("nama")
//                        thisOrg.unit = namaOrg[0].int("unit")
                        mediatorLiveData.postValue(Resource.Success(thisOrg))

                    } catch (e: Exception) {
                        mediatorLiveData.postValue(Resource.Failure(e))
                    }

                }

                is Resource.Failure -> {
                    mediatorLiveData.postValue(Resource.Failure(resource.throwable))
                }
            }
        }
        return@withContext mediatorLiveData
    }

    /*fun getThisUserData(): LiveData<Resource<User>> {
        val userLiveData: MediatorLiveData<Resource<User>> = MediatorLiveData()
        userLiveData.addSource(allUserMediatorLiveData) {
            when (it) {

                is Resource.Loading -> {
                    userLiveData.value = Resource.Loading()
                }

                is Resource.Success -> {
                    userLiveData.removeSource(allUserMediatorLiveData)
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    for (user in it.data) {
                        if (user.uid.equals(uid)) userLiveData.value = Resource.Success(user)
                    }
                }

                is Resource.Failure -> {
                    userLiveData.value = Resource.Failure(it.throwable)
                }
            }
        }
        return userLiveData
    }*/

    private fun parseMyJson(): Any? {
        val cls = Parser::class.java
        return cls.getResourceAsStream("/assets/struktur-organisasi.json")?.let { inputStream ->
            return Parser.default().parse(inputStream)
        }
    }

}
