package com.example.musicplayer.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Playlist(
    @PrimaryKey(autoGenerate = true) val playlistId: Long = 0,
    @ColumnInfo val name: String
)
