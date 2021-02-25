package com.example.musicplayer.data.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicplayer.CoroutineTestRule
import com.example.musicplayer.data.local.IContentResolverHelper
import com.example.musicplayer.data.local.db.SongDao
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
class SongRepositoryTest {

    @get:Rule
    var instantExecutor = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    lateinit var songRepository: ISongRepository
    @Mock lateinit var songDao: SongDao
    @Mock lateinit var contentResolverHelper: IContentResolverHelper

    private val songs = mutableListOf(
        Song(1, "Song 1", "artist 1", "uri 1", 1, false),
        Song(2, "Song 2", "artist 2", "uri 2", 2, true),
        Song(3, "Song 3", "artist 3", "uri 3", 3, false),
        Song(4, "Song 4", "artist 4", "uri 4", 4, true),
        Song(5, "Song 5", "artist 5", "uri 5", 5, true),
        Song(6, "Song 6", "artist 6", "uri 6", 6, false),
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        songRepository = SongRepository(songDao, contentResolverHelper )
    }

    @Test
    fun getAll() {
        `when`(songDao.getAll()).thenReturn(MutableLiveData(songs))
        songRepository.getAll()
        verify(songDao).getAll()
    }

    @Test
    fun insertAll() = runBlockingTest {
        songRepository.insertAll(songs)
        verify(songDao).insertAll(songs)
    }

    @Test
    fun update() = runBlockingTest{
        songRepository.update(songs[0])
        verify(songDao).update(songs[0])
    }

    @Test
    fun delete() = runBlockingTest {
        songRepository.delete(songs[0])
        verify(songDao).delete(songs[0])
    }

    @Test
    fun deleteAll() = runBlockingTest {
        songRepository.deleteAll(songs)
        verify(songDao).deleteAll(songs)
    }

    @Test
    fun updateDb() = runBlockingTest {
        `when`(contentResolverHelper.getAll()).thenReturn(songs)
        songRepository.updateDb()
        verify(contentResolverHelper).getAll()
        verify(songDao).insertAll(songs)
    }
}