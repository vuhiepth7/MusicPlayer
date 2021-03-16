package com.example.musicplayer.ui.player

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.databinding.ItemSleepTimerBinding

class SleepTimerAdapter(private val listener: SleepTimerListener) :
    ListAdapter<SleepInterval, SleepTimerAdapter.SleepTimerViewHolder>(SleepTimerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SleepTimerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSleepTimerBinding.inflate(layoutInflater, parent, false)
        val viewHolder = SleepTimerViewHolder(parent.context, binding)
        binding.root.setOnClickListener { listener.onClicked(viewHolder.adapterPosition) }
        return viewHolder
    }

    override fun onBindViewHolder(holder: SleepTimerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SleepTimerViewHolder(private val context: Context, private val binding: ItemSleepTimerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SleepInterval) {
            binding.apply {
                sleepTimer = item
                executePendingBindings()
            }
        }
    }

    class SleepTimerDiffCallback : DiffUtil.ItemCallback<SleepInterval>() {
        override fun areItemsTheSame(oldItem: SleepInterval, newItem: SleepInterval): Boolean {
            return oldItem.timeInMillis == newItem.timeInMillis
        }

        override fun areContentsTheSame(oldItem: SleepInterval, newItem: SleepInterval): Boolean {
            return oldItem == newItem
        }
    }

    interface SleepTimerListener {
        fun onClicked(position: Int)
    }
}
