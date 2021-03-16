package com.example.musicplayer.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.example.musicplayer.R
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.receiver.NotificationBroadcastReceiver
import com.example.musicplayer.data.service.MediaPlayerService
import com.example.musicplayer.ui.main.MainActivity

object NotificationUtils {

    fun createNotification(context: Context, song: Song, isPlaying: Boolean): Notification? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mediaSessionCompat = MediaSessionCompat(context, "tag")
            val bitmap = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source =
                        ImageDecoder.createSource(
                            context.contentResolver,
                            Uri.parse(song.thumbnailUri)
                        )
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(
                        context.contentResolver,
                        Uri.parse(song.thumbnailUri)
                    )
                }
            } catch (e: Exception) {
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_album_placeholder)
            }
            val mediaMetaData = MediaMetadataCompat.Builder()
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, -1)
                .build()
            mediaSessionCompat.setMetadata(mediaMetaData)

            val playPauseButton = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            val playPausePendingIntent = getPendingIntent<NotificationBroadcastReceiver>(
                context, 0, MediaPlayerService.ACTION_PLAY_PAUSE
            )
            val skipNextPendingIntent = getPendingIntent<NotificationBroadcastReceiver>(
                context, 0, MediaPlayerService.ACTION_SKIP_NEXT
            )
            val skipPreviousPendingIntent = getPendingIntent<NotificationBroadcastReceiver>(
                context, 0, MediaPlayerService.ACTION_SKIP_PREVIOUS
            )
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            return NotificationCompat.Builder(context, MediaPlayerService.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_play)
                .setLargeIcon(bitmap)
                .setContentTitle(song.title)
                .setContentText(song.artist)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_skip_previous, "", skipPreviousPendingIntent)
                .addAction(playPauseButton, "", playPausePendingIntent)
                .addAction(R.drawable.ic_skip_next, "", skipNextPendingIntent)
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.sessionToken)
                        .setShowActionsInCompactView(0, 1, 2)
                )
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        } else return null
    }

    private inline fun <reified T> getPendingIntent(
        context: Context,
        requestCode: Int,
        action: String,
        intentFlag: Int = PendingIntent.FLAG_UPDATE_CURRENT
    ): PendingIntent? {
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, T::class.java).setAction(action),
            intentFlag
        )
    }
}