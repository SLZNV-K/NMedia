package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
}


class PostsAdapter(
    private val onInteractionListener: OnInteractionListener
) : ListAdapter<Post, PostsViewHolder>(PostDiffCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        val view = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostsViewHolder(view, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostsViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        with(binding) {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            like.isChecked = post.likedByMe
            like.text = reformatCount(post.likes)
            share.isClickable = post.sharedByMe
            share.text = reformatCount(post.shares)
            viewingCount.text = reformatCount(post.views)

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }
            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }
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

object PostDiffCallBack : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
}
