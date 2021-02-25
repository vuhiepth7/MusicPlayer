package com.example.musicplayer.data.local

import android.content.ContentResolver
import com.example.musicplayer.data.model.Song

interface IContentResolverHelper {
    val contentResolver: ContentResolver?
    fun getAll(): List<Song>
}