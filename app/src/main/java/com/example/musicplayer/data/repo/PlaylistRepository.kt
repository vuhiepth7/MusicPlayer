package com.example.musicplayer.data.repo

import androidx.lifecycle.LiveData
import com.example.musicplayer.data.local.db.PlaylistDao
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.data.model.PlaylistSongCrossRef
import com.example.musicplayer.data.model.PlaylistWithSongs

class PlaylistRepository(private val playlistDao: PlaylistDao){


    fun getPlaylists(): LiveData<List<Playlist>> {
        return playlistDao.getPlaylists()
    }

    fun getPlaylistsWithSongs(): LiveData<List<PlaylistWithSongs>> {
        return playlistDao.getPlaylistsWithSongs()
    }

    suspend fun create(playlist: Playlist) {
        playlistDao.create(playlist)
    }

    suspend fun insertPlaylistSong(playlistSongCrossRef: PlaylistSongCrossRef) {
        playlistDao.insertPlaylistSong(playlistSongCrossRef)
    }

}