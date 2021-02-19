package com.example.musicplayer.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.musicplayer.data.model.Song

@Database(entities = [Song::class], version = 1)
abstract class SongDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: SongDatabase? = null
        private const val DATABASE_NAME = "song.db"

        fun getDatabase(context: Context): SongDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext, SongDatabase::class.java, DATABASE_NAME)
                        .build()
                }
            }
            return INSTANCE!!
        }
    }
}