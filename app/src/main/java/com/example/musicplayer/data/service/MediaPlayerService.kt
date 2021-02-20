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
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.ui.player.PlayerActivity

class MediaPlayerService : Service(), MediaPlayer.OnErrorListener,
    AudioManager.OnAudioFocusChangeListener {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager
    private val iBinder = LocalBinder()
    private lateinit var callback: MediaPlayerCallback
    private var currentSongId: Long? = null

    override fun onBind(intent: Intent?): IBinder {
        if (!requestAudioFocus()) stopSelf()
        initMediaPlayer()
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
            setOnPreparedListener { playMedia() }
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
        }
    }

    fun setSongId(id: Long) {
        currentSongId = id
        mediaPlayer.apply {
            reset()
            setDataSource(
                applicationContext,
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
            )
            prepareAsync()
        }
    }

    fun getSongId() = currentSongId

    fun playMedia() {
        if (!isPlaying()) mediaPlayer.start()
    }

    fun pauseMedia() {
        if (isPlaying()) mediaPlayer.pause()
    }

    fun reset(id: Long) {
        setSongId(id)
    }

    fun seekTo(position: Int) {
        mediaPlayer.seekTo(position)
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
        Log.e("MediaService", "onError $what, $extra")
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
    }

    inner class LocalBinder : Binder() {
        fun getService() = this@MediaPlayerService
    }

    interface MediaPlayerCallback {
        fun onCompletion()
        fun onPrepared()
    }
}