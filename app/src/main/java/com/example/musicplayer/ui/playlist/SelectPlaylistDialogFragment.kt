package com.example.musicplayer.ui.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.musicplayer.data.model.PlaylistSongCrossRef
import com.example.musicplayer.databinding.DialogFragmentSelectPlaylistBinding
import com.example.musicplayer.ui.library.PlaylistAdapter
import com.example.musicplayer.ui.main.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SelectPlaylistDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: DialogFragmentSelectPlaylistBinding
    private lateinit var playlistAdapter: PlaylistAdapter
    private val viewModel: MainViewModel by activityViewModels()
    private val args: SelectPlaylistDialogFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogFragmentSelectPlaylistBinding.inflate(inflater, container, false)
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
                viewModel.addSongToPlaylist(PlaylistSongCrossRef(playlistAdapter.currentList[position].playlist.playlistId, args.songId))
                findNavController().navigateUp()
            }
        })
        binding.playlistRv.adapter = playlistAdapter
    }
}