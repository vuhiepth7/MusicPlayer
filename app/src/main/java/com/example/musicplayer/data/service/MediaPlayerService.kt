package com.example.musicplayer.data.service

import android.app.Service
import android.content.*
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.musicplayer.ui.player.PlayerActivity

class MediaPlayerService : Service(), MediaPlayer.OnErrorListener,
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
            addAction(INTENT_ACTION_RESTART)
            addAction(INTENT_ACTION_SET_LOOPING)
        }
        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(mediaBroadcastReceiver, intentFilter)
        return iBinder
    }

    fun registerCallback(mediaPlayerCallback: MediaPlayerCallback) {
        callback = mediaPlayerCallback
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.apply {
            setOnErrorListener(this@MediaPlayerService)
            setOnCompletionListener { callback.onCompletion() }
            setOnPreparedListener {
                callback.onPrepared()
                playMedia()
            }
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setDataSource(
                applicationContext,
                ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    songsList[currentSongIndex].id
                )
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
        if (canSkipNext()) {
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

    private fun canSkipNext(): Boolean {
        return if (currentSongIndex < songsList.size) {
            currentSongIndex++
            true
        } else false
    }

    fun skipPrevious() {
        if (canSkipPrevious()) {
            mediaPlayer.apply {
                reset()
                setDataSource(
                    applicationContext,
                    ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        songsList[currentSongIndex].id
                    )
                )
                prepareAsync()
            }
        }
    }

    private fun canSkipPrevious(): Boolean {
        return if (currentSongIndex > 0) {
            currentSongIndex--
            true
        } else false
    }

    fun seekTo(position: Int) {
        mediaPlayer.seekTo(position)
    }

    fun restart() {
        currentSongIndex = -1
        skipNext()
    }

    fun setLooping(looping: Boolean) {
        mediaPlayer.isLooping = looping
    }

    fun isPlaying() = mediaPlayer.isPlaying

    fun currentPosition() = mediaPlayer.currentPosition

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
        LocalBroadcastManager.getInstance(applicationContext)
            .unregisterReceiver(mediaBroadcastReceiver)
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
                INTENT_ACTION_RESTART -> restart()
                INTENT_ACTION_SET_LOOPING -> {
                    val looping = intent.getBooleanExtra("looping", mediaPlayer.isLooping)
                    setLooping(looping)
                }
            }
        }
    }

    interface MediaPlayerCallback {
        fun onCompletion()
        fun onPrepared()
    }

    companion object {
        const val INTENT_ACTION_PLAY = "com.example.musicplayer.ACTION_PLAY"
        const val INTENT_ACTION_PAUSE = "com.example.musicplayer.ACTION_PAUSE"
        const val INTENT_ACTION_SKIP_NEXT = "com.example.musicplayer.ACTION_SKIP_NEXT"
        const val INTENT_ACTION_SKIP_PREVIOUS = "com.example.musicplayer.ACTION_SKIP_PREVIOUS"
        const val INTENT_ACTION_STOP = "com.example.musicplayer.ACTION_STOP"
        const val INTENT_ACTION_SEEK = "com.example.musicplayer.ACTION_SEEK"
        const val INTENT_ACTION_RESTART = "com.example.musicplayer.ACTION_RESTART"
        const val INTENT_ACTION_SET_LOOPING = "com.example.musicplayer.ACTION_SET_LOOPING"
    }
}