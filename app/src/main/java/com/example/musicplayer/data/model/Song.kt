package com.example.musicplayer.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Song(
    @PrimaryKey val id: Long,
    @ColumnInfo val title: String,
    @ColumnInfo val artist: String,
    @ColumnInfo val thumbnailUri: String,
    @ColumnInfo val duration: Long,
    @ColumnInfo val favorite: Boolean = false
)