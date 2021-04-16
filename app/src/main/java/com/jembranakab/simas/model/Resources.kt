package com.jembranakab.simas.model

sealed class Resource<out T> {
    class Loading<out T> : Resource<T>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Failure(val throwable: Throwable?) : Resource<Nothing>()
}
