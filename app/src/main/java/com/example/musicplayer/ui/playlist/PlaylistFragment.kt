package com.example.musicplayer.ui.playlist

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.musicplayer.R
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.data.model.PlaylistSongCrossRef
import com.example.musicplayer.data.model.PlaylistWithSongs
import com.example.musicplayer.databinding.FragmentPlaylistBinding
import com.example.musicplayer.ui.main.MainViewModel
import com.example.musicplayer.ui.main.SongAdapter
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class PlaylistFragment : Fragment() {

    private lateinit var binding: FragmentPlaylistBinding
    private lateinit var songAdapter: SongAdapter
    private val firebaseAnalytics by lazy { Firebase.analytics }
    private val viewModel: MainViewModel by activityViewModels()
    private val args: PlaylistFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        initPlaylistAdapter()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "PlaylistFragment")
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    private fun observeData() {
        if (args.playlistId == 0L) {
            viewModel.songs.observe(viewLifecycleOwner) {
                it?.filter { song -> song.favorite }?.let {
                    songAdapter.submitList(it)
                }
            }
        } else {
            viewModel.getSongsFromPlaylist(args.playlistId).observe(viewLifecycleOwner) {
                songAdapter.submitList(it)
            }
        }
    }

    private fun initPlaylistAdapter() {
        songAdapter = SongAdapter(object : SongAdapter.SongListener {
            override fun onSongClicked(position: Int) {
                val bundle = Bundle().apply {
                    putString(FirebaseAnalytics.Param.SCREEN_NAME, "PlaylistFragment")
                    putString("song_name", songAdapter.currentList[position].title)
                }
                firebaseAnalytics.logEvent("select_song", bundle)
                viewModel.setCurrentQueue(songAdapter.currentList)
                viewModel.setCurrentSongIndex(position)
                findNavController().navigate(R.id.nav_player)
            }

            override fun setSongFavorite(position: Int, isFavorite: Boolean) {
                val bundle = Bundle().apply {
                    putString(FirebaseAnalytics.Param.SCREEN_NAME, "PlaylistFragment")
                    putString("song_name", songAdapter.currentList[position].title)
                    putBoolean("is_favorite", isFavorite)
                }
                firebaseAnalytics.logEvent("favorite", bundle)
                viewModel.update(songAdapter.currentList[position].copy(favorite = isFavorite))
            }

            override fun onLongClicked(view: View, position: Int) {
                val bundle = Bundle().apply {
                    putString(FirebaseAnalytics.Param.SCREEN_NAME, "PlaylistFragment")
                    putString("song_name", songAdapter.currentList[position].title)
                }
                firebaseAnalytics.logEvent("song_long_click", bundle)
                showSongMenu(view, position)
            }
        })
        binding.songRv.adapter = songAdapter
    }

    private fun showSongMenu(view: View, position: Int) {
        PopupMenu(requireContext(), view).apply {
            inflate(R.menu.remove_from_playlist_menu)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.remove -> {
                        val bundle = Bundle().apply {
                            putString("song_name", songAdapter.currentList[position].title)
                        }
                        firebaseAnalytics.logEvent("remove_song", bundle)
                        viewModel.deleteSongFromPlaylist(PlaylistSongCrossRef(args.playlistId, songAdapter.currentList[position].songId))
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun showEditPlaylistDialog() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setText(args.playlistName)
        }
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Playlist name")
            setView(input)
            setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            setPositiveButton("Ok") { _, _ -> updatePlaylist(input.text.toString()) }
            show()
        }
    }

    private fun showConfirmDeletePlaylistDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Are you sure you want to delete?")
            setNegativeButton("Yes") { _, _ -> deletePlaylist() }
            setPositiveButton("No") { dialog, _ -> dialog.cancel() }
            show()
        }
    }

    private fun deletePlaylist() {
        val bundle = Bundle().apply {
            putString("playlist_name", args.playlistName)
        }
        firebaseAnalytics.logEvent("delete_playlist", bundle)
        viewModel.deletePlaylist(args.playlistId)
        findNavController().navigateUp()
    }

    private fun updatePlaylist(playlistName: String) {
        if (playlistName.isNotBlank() && playlistName.isNotEmpty()) {
            val bundle = Bundle().apply {
                putString("playlist_name", playlistName)
            }
            firebaseAnalytics.logEvent("update_playlist", bundle)
            viewModel.updatePlaylist(Playlist(args.playlistId, playlistName))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.playlist_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit -> {
                showEditPlaylistDialog()
                true
            }
            R.id.delete -> {
                showConfirmDeletePlaylistDialog()
                true
            }
            else -> false
        }
    }
}