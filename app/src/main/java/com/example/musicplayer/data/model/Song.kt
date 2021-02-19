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
) {
    companion object {
        const val TABLE_NAME = "song"
        const val COLUMN_NAME_ID = "id"
        const val COLUMN_NAME_TITLE = "title"
        const val COLUMN_NAME_ARTIST = "artist"
        const val COLUMN_NAME_THUMBNAIL = "thumbnail"
        const val COLUMN_NAME_DURATION = "duration"
        const val COLUMN_NAME_SIZE = "size"
        const val COLUMN_NAME_FAVORITE = "favorite"
    }
}