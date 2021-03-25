package com.example.musicplayer.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ItemSongBinding
import com.example.musicplayer.data.model.Song

class SongAdapter(private val listener: SongListener) :
    ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSongBinding.inflate(layoutInflater, parent, false)
        val songViewHolder = SongViewHolder(binding)
        binding.apply {
            root.setOnClickListener { listener.onSongClicked(songViewHolder.adapterPosition) }
            root.setOnLongClickListener {
                listener.onLongClicked(thumbnail, songViewHolder.adapterPosition)
                true
            }
            favorite.setOnClickListener {
                listener.setSongFavorite(songViewHolder.adapterPosition, favorite.isChecked)
            }
        }
        return songViewHolder
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SongViewHolder(private val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Song) {
            binding.apply {
                song = item
                favorite.isChecked = item.favorite
                executePendingBindings()
            }
        }
    }

    class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.songId == newItem.songId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }

    interface SongListener {
        fun onSongClicked(position: Int)
        fun setSongFavorite(position: Int, isFavorite: Boolean)
        fun onLongClicked(view: View, position: Int)
    }
}
