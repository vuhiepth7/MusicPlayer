package com.example.musicplayer.ui.player

import androidx.lifecycle.*
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.repo.Repository
import kotlinx.coroutines.launch

class PlayerViewModel(private val repository: Repository) : ViewModel() {

    private val _currentSongIndex = MutableLiveData<Int>()
    val currentSongIndex: LiveData<Int>
        get() = _currentSongIndex

    fun setCurrentSongIndex(index: Int) {
        _currentSongIndex.value = index
    }

    fun skipNext(): Boolean {
        val currentIndex = _currentSongIndex.value!!
        return if (currentIndex < PlayerActivity.songsList.size - 1) {
            _currentSongIndex.value = currentSongIndex.value?.plus(1)
            true
        } else false
    }

    fun skipPrevious(): Boolean {
        val currentIndex = _currentSongIndex.value!!
        return if (currentIndex > 0) {
            _currentSongIndex.value = currentSongIndex.value?.minus(1)
            true
        } else false
    }

    fun restart() {
        _currentSongIndex.value = 0
    }

    fun updateSong(song: Song) {
        viewModelScope.launch {
            repository.updateSong(song)
        }
    }
}