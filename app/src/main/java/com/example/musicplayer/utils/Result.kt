package com.example.musicplayer.utils

import com.example.musicplayer.utils.Status.*
import java.lang.Exception

data class Result<out T>(val status: Status, val data: T?, val error: Exception?) {

    companion object {
        fun <T> success(data: T?): Result<T> {
            return Result(SUCCESS, data, null)
        }

        fun <T> error(error: Exception, data: T?): Result<T> {
            return Result(ERROR, data, error)
        }

        fun <T> loading(data: T?): Result<T> {
            return Result(LOADING, data, null)
        }
    }
}

enum class Status {
    SUCCESS, LOADING, ERROR
}