package com.example.musicplayer.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.repo.Repository
import com.example.musicplayer.ui.main.MainViewModel
import com.example.musicplayer.utils.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class MainViewModelTest {

    @get:Rule
    var instantExecutor = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineTestRule = CoroutineTestRule()


    private val songsList = listOf(Song(1, "title", "artist", "thumbnail", 2, 3))
    @Mock
    private lateinit var repository: Repository
    private lateinit var mainViewModel: MainViewModel
    @Mock
    private lateinit var observer: Observer<Result<List<Song>>>

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        mainViewModel = MainViewModel(repository)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun loadSongsFromDb() {
        runBlockingTest {
            `when`(repository.getSongsFromDb()).thenReturn(Result.success(songsList))
            mainViewModel.songs.observeForever(observer)
            mainViewModel.loadSongsFromDb()

            verify(observer).onChanged(Result.loading(null))
            verify(observer).onChanged(Result.success(songsList))
            verify(repository).getSongsFromDb()
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun loadSongsFromContentResolver() {
        runBlockingTest {
            `when`(repository.getSongsFromContentResolver()).thenReturn(Result.success(songsList))
            mainViewModel.songs.observeForever(observer)
            mainViewModel.updateDb()

            verify(observer).onChanged(Result.loading(null))
            verify(observer).onChanged(Result.success(songsList))
            verify(repository).getSongsFromContentResolver()
        }
    }

    @After
    fun cleanUp() {
        mainViewModel.songs.removeObserver(observer)
    }
}