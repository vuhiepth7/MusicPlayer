package com.example.musicplayer.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicplayer.CoroutineTestRule
import com.example.musicplayer.ServiceLocator
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.data.model.PlaylistSongCrossRef
import com.example.musicplayer.data.model.PlaylistWithSongs
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.repo.FakePlaylistRepository
import com.example.musicplayer.data.repo.FakeSongRepository
import com.example.musicplayer.data.repo.IPlaylistRepository
import com.example.musicplayer.data.repo.ISongRepository
import com.example.musicplayer.getOrAwaitValue
import com.example.musicplayer.ui.main.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class ViewModelTest {

    @get:Rule
    var instantExecutor = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    lateinit var songRepository: ISongRepository
    lateinit var playlistRepository: IPlaylistRepository

    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        songRepository = FakeSongRepository()
        playlistRepository = FakePlaylistRepository()
        ServiceLocator.songRepository = songRepository
        ServiceLocator.playlistRepository = playlistRepository

        viewModel = MainViewModel(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun update() = runBlockingTest {
        viewModel.update(Song(1, "Song 1", "artist 1", "uri 1", 1, true))
        val songs = viewModel.songs.getOrAwaitValue()
        Assert.assertEquals(songRepository.getAll().value, songs)
    }

    @Test
    fun getSongsFromPlaylist() {
        val playlistSongs = viewModel.getSongsFromPlaylist(1).getOrAwaitValue()
        Assert.assertEquals(playlistRepository.getSongsFromPlaylist(1).value?.first()?.songs, playlistSongs)
    }

    @Test
    fun setCurrentSongIndex() {
        viewModel.setCurrentSongIndex(1)
        val currentSongIndex = viewModel.currentSongIndex.getOrAwaitValue()
        Assert.assertEquals(1, currentSongIndex)
    }

    @Test
    fun setCurrentQueue() {
        val songs = listOf(
            Song(1, "Song 1", "artist 1", "uri 1", 1, false),
            Song(2, "Song 2", "artist 2", "uri 2", 2, true),
            Song(3, "Song 3", "artist 3", "uri 3", 3, false)
        )
        viewModel.setCurrentQueue(songs)
        val currentQueue = viewModel.currentQueue.getOrAwaitValue()
        Assert.assertEquals(songs, currentQueue)
    }

    @Test
    fun skipNext() {
        val songs = listOf(
            Song(1, "Song 1", "artist 1", "uri 1", 1, false),
            Song(2, "Song 2", "artist 2", "uri 2", 2, true),
            Song(3, "Song 3", "artist 3", "uri 3", 3, false)
        )
        viewModel.setCurrentQueue(songs)
        viewModel.setCurrentSongIndex(0)
        var hasSkipNext = viewModel.skipNext()
        Assert.assertEquals(true, hasSkipNext)

        viewModel.setCurrentSongIndex(2)
        hasSkipNext = viewModel.skipNext()
        Assert.assertEquals(false, hasSkipNext)
    }

    @Test
    fun skipPrevious() {
        val songs = listOf(
            Song(1, "Song 1", "artist 1", "uri 1", 1, false),
            Song(2, "Song 2", "artist 2", "uri 2", 2, true),
            Song(3, "Song 3", "artist 3", "uri 3", 3, false)
        )
        viewModel.setCurrentQueue(songs)
        viewModel.setCurrentSongIndex(0)
        var hasSkipPrevious = viewModel.skipPrevious()
        Assert.assertEquals(false, hasSkipPrevious)

        viewModel.setCurrentSongIndex(2)
        hasSkipPrevious = viewModel.skipPrevious()
        Assert.assertEquals(true, hasSkipPrevious)
    }

    @Test
    fun togglePlayPause() {
        viewModel.togglePlayPause()
        val event = viewModel.togglePlayPauseEvent.getOrAwaitValue()
        Assert.assertEquals(Unit, event.getContentIfNotHandled())
    }

    @Test
    fun setIsPlaying() {
        viewModel.setIsPlaying(true)
        val isPlaying = viewModel.isPlaying.getOrAwaitValue()
        Assert.assertEquals(true, isPlaying)
    }

    @Test
    fun setCurrentProgress() {
        viewModel.setCurrentProgress(10)
        val currentProgress = viewModel.currentProgress.getOrAwaitValue()
        Assert.assertEquals(10, currentProgress)
    }

    @Test
    fun setSeekTo() {
        viewModel.setSeekTo(10)
        val event = viewModel.seekToEvent.getOrAwaitValue()
        Assert.assertEquals(10, event.getContentIfNotHandled())
    }

    @Test
    fun setLooping() {
        viewModel.setLooping(true)
        val isLooping = viewModel.isLooping.getOrAwaitValue()
        Assert.assertEquals(true, isLooping)
    }

    @Test
    fun updateDb() = runBlockingTest {
        viewModel.updateDb()
        val songs = viewModel.songs.getOrAwaitValue()
        Assert.assertEquals(songRepository.getAll().value, songs)
    }

    @Test
    fun createPlaylist() = runBlockingTest {
        val newPlaylist = Playlist(5, "Playlist 5")
        viewModel.createPlaylist(newPlaylist)
        val playlist = playlistRepository.getPlaylists().getOrAwaitValue().first { it.playlistId == newPlaylist.playlistId }
        Assert.assertEquals(newPlaylist, playlist)
    }

    @Test
    fun updatePlaylist() = runBlockingTest {
        val newPlaylist = Playlist(2, "New Playlist")
        viewModel.updatePlaylist(newPlaylist)
        val playlist = playlistRepository.getPlaylists().getOrAwaitValue().first { it.playlistId == newPlaylist.playlistId }
        Assert.assertEquals(newPlaylist, playlist)
    }

    @Test
    fun deletePlaylist() = runBlockingTest {
        viewModel.deletePlaylist(2)
        val contains = playlistRepository.getPlaylists().getOrAwaitValue().find { it.playlistId == 2L }
        Assert.assertEquals(null, contains)
    }

    @Test
    fun addSongToPlaylist() = runBlockingTest {
        val playlistSong = PlaylistSongCrossRef(1, 2)
        viewModel.addSongToPlaylist(playlistSong)
        val playlistWithSongs = playlistRepository.getSongsFromPlaylist(1).getOrAwaitValue().first().songs.find { it.songId == 2L }
        Assert.assertEquals(2L, playlistWithSongs?.songId)
    }

    @Test
    fun deleteSongFromPlaylist() = runBlockingTest {
        val playlistSong = PlaylistSongCrossRef(1, 2)
        viewModel.deleteSongFromPlaylist(playlistSong)
        val playlistWithSongs = playlistRepository.getSongsFromPlaylist(1).getOrAwaitValue().first().songs.find { it.songId == 2L }
        Assert.assertEquals(null, null)
    }
}