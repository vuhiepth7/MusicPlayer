package com.example.musicplayer.data.local.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.data.model.PlaylistSongCrossRef
import com.example.musicplayer.data.model.PlaylistWithSongs

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlist")
    fun getPlaylists(): LiveData<List<Playlist>>

    @Transaction
    @Query("SELECT * FROM playlist")
    fun getPlaylistsWithSongs(): LiveData<List<PlaylistWithSongs>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun create(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSong(playlistSongCrossRef: PlaylistSongCrossRef)
}