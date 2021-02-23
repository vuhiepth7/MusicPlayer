package com.example.musicplayer.ui.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentPlaylistBinding
import com.example.musicplayer.ui.main.MainViewModel
import com.example.musicplayer.ui.main.SongAdapter

class PlaylistFragment : Fragment() {

    private lateinit var binding: FragmentPlaylistBinding
    private lateinit var songAdapter: SongAdapter
    private val viewModel: MainViewModel by activityViewModels()
    private val args: PlaylistFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        observeData()
        initPlaylistAdapter()
    }

    private fun observeData() {
        viewModel.getSongsFromPlaylist(args.playlistId).observe(viewLifecycleOwner) {
            songAdapter.submitList(it)
        }
    }

    private fun initPlaylistAdapter() {
        songAdapter = SongAdapter(object : SongAdapter.SongListener {
            override fun onSongClicked(position: Int) {
                viewModel.setCurrentQueue(songAdapter.currentList)
                viewModel.setCurrentSongIndex(position)
                findNavController().navigate(R.id.nav_player)
            }

            override fun setSongFavorite(position: Int, isFavorite: Boolean) {
                viewModel.update(songAdapter.currentList[position].copy(favorite = isFavorite))
            }
        })
        binding.songRv.adapter = songAdapter
    }
}