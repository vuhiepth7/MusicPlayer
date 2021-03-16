package com.example.musicplayer.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.musicplayer.data.service.MediaPlayerService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.sendBroadcast(
            Intent(MediaPlayerService.MEDIA_INTENT_FILTER).putExtra("action", MediaPlayerService.ACTION_STOP)
        )
    }
}