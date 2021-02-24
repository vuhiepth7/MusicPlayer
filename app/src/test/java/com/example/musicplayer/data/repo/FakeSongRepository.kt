package com.example.musicplayer.data.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicplayer.data.model.Song

class FakeSongRepository : ISongRepository {

    private val songs = mutableListOf(
        Song(1, "Song 1", "artist 1", "uri 1", 1, false),
        Song(2, "Song 2", "artist 2", "uri 2", 2, true),
        Song(3, "Song 3", "artist 3", "uri 3", 3, false),
        Song(4, "Song 4", "artist 4", "uri 4", 4, true),
        Song(5, "Song 5", "artist 5", "uri 5", 5, true),
        Song(6, "Song 6", "artist 6", "uri 6", 6, false),
    )
    override fun getAll(): LiveData<List<Song>> {
        return MutableLiveData(songs)
    }

    override suspend fun insertAll(songs: List<Song>) {
        this.songs.addAll(songs)
    }

    override suspend fun update(song: Song) {
        songs.filter { it.songId == song.songId }.map { it.copy(favorite = song.favorite) }
    }

    override suspend fun delete(song: Song) {
        songs.remove(song)
    }

    override suspend fun deleteAll(songs: List<Song>) {
        this.songs.removeAll(songs)
    }

    override suspend fun updateDb() {}
}