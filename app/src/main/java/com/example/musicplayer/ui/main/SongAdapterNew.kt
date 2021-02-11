package com.example.musicplayer.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.databinding.ItemSongBinding
import com.example.musicplayer.ui.SongAdapter.*

class SongAdapterNew(private val songs: List<Song>, private val listener: SongListener) : RecyclerView.Adapter<SongViewHolder>() {

    class SongViewHolder(private val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Song, listener: SongListener) {
            binding.apply {
                binding.song = item
                binding.container.setOnClickListener { listener.onSongClicked(item) }
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongAdapter.SongViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSongBinding.inflate(layoutInflater, parent, false)
        return SongAdapter.SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongAdapter.SongViewHolder, position: Int) {
        holder.bind(songs[position], listener)
    }

    override fun getItemCount() = songs.size
}