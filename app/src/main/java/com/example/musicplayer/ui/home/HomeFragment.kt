package com.example.musicplayer.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentHomeBinding
import com.example.musicplayer.ui.main.MainViewModel
import com.example.musicplayer.ui.main.SongAdapter

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var songAdapter: SongAdapter
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        initSongAdapter()
        observeData()
    }

    private fun initSongAdapter() {
        songAdapter = SongAdapter(object : SongAdapter.SongListener {
            override fun onSongClicked(position: Int) {
                viewModel.setCurrentSongIndex(position)
                findNavController().navigate(R.id.nav_player)
            }

            override fun setSongFavorite(position: Int, isFavorite: Boolean) {
                viewModel.update(songAdapter.currentList[position].copy(favorite = isFavorite))
            }
        })
        binding.songRv.adapter = songAdapter
    }

    private fun observeData() {
        viewModel.songs.observe(viewLifecycleOwner) { songAdapter.submitList(it) }
    }
}