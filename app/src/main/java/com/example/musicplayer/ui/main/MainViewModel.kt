package com.example.musicplayer.ui.main

import android.app.Application
import androidx.lifecycle.*
import com.example.musicplayer.ServiceLocator
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.utils.Event
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val songRepo = ServiceLocator.provideSongRepository(application)

    val songs = songRepo.getAll()

    private val _currentSongIndex = MutableLiveData<Int>()
    val currentSongIndex: LiveData<Int>
        get() = _currentSongIndex

    val currentSong: LiveData<Song> = _currentSongIndex.map { songs.value?.get(it)!! }

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean>
        get() = _isPlaying

    private val _songChangeEvent = MutableLiveData<Event<Long>>()
    val songChangeEvent: LiveData<Event<Long>>
        get() = _songChangeEvent

    private val _togglePlayPauseEvent = MutableLiveData<Event<Unit>>()
    val togglePlayPauseEvent: LiveData<Event<Unit>>
        get() = _togglePlayPauseEvent

    private val _currentProgress = MutableLiveData<Int>()
    val currentProgress: LiveData<Int>
        get() = _currentProgress

    private val _seekToEvent = MutableLiveData<Event<Int>>()
    val seekToEvent: LiveData<Event<Int>>
        get() = _seekToEvent

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
            _songChangeEvent.value = Event(currentSong.value?.id ?: 0)
            true
        } else false
    }

    fun skipPrevious(): Boolean {
        return if (_currentSongIndex.value!! > 0) {
            _currentSongIndex.value = currentSongIndex.value?.minus(1)
            _songChangeEvent.value = Event(currentSong.value?.id ?: 0)
            true
        } else false
    }

    fun togglePlayPause() {
        _togglePlayPauseEvent.value = Event(Unit)
    }

    fun setIsPlaying(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    fun setCurrentProgress(progress: Int) {
        _currentProgress.value = progress
    }

    fun setSeekTo(progress: Int) {
        _seekToEvent.value = Event((progress))
    }

    fun updateDb() {
        viewModelScope.launch {
            songRepo.updateDb()
        }
    }
}