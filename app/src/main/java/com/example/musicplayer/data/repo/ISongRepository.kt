package com.example.musicplayer.data.repo

import androidx.lifecycle.LiveData
import com.example.musicplayer.data.model.Song

interface ISongRepository {
    fun getAll(): LiveData<List<Song>>

    suspend fun insertAll(songs: List<Song>)

    suspend fun update(song: Song)

    suspend fun delete(song: Song)

    suspend fun deleteAll(songs: List<Song>)

    suspend fun updateDb()
}