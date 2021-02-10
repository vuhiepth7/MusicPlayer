package com.example.musicplayer.data.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val thumbnail: String,
    val duration: Long,
    val size: Long
) {
    companion object {
        const val TABLE_NAME = "song"
        const val COLUMN_NAME_ID = "id"
        const val COLUMN_NAME_TITLE = "title"
        const val COLUMN_NAME_ARTIST = "artist"
        const val COLUMN_NAME_THUMBNAIL = "thumbnail"
        const val COLUMN_NAME_DURATION = "duration"
        const val COLUMN_NAME_SIZE = "size"
    }
}