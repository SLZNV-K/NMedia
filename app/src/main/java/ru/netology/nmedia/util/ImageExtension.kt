package ru.netology.nmedia.util

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import ru.netology.nmedia.R

fun ImageView.load(url: String, circleCrop: Boolean = false) {

    Glide.with(this)
        .load(url)
        .timeout(10_000)
        .apply { if (circleCrop) this.circleCrop() }
        .apply { if (!circleCrop) this.transition(DrawableTransitionOptions.withCrossFade()) }
        .error(R.drawable.error_24)
        .apply {
            if (!circleCrop)
                this.into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        this@load.setImageDrawable(resource)
                        val layoutParams = this@load.layoutParams
                        val width = resource.intrinsicWidth
                        val height = resource.intrinsicHeight

                        val displayMetrics = context.resources.displayMetrics
                        val screenWidth = displayMetrics.widthPixels
                        layoutParams.width = screenWidth

                        val calculatedHeight =
                            (screenWidth.toFloat() / width.toFloat() * height).toInt()
                        layoutParams.height = calculatedHeight
                        this@load.layoutParams = layoutParams
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        this@load.setImageDrawable(placeholder)
                    }

                }) else this.into(this@load)
        }
}