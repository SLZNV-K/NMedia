package ru.netology.nmedia.util

import androidx.recyclerview.widget.DiffUtil
import ru.netology.nmedia.dto.Post

object PostDiffCallBack : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
}
