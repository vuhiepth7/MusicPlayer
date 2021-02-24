package com.example.musicplayer.data.repo

import androidx.lifecycle.LiveData
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.data.model.PlaylistSongCrossRef
import com.example.musicplayer.data.model.PlaylistWithSongs

interface IPlaylistRepository {
    fun getPlaylists(): LiveData<List<Playlist>>
    fun getPlaylistsWithSongs(): LiveData<List<PlaylistWithSongs>>
    fun getSongsFromPlaylist(playlistId: Long): LiveData<List<PlaylistWithSongs>>

    suspend fun create(playlist: Playlist)

    suspend fun update(playlist: Playlist)

    suspend fun delete(playlistId: Long)

    suspend fun insertPlaylistSong(playlistSongCrossRef: PlaylistSongCrossRef)

    suspend fun deletePlaylistSong(playlistSongCrossRef: PlaylistSongCrossRef)
}