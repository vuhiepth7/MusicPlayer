package com.example.musicplayer.data.local

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val NAME = "com.example.musicplayer"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    private val FIRST_TIME_ASKING = Pair("FIRST_TIME_ASKING", true)
    private val FCM_PLAYBACK_ENABLED = Pair("FCM_PLAYBACK_ENABLED", false)


    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var isFirstTimeAsking: Boolean
        get() = preferences.getBoolean(FIRST_TIME_ASKING.first, FIRST_TIME_ASKING.second)
        set(value) = preferences.edit {
            it.putBoolean(FIRST_TIME_ASKING.first, value)
        }

    var isFcmPlaybackEnabled: Boolean
        get() = preferences.getBoolean(FCM_PLAYBACK_ENABLED.first, FCM_PLAYBACK_ENABLED.second)
        set(value) = preferences.edit {
            it.putBoolean(FCM_PLAYBACK_ENABLED.first, value)
        }
}