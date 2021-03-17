package com.example.musicplayer.ui.library

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.data.model.PlaylistWithSongs
import com.example.musicplayer.databinding.FragmentLibraryBinding
import com.example.musicplayer.ui.main.MainViewModel


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
        binding.fab.setOnClickListener {
            showCreatePlaylistDialog()
        }
    }

    private fun showCreatePlaylistDialog() {
        val input = EditText(requireContext()).apply { inputType = InputType.TYPE_CLASS_TEXT }
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Playlist name")
            setView(input)
            setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            setPositiveButton("Ok") { _, _ -> createPlaylist(input.text.toString()) }
            show()
        }
    }

    private fun createPlaylist(playlistName: String) {
        if (playlistName.isNotBlank() && playlistName.isNotEmpty()) {
            viewModel.createPlaylist(Playlist(name = playlistName))
        }
    }

    private fun observeData() {
        viewModel.playlists.observe(viewLifecycleOwner) {
            val list = it.toMutableList()
            viewModel.songs.value?.filter { song -> song.favorite }?.let {
                list.add(0, PlaylistWithSongs(Playlist(0, "Favorites"), it))
            }
            playlistAdapter.submitList(list)
        }
    }

    private fun initPlaylistAdapter() {
        playlistAdapter = PlaylistAdapter(object : PlaylistAdapter.PlaylistListener {
            override fun onPlaylistClicked(position: Int) {
                val action = LibraryFragmentDirections.actionNavLibraryToNavPlaylist(
                    playlistAdapter.currentList[position].playlist.playlistId,
                    playlistAdapter.currentList[position].playlist.name
                )
                findNavController().navigate(action)
            }
        })
        binding.playlistRv.adapter = playlistAdapter
    }
}