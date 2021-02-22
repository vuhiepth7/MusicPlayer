package com.example.musicplayer.ui.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.databinding.ItemPlaylistBinding

class PlaylistAdapter(private val listener: PlaylistListener) :
    ListAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPlaylistBinding.inflate(layoutInflater, parent, false)
        val playlistViewHolder = PlaylistViewHolder(binding)
        binding.root.setOnClickListener { listener.onPlaylistClicked(playlistViewHolder.adapterPosition) }
        return playlistViewHolder
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PlaylistViewHolder(private val binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Playlist) {
            binding.apply {
                playlist = item
                executePendingBindings()
            }
        }
    }

    class PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.playlistId == newItem.playlistId
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }
    }

    interface PlaylistListener {
        fun onPlaylistClicked(position: Int)
    }
}
