package com.example.musicplayer.ui.player

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.musicplayer.R
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.databinding.ActivityPlayerBinding
import com.example.musicplayer.databinding.ItemSongImageBinding
import java.util.*
import java.util.concurrent.TimeUnit

class PlayerActivity : FragmentActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private val viewModel by lazy { ViewModelProvider(this).get(PlayerViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_player)
        setSongListAndCurrentSong()
        setUpClickListeners()
        initViewPager()
        updateUi()
    }

    private fun setSongListAndCurrentSong() {
        viewModel.setSongList(songsList)
        viewModel.setCurrentSong(selectedSong)
    }

    private fun setUpClickListeners() {
        binding.chevronDown.setOnClickListener { finish() }
    }

    private fun initViewPager() {
        binding.viewPager.adapter = PlayerActivityAdapter(this)
        binding.viewPager.currentItem = songsList.indexOf(selectedSong)
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
                viewModel.setCurrentSong(songsList[position])
            }

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun updateUi() {
        viewModel.currentSong.observe(this) {
            it?.let { song ->
                binding.apply {
                    titleTv.text = song.title
                    artistTv.text = song.artist
                    endTimestamp.text = getTimestamp(song.duration)
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
        var selectedSong: Song? = null

        fun setSongs(songs: List<Song>) {
            songsList = songs
        }

        fun setSelectedSongs(song: Song) {
            selectedSong = song
        }
    }
}