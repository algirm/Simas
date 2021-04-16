package com.jembranakab.simas.model.network.base

import com.jembranakab.simas.model.Resource
import com.jembranakab.simas.model.entities.User

interface ISubjectRepo {
    suspend fun getThisUserDB(): Resource<User>
    suspend fun getUsersDB(): Resource<MutableList<User>>
}