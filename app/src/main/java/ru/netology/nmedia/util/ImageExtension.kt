package ru.netology.nmedia.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R

fun ImageView.load(url: String, circleCrop: Boolean = false) {
    Glide.with(this)
        .load(url)
        .apply { if (circleCrop) this.circleCrop()}
        .timeout(30_000)
        .error(R.drawable.error_24)
        .into(this)
}
