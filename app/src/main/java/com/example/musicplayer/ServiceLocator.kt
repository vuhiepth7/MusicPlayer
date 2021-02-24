package com.example.musicplayer

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.example.musicplayer.data.local.ContentResolverHelper
import com.example.musicplayer.data.local.db.SongDatabase
import com.example.musicplayer.data.repo.IPlaylistRepository
import com.example.musicplayer.data.repo.ISongRepository
import com.example.musicplayer.data.repo.PlaylistRepository
import com.example.musicplayer.data.repo.SongRepository

object ServiceLocator {

    @Volatile
    var songRepository: ISongRepository? = null
    @VisibleForTesting set
    @Volatile
    var playlistRepository: IPlaylistRepository? = null
    @VisibleForTesting set

    fun provideSongRepository(context: Context): ISongRepository {
        synchronized(this) {
            return songRepository ?: createSongRepository(context)
        }
    }

    fun providePlaylistRepository(context: Context): IPlaylistRepository {
        synchronized(this) {
            return playlistRepository ?: createPlaylistRepository(context)
        }
    }

    private fun createSongRepository(context: Context): ISongRepository {
        val songDao = SongDatabase.getDatabase(context).songDao()
        val instance = SongRepository(songDao, ContentResolverHelper(context))
        songRepository = instance
        return instance
    }

    private fun createPlaylistRepository(context: Context): IPlaylistRepository {
        val playlistDao = SongDatabase.getDatabase(context).playlistDao()
        val instance = PlaylistRepository(playlistDao)
        playlistRepository = instance
        return instance
    }
}