package com.example.musicplayer.ui.player

import android.app.Service
import android.content.*
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.musicplayer.data.model.Song
import java.lang.Exception

class MediaPlayerService : Service(), MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    AudioManager.OnAudioFocusChangeListener {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager
    private val songsList = PlayerActivity.songsList
    private var currentSongIndex = PlayerActivity.currentSongIndex
    private val iBinder = LocalBinder()
    private lateinit var callback: MediaPlayerCallback
    private val mediaBroadcastReceiver = MediaBroadcastReceiver()

    override fun onBind(intent: Intent?): IBinder {
        if (!requestAudioFocus()) stopSelf()
        initMediaPlayer()
        val intentFilter = IntentFilter().apply {
            addAction(INTENT_ACTION_PLAY)
            addAction(INTENT_ACTION_PAUSE)
            addAction(INTENT_ACTION_SKIP_NEXT)
            addAction(INTENT_ACTION_SKIP_PREVIOUS)
            addAction(INTENT_ACTION_STOP)
            addAction(INTENT_ACTION_SEEK)
        }
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(mediaBroadcastReceiver, intentFilter)
        return iBinder
    }

    fun registerCallback(mediaPlayerCallback: MediaPlayerCallback) {
        callback = mediaPlayerCallback
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.apply {
            setOnCompletionListener(this@MediaPlayerService)
            setOnErrorListener(this@MediaPlayerService)
            setOnPreparedListener(this@MediaPlayerService)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setDataSource(
                applicationContext,
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songsList[currentSongIndex].id)
            )
            prepareAsync()
        }
    }

    fun playMedia() {
        if (!isPlaying()) mediaPlayer.start()
    }

    fun pauseMedia() {
        if (isPlaying()) mediaPlayer.pause()
    }

    fun skipNext() {
        if (currentSongIndex < songsList.size) {
            currentSongIndex++
            mediaPlayer.apply {
                reset()
                setDataSource(
                    applicationContext,
                    ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songsList[currentSongIndex].id
                    )
                )
                prepareAsync()
            }
        }
    }

    fun skipPrevious() {
        if (currentSongIndex > 0) {
            currentSongIndex--
            mediaPlayer.apply {
                reset()
                setDataSource(
                    applicationContext,
                    ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songsList[currentSongIndex].id)
                )
                prepareAsync()
            }
        }
    }

    fun seekTo(position: Int) {
        if (isPlaying()) mediaPlayer.seekTo(position)
    }

    fun isPlaying() = mediaPlayer.isPlaying

    override fun onPrepared(mediaPlayer: MediaPlayer?) { playMedia() }

    override fun onCompletion(mediaPlayer: MediaPlayer?) { callback.onCompletion() }

    private fun requestAudioFocus(): Boolean {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this)
                .build()
            audioManager.requestAudioFocus(focusRequest)
        } else {
            audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun removeAudioFocus() {
        audioManager.abandonAudioFocus(this)
    }

    override fun onError(mediaPlayer: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.d("MediaService", "onError")
        return false
    }

    override fun onAudioFocusChange(focusState: Int) {
        when (focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (!mediaPlayer.isPlaying) mediaPlayer.start()
                mediaPlayer.setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
                mediaPlayer.release()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (mediaPlayer.isPlaying) mediaPlayer.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.setVolume(0.1f, 0.1f)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
        removeAudioFocus()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(mediaBroadcastReceiver)
    }

    inner class LocalBinder : Binder() {
        fun getService() = this@MediaPlayerService
    }

    inner class MediaBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                INTENT_ACTION_PLAY -> playMedia()
                INTENT_ACTION_PAUSE -> pauseMedia()
                INTENT_ACTION_SKIP_NEXT -> skipNext()
                INTENT_ACTION_SKIP_PREVIOUS -> skipPrevious()
                INTENT_ACTION_STOP -> mediaPlayer.stop()
                INTENT_ACTION_SEEK -> {
                    val position = intent.getIntExtra("seek_to", mediaPlayer.currentPosition)
                    seekTo(position)
                }
            }
        }
    }

    interface MediaPlayerCallback {
        fun onCompletion()
    }

    companion object {
        const val INTENT_ACTION_PLAY = "com.example.musicplayer.ACTION_PLAY"
        const val INTENT_ACTION_PAUSE = "com.example.musicplayer.ACTION_PAUSE"
        const val INTENT_ACTION_SKIP_NEXT = "com.example.musicplayer.ACTION_SKIP_NEXT"
        const val INTENT_ACTION_SKIP_PREVIOUS = "com.example.musicplayer.ACTION_SKIP_PREVIOUS"
        const val INTENT_ACTION_STOP = "com.example.musicplayer.ACTION_STOP"
        const val INTENT_ACTION_SEEK = "com.example.musicplayer.ACTION_SEEK"
    }
}