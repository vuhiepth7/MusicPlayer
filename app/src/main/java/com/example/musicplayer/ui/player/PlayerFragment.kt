package com.example.musicplayer.ui.player

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.musicplayer.R
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.databinding.FragmentPlayerBinding
import com.example.musicplayer.databinding.ItemSongImageBinding
import com.example.musicplayer.ui.main.MainViewModel
import java.util.*
import java.util.concurrent.TimeUnit

class PlayerFragment : Fragment() {

    private lateinit var binding: FragmentPlayerBinding
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var songs: List<Song>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        setupClickListeners()
        observeData()
    }

    private fun setupClickListeners() {
        binding.apply {
            chevronDown.setOnClickListener { findNavController().navigateUp() }
            sleepTimer.setOnClickListener {
                val dialog = SleepTimerDialogFragment()
                dialog.show(parentFragmentManager, dialog.tag)
            }
            skipNext.setOnClickListener { viewModel.skipNext() }
            skipPrevious.setOnClickListener { viewModel.skipPrevious() }
            playPause.setOnClickListener { viewModel.togglePlayPause() }
            repeat.setOnCheckedChangeListener { btn, _ -> viewModel.setLooping(btn.isChecked) }
            favorite.setOnCheckedChangeListener { btn, _ ->
                viewModel.update(viewModel.currentSong.value?.copy(favorite = btn.isChecked)!!)
            }
            shuffle.setOnCheckedChangeListener { btn, _ -> viewModel.setShuffle(btn.isChecked) }
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {
                    viewModel.setSeekTo(seekBar.progress)
                }
            })
        }
    }

    private fun observeData() {
        viewModel.apply {
            currentQueue.observe(viewLifecycleOwner) {
                if (!binding.shuffle.isChecked) viewModel.setCurrentSongsList(it)
                this@PlayerFragment.songs = it
                initViewPager()
            }

            currentSong.observe(viewLifecycleOwner) {
                it?.let {
                    binding.apply {
                        song = it
                        endTimestamp.text = getMinutesSeconds(it.duration)
                        seekBar.max = it.duration.toInt()
                        viewPager.currentItem = viewModel.currentSongIndex.value ?: 0
                    }
                }
            }

            isPlaying.observe(viewLifecycleOwner) { isPlaying ->
                if (isPlaying) binding.playPause.setImageDrawable(resources.getDrawable(R.drawable.ic_pause, requireActivity().theme))
                else binding.playPause.setImageDrawable(resources.getDrawable(R.drawable.ic_play, requireActivity().theme))
            }

            currentProgress.observe(viewLifecycleOwner) {
                binding.seekBar.progress = it
                binding.currentTimestamp.text = getMinutesSeconds(it.toLong())
            }

            isLooping.observe(viewLifecycleOwner) { binding.repeat.isChecked = it }

            shuffle.observe(viewLifecycleOwner) {
                binding.shuffle.isChecked = it
                if (it) {
                    val list = this@PlayerFragment.songs.toMutableList()
                    val current = list.removeAt(viewModel.currentSongIndex.value!!)
                    list.shuffle()
                    list.add(0, current)
                    setCurrentQueue(list)
                    setCurrentSongIndex(0)
                } else {
                    setCurrentSongIndex(currentSongsList.value?.indexOf(currentSong.value) ?: 0)
                    setCurrentQueue(currentSongsList.value!!)
                }
            }
        }
    }

    private fun getMinutesSeconds(duration: Long): String {
        return String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(duration),
            TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        )
    }

    private fun initViewPager() {
        binding.viewPager.apply {
            adapter = PlayerActivityAdapter(context)
            currentItem = viewModel.currentSongIndex.value ?: 0
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageSelected(position: Int) {
                    viewModel.setCurrentSongIndex(position)
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

    inner class PlayerActivityAdapter(context: Context) : PagerAdapter() {
        private val layoutInflater = LayoutInflater.from(context)

        override fun getCount() = songs.size

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object` as LinearLayoutCompat
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val binding = ItemSongImageBinding.inflate(layoutInflater, container, false)
            binding.url = songs[position].thumbnailUri
            Objects.requireNonNull(container).addView(binding.root)
            return binding.root
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as LinearLayoutCompat)
        }
    }
}