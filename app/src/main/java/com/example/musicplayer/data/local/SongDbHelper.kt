package com.example.musicplayer.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.musicplayer.data.model.Song
import java.lang.Exception

class SongDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION), SongDataSource {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun addAllSongs(songs: List<Song>) {
        val db = this.writableDatabase
        db?.beginTransaction()
        try {
            val values = ContentValues()
            songs.forEach { song ->
                values.apply {
                    put(Song.COLUMN_NAME_ID, song.id)
                    put(Song.COLUMN_NAME_TITLE, song.title)
                    put(Song.COLUMN_NAME_ARTIST, song.artist)
                    put(Song.COLUMN_NAME_THUMBNAIL, song.thumbnail)
                    put(Song.COLUMN_NAME_DURATION, song.duration)
                    put(Song.COLUMN_NAME_SIZE, song.size)
                }
                db?.insert(Song.TABLE_NAME, null, values)
            }
            db?.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    override fun deleteAll() {
        val db = this.writableDatabase
        db.delete(Song.TABLE_NAME, null, null)
    }

    override fun getAllSongs(): List<Song> {
        val songs = mutableListOf<Song>()
        val db = this.readableDatabase
        val projection = arrayOf(
            Song.COLUMN_NAME_ID,
            Song.COLUMN_NAME_TITLE,
            Song.COLUMN_NAME_ARTIST,
            Song.COLUMN_NAME_THUMBNAIL,
            Song.COLUMN_NAME_DURATION,
            Song.COLUMN_NAME_SIZE
        )
        val sortBy = "${Song.COLUMN_NAME_TITLE} ASC"
        val cursor = db.query(Song.TABLE_NAME, projection, null, null, null, null, sortBy)
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(Song.COLUMN_NAME_ID))
                val title = getString(getColumnIndexOrThrow(Song.COLUMN_NAME_TITLE))
                val artist = getString(getColumnIndexOrThrow(Song.COLUMN_NAME_ARTIST))
                val thumbnail = getString(getColumnIndexOrThrow(Song.COLUMN_NAME_THUMBNAIL))
                val duration = getLong(getColumnIndexOrThrow(Song.COLUMN_NAME_DURATION))
                val size = getLong(getColumnIndexOrThrow(Song.COLUMN_NAME_SIZE))

                songs.add(Song(id, title, artist, thumbnail, duration, size))
            }
        }
        return songs
    }

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "song.db"

        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${Song.TABLE_NAME} (" +
                    "${Song.COLUMN_NAME_ID} INTEGER PRIMARY KEY," +
                    "${Song.COLUMN_NAME_TITLE} TEXT," +
                    "${Song.COLUMN_NAME_ARTIST} TEXT," +
                    "${Song.COLUMN_NAME_THUMBNAIL} STRING," +
                    "${Song.COLUMN_NAME_DURATION} INTEGER," +
                    "${Song.COLUMN_NAME_SIZE} INTEGER)"

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${Song.TABLE_NAME}"
    }
}