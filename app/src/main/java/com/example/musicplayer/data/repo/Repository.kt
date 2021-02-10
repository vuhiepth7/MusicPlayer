package com.example.musicplayer.data.repo

import com.example.musicplayer.data.model.Song
import com.example.musicplayer.utils.Result

interface Repository {
    fun getSongsFromDb() : Result<List<Song>>
    fun getSongsFromContentResolver() : Result<List<Song>>
}