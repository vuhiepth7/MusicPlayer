package com.example.musicplayer.data.repo

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicplayer.data.local.SongDataSource
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.utils.Status
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class SongRepositoryTest {

    private val songsList = listOf(Song(1, "title", "artist", "thumbnail", 2, 3))
    private lateinit var songDbHelper: SongDataSource
    private lateinit var contentResolverHelper: SongDataSource
    private lateinit var repository: SongRepository

    @Before
    fun setup() {
        songDbHelper = mock(SongDataSource::class.java)
        contentResolverHelper = mock(SongDataSource::class.java)
        repository = SongRepository(songDbHelper, contentResolverHelper)
    }

    @Test
    fun getSongsFromDb() {
        `when`(songDbHelper.getAllSongs()).thenReturn(songsList)
        var values = repository.getSongsFromDb()
        Assert.assertEquals(Status.SUCCESS, values.status)
        Assert.assertEquals(songsList, values.data)

        `when`(songDbHelper.getAllSongs()).thenThrow(IllegalArgumentException())
        values = repository.getSongsFromDb()
        Assert.assertEquals(Status.ERROR, values.status)
        Assert.assertEquals(null, values.data)
    }

    @Test
    fun getSongsFromContentResolver() {
        `when`(contentResolverHelper.getAllSongs()).thenReturn(songsList)
        var values = repository.getSongsFromContentResolver()
        verify(songDbHelper).deleteAll()
        verify(songDbHelper).addAllSongs(songsList)
        Assert.assertEquals(Status.SUCCESS, values.status)
        Assert.assertEquals(songsList, values.data)

        `when`(contentResolverHelper.getAllSongs()).thenThrow(IllegalArgumentException())
        values = repository.getSongsFromContentResolver()
        Assert.assertEquals(Status.ERROR, values.status)
        Assert.assertEquals(null, values.data)
    }
}
