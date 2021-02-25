package com.example.musicplayer.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.musicplayer.data.service.MediaPlayerService.Companion.MEDIA_INTENT_FILTER

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.sendBroadcast(
            Intent(MEDIA_INTENT_FILTER).putExtra("action", intent?.action)
        )
    }
}