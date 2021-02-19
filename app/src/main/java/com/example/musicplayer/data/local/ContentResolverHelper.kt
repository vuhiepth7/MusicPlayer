package com.example.musicplayer.data.local

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.musicplayer.data.model.Song

class ContentResolverHelper(context: Context) {

    private val contentResolver = context.contentResolver

    fun getAll(): List<Song> {
        val songs = mutableListOf<Song>()
        val projections = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )
        val selection = "${MediaStore.Audio.Media.TITLE} != ?"
        val selectionArgs = arrayOf("\"\"")
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projections,
            selection,
            selectionArgs,
            null
        )
        if (cursor != null) {
            with(cursor) {
                while (moveToNext()) {
                    val id = getLong(getColumnIndex(projections[0]))
                    val title = getString(getColumnIndex(projections[1]))
                    val artist = getString(getColumnIndex(projections[2]))
                    val albumId = getLong(getColumnIndex(projections[3]))
                    val duration = getLong(getColumnIndex(projections[4]))
                    val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
                    val thumbnailUri = ContentUris.withAppendedId(sArtworkUri, albumId)

                    songs.add(Song(id, title, artist, thumbnailUri.toString(), duration))
                }
            }
        }
        return songs
    }
}