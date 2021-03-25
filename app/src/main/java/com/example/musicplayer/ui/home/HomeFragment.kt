package com.example.musicplayer.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.musicplayer.R
import com.example.musicplayer.data.model.PlaylistSongCrossRef
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.databinding.FragmentHomeBinding
import com.example.musicplayer.ui.library.PlaylistAdapter
import com.example.musicplayer.ui.main.MainViewModel
import com.example.musicplayer.ui.main.SongAdapter
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var songAdapter: SongAdapter
    private val firebaseAnalytics by lazy { Firebase.analytics }
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

    override fun onResume() {
        super.onResume()
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "HomeFragment")
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    private fun initSongAdapter() {
        songAdapter = SongAdapter(object : SongAdapter.SongListener {
            override fun onSongClicked(position: Int) {
                val bundle = Bundle().apply {
                    putString(FirebaseAnalytics.Param.SCREEN_NAME, "HomeFragment")
                    putString("song_name", songAdapter.currentList[position].title)
                }
                firebaseAnalytics.logEvent("select_song", bundle)
                viewModel.setCurrentQueue(songAdapter.currentList)
                viewModel.setCurrentSongIndex(position)
                findNavController().navigate(R.id.nav_player)
            }

            override fun setSongFavorite(position: Int, isFavorite: Boolean) {
                val bundle = Bundle().apply {
                    putString(FirebaseAnalytics.Param.SCREEN_NAME, "HomeFragment")
                    putString("song_name", songAdapter.currentList[position].title)
                    putBoolean("is_favorite", isFavorite)
                }
                firebaseAnalytics.logEvent("favorite", bundle)
                viewModel.update(songAdapter.currentList[position].copy(favorite = isFavorite))
            }

            override fun onLongClicked(view: View, position: Int) {
                val bundle = Bundle().apply {
                    putString(FirebaseAnalytics.Param.SCREEN_NAME, "HomeFragment")
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
            inflate(R.menu.add_to_playlist_menu)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.add -> {
                        val action = HomeFragmentDirections.actionNavHomeToSelectPlaylistDialogFragment(songAdapter.currentList[position].songId)
                        findNavController().navigate(action)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun observeData() {
        viewModel.songs.observe(viewLifecycleOwner) {
            songAdapter.submitList(it)
        }
    }
}