package com.example.musicplayer

import android.content.Context
import com.example.musicplayer.data.local.ContentResolverHelper
import com.example.musicplayer.data.local.db.SongDatabase
import com.example.musicplayer.data.repo.PlaylistRepository
import com.example.musicplayer.data.repo.SongRepository

object ServiceLocator {

    @Volatile
    private var songRepository: SongRepository? = null
    @Volatile
    private var playlistRepository: PlaylistRepository? = null

    fun provideSongRepository(context: Context): SongRepository {
        synchronized(this) {
            return songRepository ?: createSongRepository(context)
        }
    }

    fun providePlaylistRepository(context: Context): PlaylistRepository {
        synchronized(this) {
            return playlistRepository ?: createPlaylistRepository(context)
        }
    }

    private fun createSongRepository(context: Context): SongRepository {
        val songDao = SongDatabase.getDatabase(context).songDao()
        val instance = SongRepository(songDao, ContentResolverHelper(context))
        songRepository = instance
        return instance
    }

    private fun createPlaylistRepository(context: Context): PlaylistRepository {
        val playlistDao = SongDatabase.getDatabase(context).playlistDao()
        val instance = PlaylistRepository(playlistDao)
        playlistRepository = instance
        return instance
    }
}