package com.example.musicplayer

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.musicplayer.data.local.AppPreferences

class MusicPlayerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}