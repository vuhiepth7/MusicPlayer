package com.example.musicplayer.data.local.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.musicplayer.data.model.Song

@Dao
interface SongDao {

    @Query("SELECT * FROM song ORDER BY title ASC")
    fun getAll(): LiveData<List<Song>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(songs: List<Song>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(song: Song)

    @Delete
    suspend fun delete(song: Song)

    @Delete
    suspend fun deleteAll(songs: List<Song>)

}