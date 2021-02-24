package com.example.musicplayer.data.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.data.model.PlaylistSongCrossRef
import com.example.musicplayer.data.model.PlaylistWithSongs
import com.example.musicplayer.data.model.Song

class FakePlaylistRepository : IPlaylistRepository {

    private val playlists = mutableListOf(
        Playlist(1, "Playlist 1"),
        Playlist(2, "Playlist 2"),
        Playlist(3, "Playlist 3")
    )
    private val playlistSongCrossRefs = mutableListOf(
        PlaylistSongCrossRef(1, 1),
        PlaylistSongCrossRef(1, 2),
        PlaylistSongCrossRef(1, 5),
        PlaylistSongCrossRef(2, 2),
        PlaylistSongCrossRef(2, 6),
        PlaylistSongCrossRef(3, 3),
        PlaylistSongCrossRef(3, 4)
    )
    private val playlistWithSongs = mutableListOf(
        PlaylistWithSongs(
            playlists[0], mutableListOf(
                Song(1, "Song 1", "artist 1", "uri 1", 1, false),
                Song(2, "Song 2", "artist 2", "uri 2", 2, true),
                Song(5, "Song 5", "artist 5", "uri 5", 5, true)
            )
        ),
        PlaylistWithSongs(
            playlists[1], mutableListOf(
                Song(2, "Song 2", "artist 2", "uri 2", 2, true),
                Song(6, "Song 6", "artist 6", "uri 6", 6, false)
            )
        ),
        PlaylistWithSongs(
            playlists[2], mutableListOf(
                Song(3, "Song 3", "artist 3", "uri 3", 3, false),
                Song(4, "Song 4", "artist 4", "uri 4", 4, true)
            )
        ),
    )

    override fun getPlaylists(): LiveData<List<Playlist>> {
        return MutableLiveData(playlists)
    }

    override fun getPlaylistsWithSongs(): LiveData<List<PlaylistWithSongs>> {
        return MutableLiveData(playlistWithSongs)
    }

    override fun getSongsFromPlaylist(playlistId: Long): LiveData<List<PlaylistWithSongs>> {
        return MutableLiveData(playlistWithSongs.filter { it.playlist.playlistId == playlistId })
    }

    override suspend fun create(playlist: Playlist) {
        playlists.add(playlist)
    }

    override suspend fun update(playlist: Playlist) {
        playlists.replaceAll { if (it.playlistId == playlist.playlistId) playlist else it }
    }

    override suspend fun delete(playlistId: Long) {
        playlists.removeIf { it.playlistId == playlistId }
        playlistSongCrossRefs.removeIf { it.playlistId == playlistId }
    }

    override suspend fun insertPlaylistSong(playlistSongCrossRef: PlaylistSongCrossRef) {
        playlistSongCrossRefs.add(playlistSongCrossRef)
    }

    override suspend fun deletePlaylistSong(playlistSongCrossRef: PlaylistSongCrossRef) {
        playlistSongCrossRefs.remove(playlistSongCrossRef)
    }

}