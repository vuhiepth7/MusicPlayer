package com.example.musicplayer.ui.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.musicplayer.R
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.databinding.ActivityPlayerBinding
import com.example.musicplayer.databinding.ItemSongImageBinding
import java.util.*
import java.util.concurrent.TimeUnit

class PlayerActivity : AppCompatActivity(), MediaPlayerService.MediaPlayerCallback {

    private lateinit var playerService: MediaPlayerService
    private var isBound = false
    private lateinit var binding: ActivityPlayerBinding
    private val viewModel by lazy { ViewModelProvider(this).get(PlayerViewModel::class.java) }
    private val localBroadcastManager by lazy { LocalBroadcastManager.getInstance(this@PlayerActivity) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_player)
        viewModel.setCurrentSongIndex(currentSongIndex)
        bindPlayerService()
        setUpClickListeners()
        initViewPager()
        updateUi()
    }

    private fun bindPlayerService() {
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as MediaPlayerService.LocalBinder
                playerService = binder.getService()
                playerService.registerCallback(this@PlayerActivity)
                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                isBound = false
            }
        }
        if (!isBound) {
            val intent = Intent(this, MediaPlayerService::class.java)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun setUpClickListeners() {
        binding.chevronDown.setOnClickListener { finish() }
        binding.playPause.setOnClickListener {
            val intent = if (playerService.isPlaying()) {
                binding.playPause.setImageDrawable(resources.getDrawable(R.drawable.ic_play, theme))
                Intent(MediaPlayerService.INTENT_ACTION_PAUSE)
            } else {
                binding.playPause.setImageDrawable(resources.getDrawable(R.drawable.ic_pause, theme))
                Intent(MediaPlayerService.INTENT_ACTION_PLAY)
            }
            localBroadcastManager.sendBroadcast(intent)
        }
        binding.skipNext.setOnClickListener {
            if (viewModel.skipNext()) {
                localBroadcastManager.sendBroadcast(Intent(MediaPlayerService.INTENT_ACTION_SKIP_NEXT))
            }
        }

        binding.skipPrevious.setOnClickListener {
            if (viewModel.skipPrevious()) {
                localBroadcastManager.sendBroadcast(Intent(MediaPlayerService.INTENT_ACTION_SKIP_PREVIOUS))
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.currentTimestamp.text = getTimestamp(progress * 1000L)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val intent = Intent(MediaPlayerService.INTENT_ACTION_SEEK)
                intent.putExtra("seek_to", seekBar?.progress)
                localBroadcastManager.sendBroadcast(intent)
            }
        })
    }

    private fun initViewPager() {
        binding.viewPager.apply {
            adapter = PlayerActivityAdapter(this@PlayerActivity)
            currentItem = currentSongIndex
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageSelected(position: Int) {
                    if (currentSongIndex == position) return
                    val intent = when {
                        currentSongIndex < position -> {
                            viewModel.skipNext()
                            Intent(MediaPlayerService.INTENT_ACTION_SKIP_NEXT)
                        }
                        else -> {
                            viewModel.skipPrevious()
                            Intent(MediaPlayerService.INTENT_ACTION_SKIP_PREVIOUS)
                        }
                    }
                    localBroadcastManager.sendBroadcast(intent)
                }

                override fun onPageScrollStateChanged(state: Int) {}
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }
            })
        }
    }

    private fun updateUi() {
        viewModel.currentSongIndex.observe(this) {
            it?.let { index ->
                currentSongIndex = index
                val currentSong = songsList[index]
                binding.apply {
                    titleTv.text = currentSong.title
                    artistTv.text = currentSong.artist
                    currentTimestamp.text = getTimestamp(0L)
                    endTimestamp.text = getTimestamp(currentSong.duration)
                    seekBar.progress = 0
                    seekBar.max = TimeUnit.MILLISECONDS.toSeconds(currentSong.duration).toInt()
                    viewPager.currentItem = index
                }
            }
        }
    }

    private fun getTimestamp(duration: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        return "$minutes:$seconds"
    }

    inner class PlayerActivityAdapter(context: Context) : PagerAdapter() {
        private val layoutInflater = LayoutInflater.from(context)

        override fun getCount() = songsList.size

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object` as LinearLayoutCompat
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val binding = ItemSongImageBinding.inflate(layoutInflater, container, false)
            binding.url = songsList[position].thumbnail
            Objects.requireNonNull(container).addView(binding.root)
            return binding.root
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as LinearLayoutCompat)
        }
    }

    companion object {
        var songsList: List<Song> = emptyList()
        var currentSongIndex = 0

        fun setSongs(songs: List<Song>) {
            songsList = songs
        }

        fun setSongIndex(index: Int) {
            currentSongIndex = index
        }
    }

    override fun onCompletion() {
        if (viewModel.skipNext()) {
            localBroadcastManager.sendBroadcast(Intent(MediaPlayerService.INTENT_ACTION_SKIP_NEXT))
        } else localBroadcastManager.sendBroadcast(Intent(MediaPlayerService.INTENT_ACTION_STOP))
    }
}