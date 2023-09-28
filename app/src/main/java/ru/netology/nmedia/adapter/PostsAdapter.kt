package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post

typealias OnLikeListener = (post: Post) -> Unit
typealias OnShareListener = (post: Post) -> Unit


class PostsAdapter(
    private val onLikeListener: OnLikeListener,
    private val onShareListener: OnShareListener
) :
    ListAdapter<Post, PostsViewHolder>(PostDiffCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        val view = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostsViewHolder(view, onLikeListener, onShareListener)
    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostsViewHolder(
    private val binding: CardPostBinding,
    private val onLikeListener: OnLikeListener,
    private val onShareListener: OnShareListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        with(binding) {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            like.setImageResource(if (post.likedByMe) R.drawable.ic_liked_24 else R.drawable.ic_unliked_24)
            likesCount.text = reformatCount(post.likes)
//            likesCount.text = post.likes.toString()
            share.setImageResource(if (post.sharedByMe) R.drawable.ic_shared_24 else R.drawable.ic_unshared_24)
            shareCount.text = reformatCount(post.shares)
//            shareCount.text = post.shares.toString()
            viewingCount.text = reformatCount(post.views)
//            viewingCount.text = post.views.toString()

            like.setOnClickListener {
                onLikeListener(post)
            }
            share.setOnClickListener {
                onShareListener(post)
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
