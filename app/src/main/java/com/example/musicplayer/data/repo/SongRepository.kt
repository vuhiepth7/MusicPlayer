package com.example.musicplayer.data.repo

import com.example.musicplayer.data.local.SongDataSource
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.utils.Result
import java.lang.Exception

class SongRepository(
    private val dbHelper: SongDataSource,
    private val contentResolverHelper: SongDataSource
) : Repository {

    override fun getSongsFromDb(): Result<List<Song>> {
        return try {
            val data = dbHelper.getAllSongs()
            Result.success(data)
        } catch (e: Exception) {
            Result.error(e, null)
        }
    }

    override fun getSongsFromContentResolver(): Result<List<Song>> {
        return try {
            val data = contentResolverHelper.getAllSongs()
            dbHelper.deleteAll()
            dbHelper.addAllSongs(data)
            Result.success(data)
        } catch (e: Exception) {
            Result.error(e, null)
        }
    }

}