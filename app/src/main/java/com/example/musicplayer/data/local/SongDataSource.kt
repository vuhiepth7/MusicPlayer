package com.example.musicplayer.data.local

import com.example.musicplayer.data.model.Song

interface SongDataSource {

    fun addAllSongs(songs: List<Song>)
    fun getAllSongs(): List<Song>
    fun updateSong(song: Song)
    fun deleteAll()
}