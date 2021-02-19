package com.example.musicplayer

import android.content.Context
import com.example.musicplayer.data.local.ContentResolverHelper
import com.example.musicplayer.data.local.db.SongDatabase
import com.example.musicplayer.data.repo.SongRepository

object ServiceLocator {

    @Volatile
    private var songRepository: SongRepository? = null

    fun provideSongRepository(context: Context): SongRepository {
        synchronized(this) {
            return songRepository ?: createSongRepository(context)
        }
    }

    private fun createSongRepository(context: Context): SongRepository {
        val songDao = SongDatabase.getDatabase(context).songDao()
        val instance = SongRepository(songDao, ContentResolverHelper(context))
        songRepository = instance
        return instance
    }
}