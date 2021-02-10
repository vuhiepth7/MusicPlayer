package com.example.musicplayer.data.local

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.musicplayer.data.model.Song

class ContentResolverHelper(context: Context) : SongDataSource {

    private val contentResolver = context.contentResolver

    override fun getAllSongs(): List<Song> {
        val songs = mutableListOf<Song>()
        val projections = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE
        )
        val selection = "${MediaStore.Audio.Media.TITLE} != \"\""
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projections,
            selection,
            null,
            sortOrder
        )
        cursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndex(projections[0]))
                val title = cursor.getString(cursor.getColumnIndex(projections[1]))
                val artist = cursor.getString(cursor.getColumnIndex(projections[2]))
                val albumId = cursor.getLong(cursor.getColumnIndex(projections[3]))
                val duration = cursor.getLong(cursor.getColumnIndex(projections[4]))
                val size = cursor.getLong(cursor.getColumnIndex(projections[5]))

                val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
                val uri = ContentUris.withAppendedId(sArtworkUri, albumId)

                songs.add(Song(id, title, artist, uri.toString(), duration, size))
            }
        }
        return songs
    }

    override fun addAllSongs(songs: List<Song>) {}

    override fun deleteAll() {}
}