package com.example.musicplayer.ui

import androidx.lifecycle.*
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.repo.Repository
import com.example.musicplayer.data.repo.SongRepository
import com.example.musicplayer.utils.Result
import kotlinx.coroutines.launch

class MainViewModel(private val repository: Repository, ) : ViewModel() {

    private val _songs = MutableLiveData<Result<List<Song>>>()
    val songs: LiveData<Result<List<Song>>>
        get() = _songs

    fun loadSongsFromDb() {
        viewModelScope.launch {
            _songs.value = Result.loading(null)
            _songs.value = repository.getSongsFromDb()
        }
    }

    fun loadSongsFromContentResolver() {
        viewModelScope.launch {
            _songs.value = Result.loading(null)
            _songs.value = repository.getSongsFromContentResolver()
        }
    }

}