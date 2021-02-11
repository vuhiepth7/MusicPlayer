package com.example.musicplayer.utils

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.musicplayer.R

@BindingAdapter("imageUrl")
fun ImageView.loadImage(url: String?) {
    url?.let {
        Glide.with(this.context)
            .load(it)
            .placeholder(R.drawable.image_placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this)
    }
}

@BindingAdapter("visibleIf")
fun View.visibleIf(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.INVISIBLE
}