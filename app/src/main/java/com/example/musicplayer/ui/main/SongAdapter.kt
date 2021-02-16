package com.example.musicplayer.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.databinding.ItemSongBinding
import com.example.musicplayer.data.model.Song

class SongAdapter(private val listener: SongListener) :
    ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSongBinding.inflate(layoutInflater, parent, false)
        val songViewHolder = SongViewHolder(binding)
        binding.root.setOnClickListener { listener.onSongClicked(songViewHolder.adapterPosition) }
        binding.favorite.setOnCheckedChangeListener { button, _ ->
            listener.setSongFavorite(songViewHolder.adapterPosition, button.isChecked)
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
                favorite.isChecked = item.favorite == 1
                executePendingBindings()
            }
        }
    }

    class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }

    interface SongListener {
        fun onSongClicked(position: Int)
        fun setSongFavorite(position: Int, isFavorite: Boolean)
    }
}
