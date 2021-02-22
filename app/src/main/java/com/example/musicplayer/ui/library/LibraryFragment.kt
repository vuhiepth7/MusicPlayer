package com.example.musicplayer.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.musicplayer.R
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.data.model.PlaylistSongCrossRef
import com.example.musicplayer.databinding.FragmentLibraryBinding
import com.example.musicplayer.ui.main.MainViewModel
import com.example.musicplayer.ui.main.SongAdapter

class LibraryFragment : Fragment() {

    private lateinit var binding: FragmentLibraryBinding
    private lateinit var playlistAdapter: PlaylistAdapter
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        observeData()
        initPlaylistAdapter()
    }

    private fun observeData() {
        viewModel.playlists.observe(viewLifecycleOwner) {
            playlistAdapter.submitList(it)
        }
    }

    private fun initPlaylistAdapter() {
        playlistAdapter = PlaylistAdapter(object : PlaylistAdapter.PlaylistListener {
            override fun onPlaylistClicked(position: Int) {

            }
        })
        binding.playlistRv.adapter = playlistAdapter
    }
}