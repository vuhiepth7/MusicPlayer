package com.example.musicplayer.data.service

import android.app.PendingIntent
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
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.musicplayer.R
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.receiver.NotificationBroadcastReceiver
import com.example.musicplayer.utils.NotificationUtils


class MediaPlayerService : Service(), MediaPlayer.OnErrorListener,
    AudioManager.OnAudioFocusChangeListener {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager
    private val iBinder = LocalBinder()
    private lateinit var callback: MediaPlayerCallback
    private val mediaBroadcastReceiver by lazy { MediaBroadcastReceiver() }
    private var currentSong: Song? = null

    override fun onBind(intent: Intent?): IBinder {
        if (!requestAudioFocus()) stopSelf()
        initMediaPlayer()
        registerReceiver()
        return iBinder
    }

    fun registerCallback(mediaPlayerCallback: MediaPlayerCallback) {
        callback = mediaPlayerCallback
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter(MEDIA_INTENT_FILTER)
        registerReceiver(mediaBroadcastReceiver, intentFilter)
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

    private fun startNotification(song: Song?) {
        song?.let {
            val notification = NotificationUtils.createNotification(this, it, isPlaying())
            startForeground(1, notification)
        }
    }

    fun setSong(song: Song) {
        startNotification(song)
        currentSong = song
        mediaPlayer.apply {
            reset()
            setDataSource(
                applicationContext,
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.songId)
            )
            prepareAsync()
        }
    }

    fun getSong() = currentSong

    fun playMedia() {
        if (!isPlaying()) mediaPlayer.start()
        startNotification(currentSong)
    }

    fun pauseMedia() {
        if (isPlaying()) mediaPlayer.pause()
        startNotification(currentSong)
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
        unregisterReceiver(mediaBroadcastReceiver)
        removeAudioFocus()
    }

    companion object {
        const val NOTIFICATION_CHANNEL_NAME = "Playback"
        const val NOTIFICATION_CHANNEL_ID = "10"

        const val MEDIA_INTENT_FILTER = "com.example.musicplayer.MEDIA_INTENT_FILTER"
        const val ACTION_PLAY_PAUSE = "com.example.musicplayer.ACTION_PLAY_PAUSE"
        const val ACTION_SKIP_NEXT = "com.example.musicplayer.ACTION_SKIP_NEXT"
        const val ACTION_SKIP_PREVIOUS = "com.example.musicplayer.ACTION_SKIP_PREVIOUS"
        const val ACTION_STOP = "com.example.musicplayer.ACTION_STOP"
    }

    inner class LocalBinder : Binder() {
        fun getService() = this@MediaPlayerService
    }

    inner class MediaBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.getStringExtra("action")) {
                ACTION_PLAY_PAUSE -> callback.onMediaPlayPause()
                ACTION_SKIP_NEXT -> callback.onMediaSkipNext()
                ACTION_SKIP_PREVIOUS -> callback.onMediaSkipPrevious()
                ACTION_STOP -> callback.onMediaStop()
            }
        }
    }

    interface MediaPlayerCallback {
        fun onCompletion()
        fun onMediaPlayPause()
        fun onMediaSkipNext()
        fun onMediaSkipPrevious()
        fun onMediaStop()
    }
}