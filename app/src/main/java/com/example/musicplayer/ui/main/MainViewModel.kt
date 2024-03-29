package com.example.musicplayer.ui.main

import android.app.Application
import androidx.lifecycle.*
import com.example.musicplayer.ServiceLocator
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.data.model.PlaylistSongCrossRef
import com.example.musicplayer.data.model.PlaylistWithSongs
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.utils.Event
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val songRepo = ServiceLocator.provideSongRepository(application)
    private val playlistRepo = ServiceLocator.providePlaylistRepository(application)

    val songs = songRepo.getAll()
    val playlists = playlistRepo.getPlaylistsWithSongs()

    private val _currentQueue = MutableLiveData<List<Song>>()
    val currentQueue: LiveData<List<Song>>
        get() = _currentQueue

    private val _currentSongsList = MutableLiveData<List<Song>>()
    val currentSongsList: LiveData<List<Song>>
        get() = _currentSongsList

    private val _currentSongIndex = MutableLiveData<Int>()
    val currentSongIndex: LiveData<Int>
        get() = _currentSongIndex

    val currentSong: LiveData<Song> = _currentSongIndex.map { _currentQueue.value?.get(it)!! }

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean>
        get() = _isPlaying

    private val _togglePlayPauseEvent = MutableLiveData<Event<Unit>>()
    val togglePlayPauseEvent: LiveData<Event<Unit>>
        get() = _togglePlayPauseEvent

    private val _currentProgress = MutableLiveData<Int>()
    val currentProgress: LiveData<Int>
        get() = _currentProgress

    private val _seekToEvent = MutableLiveData<Event<Int>>()
    val seekToEvent: LiveData<Event<Int>>
        get() = _seekToEvent

    private val _isLooping = MutableLiveData<Boolean>()
    val isLooping: LiveData<Boolean>
        get() = _isLooping

    private val _shuffle = MutableLiveData<Boolean>()
    val shuffle: LiveData<Boolean>
        get() = _shuffle

    fun update(song: Song) {
        viewModelScope.launch {
            songRepo.update(song)
        }
    }

    fun getSongsFromPlaylist(playlistId: Long): LiveData<List<Song>> {
        return playlistRepo.getSongsFromPlaylist(playlistId).map { it.first().songs }
    }

    fun setCurrentSongIndex(index: Int) {
        _currentSongIndex.value = index
    }

    fun setCurrentQueue(songs: List<Song>) {
        _currentQueue.value = songs
    }

    fun setCurrentSongsList(songs: List<Song>) {
        _currentSongsList.value = songs
    }

    fun skipNext(): Boolean? {
        return _currentSongIndex.value?.let {
             if (it < _currentQueue.value?.size?.minus(1) ?: 0) {
                 _currentSongIndex.value = currentSongIndex.value?.plus(1)
                 true
             } else false
        }
    }

    fun skipPrevious(): Boolean? {
        return _currentSongIndex.value?.let {
            if (it > 0) {
                _currentSongIndex.value = currentSongIndex.value?.minus(1)
                true
            } else false
        }
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

    fun setLooping(isLooping: Boolean) {
        _isLooping.value = isLooping
    }

    fun setShuffle(shuffle: Boolean) {
        _shuffle.value = shuffle
    }

    fun updateDb() {
        viewModelScope.launch {
            songRepo.updateDb()
        }
    }

    fun createPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistRepo.create(playlist)
        }
    }

    fun updatePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistRepo.update(playlist)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            playlistRepo.delete(playlistId)
        }
    }

    fun addSongToPlaylist(playlistSong: PlaylistSongCrossRef) {
        viewModelScope.launch {
            playlistRepo.insertPlaylistSong(playlistSong)
        }
    }

    fun deleteSongFromPlaylist(playlistSong: PlaylistSongCrossRef) {
        viewModelScope.launch {
            playlistRepo.deletePlaylistSong(playlistSong)
        }
    }
}