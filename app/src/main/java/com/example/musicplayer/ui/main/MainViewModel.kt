package com.example.musicplayer.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.musicplayer.ServiceLocator
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.ui.player.PlayerActivity
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val songRepo = ServiceLocator.provideSongRepository(application)

    val songs = songRepo.getAll()

    private val _currentSongIndex = MutableLiveData<Int>()
    val currentSongIndex: LiveData<Int>
        get() = _currentSongIndex

    val currentSong: LiveData<Song> = _currentSongIndex.map { songs.value?.get(it)!! }

    fun update(song: Song) {
        viewModelScope.launch {
            songRepo.update(song)
        }
    }

    fun setCurrentSongIndex(index: Int) {
        _currentSongIndex.value = index
    }

    fun skipNext(): Boolean {
        return if (_currentSongIndex.value!! < songs.value?.size!! - 1) {
            _currentSongIndex.value = currentSongIndex.value?.plus(1)
            true
        } else false
    }

    fun skipPrevious(): Boolean {
        return if (_currentSongIndex.value!! > 0) {
            _currentSongIndex.value = currentSongIndex.value?.minus(1)
            true
        } else false
    }

    fun updateDb() {
        viewModelScope.launch {
            songRepo.updateDb()
        }
    }
}