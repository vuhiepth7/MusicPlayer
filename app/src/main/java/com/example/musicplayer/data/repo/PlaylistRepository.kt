package com.example.musicplayer.data.repo

import androidx.lifecycle.LiveData
import com.example.musicplayer.data.local.db.PlaylistDao
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.data.model.PlaylistSongCrossRef
import com.example.musicplayer.data.model.PlaylistWithSongs

class PlaylistRepository(private val playlistDao: PlaylistDao) : IPlaylistRepository {


    override fun getPlaylists(): LiveData<List<Playlist>> {
        return playlistDao.getPlaylists()
    }

    override fun getPlaylistsWithSongs(): LiveData<List<PlaylistWithSongs>> {
        return playlistDao.getPlaylistsWithSongs()
    }

    override fun getSongsFromPlaylist(playlistId: Long): LiveData<List<PlaylistWithSongs>> {
        return playlistDao.getSongsFromPlaylist(playlistId)
    }

    override suspend fun create(playlist: Playlist) {
        playlistDao.create(playlist)
    }

    override suspend fun update(playlist: Playlist) {
        playlistDao.update(playlist)
    }

    override suspend fun delete(playlistId: Long) {
        playlistDao.delete(playlistId)
        playlistDao.deletePlaylistSong(playlistId)
    }

    override suspend fun insertPlaylistSong(playlistSongCrossRef: PlaylistSongCrossRef) {
        playlistDao.insertPlaylistSong(playlistSongCrossRef)
    }

    override suspend fun deletePlaylistSong(playlistSongCrossRef: PlaylistSongCrossRef) {
        playlistDao.deletePlaylistSong(playlistSongCrossRef)
    }

}