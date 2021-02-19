package com.example.musicplayer.data.repo

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.musicplayer.data.local.ContentResolverHelper
import com.example.musicplayer.data.local.db.SongDao
import com.example.musicplayer.data.model.Song

class SongRepository(private val songDao: SongDao, private val contentResolver: ContentResolverHelper) {


    fun getAll(): LiveData<List<Song>> {
        return songDao.getAll()
    }

    suspend fun insertAll(songs: List<Song>) {
        songDao.insertAll(songs)
    }

    suspend fun update(song: Song) {
        songDao.update(song)
    }

    suspend fun delete(song: Song) {
        songDao.delete(song)
    }

    suspend fun deleteAll(songs: List<Song>) {
        songDao.deleteAll(songs)
    }

    suspend fun updateDb() {
        songDao.insertAll(contentResolver.getAll())
    }
}