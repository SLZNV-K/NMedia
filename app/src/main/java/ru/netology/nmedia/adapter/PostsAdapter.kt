package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.BuildConfig.BASE_URL
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.databinding.CardTimeSeparatorBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.TimeSeparator
import ru.netology.nmedia.util.PostDiffCallBack
import ru.netology.nmedia.util.load

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
    fun onPost(post: Post)
    fun onPhoto(post: Post)
}


class PostsAdapter(
    private val onInteractionListener: OnInteractionListener
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallBack) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val view =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostsViewHolder(view, onInteractionListener)
            }

            R.layout.card_ad -> {
                val view = CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(view)
            }

            R.layout.card_time_separator -> {
                val view = CardTimeSeparatorBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                TimeSeparatorHolder(view)
            }

            else -> error("Unknown item type: $viewType")
        }


    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            is TimeSeparator -> R.layout.card_time_separator
            null -> error("Unknown item type")
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Ad -> (holder as AdViewHolder).bind(item)
            is Post -> (holder as PostsViewHolder).bind(item)
            is TimeSeparator -> (holder as TimeSeparatorHolder).bind(item)
            null -> error("Unknown item type")
        }
    }
}

class AdViewHolder(
    private val binding: CardAdBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(ad: Ad) {
        binding.image.load("${BASE_URL}/media/${ad.image}")
    }
}

class TimeSeparatorHolder(
    private val binding: CardTimeSeparatorBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(separator: TimeSeparator) {
        binding.textSeparator.text = separator.text
    }
}

class PostsViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        with(binding) {
            author.text = post.author
            published.text = post.published.toString()
            avatar.load("${BASE_URL}/avatars/${post.authorAvatar}", true)
            newContent.text = post.content
            like.isChecked = post.likedByMe
            like.text = reformatCount(post.likes)
            share.isClickable = post.sharedByMe
            share.text = reformatCount(post.shares)
            viewingCount.text = reformatCount(post.views)
            attachment.visibility = View.GONE

            if (post.isSaveOnService) {
                savedOnServer.setImageResource(R.drawable.done_service)
            } else savedOnServer.setImageResource(R.drawable.done_db_24)

            if (post.attachment != null) {
                attachment.visibility = View.VISIBLE
                attachment.load("${BASE_URL}/media/${post.attachment!!.url}")
            }

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }
            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }
            attachment.setOnClickListener {
                onInteractionListener.onPhoto(post)
            }

            menu.isVisible = post.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
            root.setOnClickListener {
                onInteractionListener.onPost(post)
            }
        }
    }
}

fun reformatCount(count: Int): String {
    return when {
        count == 0 -> "0"
        count % 1_000 == 0 && count < 1_000_000 -> "${count / 1_000}K"
        count % 1_000_000 == 0 -> "${count / 1_000_000}M"
        count in 1_000..9_999 -> if (((count * 10 / 1_000).toFloat() % 10).toInt() == 0) {
            "${count / 1_000}K"
        } else {
            "${(count * 10 / 1_000).toFloat() / 10}K"
        }

        count in 10_000..999_999 -> "${count / 1000}K"
        count in 1_000_000..10_000_000 -> if (((count * 10 / 1_000_000).toFloat() % 10).toInt() == 0) {
            "${count / 1_000_000}M"
        } else {
            "${(count * 10 / 1_000_000).toFloat() / 10}M"
        }

        count > 10_000_000 -> "${count / 1_000_000}M"
        else -> count.toString()
    }
}