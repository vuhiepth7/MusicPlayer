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

    private val _currentSongIndex = MutableLiveData<Int>()
    val currentSongIndex: LiveData<Int>
        get() = _currentSongIndex

    val currentSong: LiveData<Song> = _currentSongIndex.map { _currentQueue.value?.get(it)!! }

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

    private val _isLooping = MutableLiveData<Boolean>()
    val isLooping: LiveData<Boolean>
        get() = _isLooping

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

    fun skipNext(): Boolean {
        return if (_currentSongIndex.value!! < _currentQueue.value?.size!! - 1) {
            _currentSongIndex.value = currentSongIndex.value?.plus(1)
            _songChangeEvent.value = Event(currentSong.value?.songId ?: 0)
            true
        } else false
    }

    fun skipPrevious(): Boolean {
        return if (_currentSongIndex.value!! > 0) {
            _currentSongIndex.value = currentSongIndex.value?.minus(1)
            _songChangeEvent.value = Event(currentSong.value?.songId ?: 0)
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

    fun setLooping(isLooping: Boolean) {
        _isLooping.value = isLooping
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
            playlistRepo.create(playlist)
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