package com.example.musicplayer.data.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicplayer.CoroutineTestRule
import com.example.musicplayer.data.local.IContentResolverHelper
import com.example.musicplayer.data.local.db.PlaylistDao
import com.example.musicplayer.data.local.db.SongDao
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.data.model.PlaylistSongCrossRef
import com.example.musicplayer.data.model.PlaylistWithSongs
import com.example.musicplayer.data.model.Song
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class PlaylistRepositoryTest {

    @get:Rule
    var instantExecutor = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    lateinit var playlistRepository: IPlaylistRepository
    @Mock lateinit var playlistDao: PlaylistDao

    private val playlists = mutableListOf(
        Playlist(1, "Playlist 1"),
        Playlist(2, "Playlist 2"),
        Playlist(3, "Playlist 3")
    )
    private val playlistSongCrossRefs = mutableListOf(
        PlaylistSongCrossRef(1, 1),
        PlaylistSongCrossRef(1, 2),
        PlaylistSongCrossRef(1, 5),
        PlaylistSongCrossRef(2, 2),
        PlaylistSongCrossRef(2, 6),
        PlaylistSongCrossRef(3, 3),
        PlaylistSongCrossRef(3, 4)
    )
    private val playlistWithSongs = mutableListOf(
        PlaylistWithSongs(
            playlists[0], mutableListOf(
                Song(1, "Song 1", "artist 1", "uri 1", 1, false),
                Song(2, "Song 2", "artist 2", "uri 2", 2, true),
                Song(5, "Song 5", "artist 5", "uri 5", 5, true)
            )
        ),
        PlaylistWithSongs(
            playlists[1], mutableListOf(
                Song(2, "Song 2", "artist 2", "uri 2", 2, true),
                Song(6, "Song 6", "artist 6", "uri 6", 6, false)
            )
        ),
        PlaylistWithSongs(
            playlists[2], mutableListOf(
                Song(3, "Song 3", "artist 3", "uri 3", 3, false),
                Song(4, "Song 4", "artist 4", "uri 4", 4, true)
            )
        ),
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        playlistRepository = PlaylistRepository(playlistDao)
    }

    @Test
    fun getPlaylists() {
        `when`(playlistDao.getPlaylists()).thenReturn(MutableLiveData(playlists))
        playlistRepository.getPlaylists()
        verify(playlistDao).getPlaylists()
    }

    @Test
    fun getPlaylistsWithSongs() {
        `when`(playlistDao.getPlaylistsWithSongs()).thenReturn(MutableLiveData(playlistWithSongs))
        playlistRepository.getPlaylistsWithSongs()
        verify(playlistDao).getPlaylistsWithSongs()
    }

    @Test
    fun getSongsFromPlaylist() {
        `when`(playlistDao.getSongsFromPlaylist(1)).thenReturn(MutableLiveData(playlistWithSongs))
        playlistRepository.getSongsFromPlaylist(1)
        verify(playlistDao).getSongsFromPlaylist(1)
    }

    @Test
    fun create() = runBlockingTest {
        playlistRepository.create(playlists[0])
        verify(playlistDao).create(playlists[0])
    }

    @Test
    fun update() = runBlockingTest {
        playlistRepository.update(playlists[0])
        verify(playlistDao).update(playlists[0])
    }

    @Test
    fun delete() = runBlockingTest {
        playlistRepository.delete(playlists[0].playlistId)
        verify(playlistDao).delete(playlists[0].playlistId)
        verify(playlistDao).deletePlaylistSong(playlists[0].playlistId)
    }

    @Test
    fun insertPlaylistSong() = runBlockingTest {
        playlistRepository.insertPlaylistSong(playlistSongCrossRefs[0])
        verify(playlistDao).insertPlaylistSong(playlistSongCrossRefs[0])
    }

    @Test
    fun deletePlaylistSong() = runBlockingTest {
        playlistRepository.deletePlaylistSong(playlistSongCrossRefs[0])
        verify(playlistDao).deletePlaylistSong(playlistSongCrossRefs[0])
    }
}