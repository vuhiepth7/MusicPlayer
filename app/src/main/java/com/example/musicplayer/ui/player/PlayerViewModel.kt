package com.example.musicplayer.ui.player

import androidx.lifecycle.*
import com.example.musicplayer.data.model.Song

class PlayerViewModel : ViewModel() {

    private val _songList = MutableLiveData<List<Song>>()
    val songList: LiveData<List<Song>>
        get() = _songList

    private val _currentSong = MutableLiveData<Song>()
    val currentSong: LiveData<Song>
        get() = _currentSong

    val currentSongIndex: LiveData<Int> = _currentSong.map {
        _songList.value?.indexOf(it) ?: 0
    }

    fun setSongList(songs: List<Song>) {
        _songList.value = songs
    }

    fun setCurrentSong(song: Song?) {
        _currentSong.value = song
    }
}