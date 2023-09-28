package ru.netology.nmedia.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel by viewModels<PostViewModel>()
        viewModel.data.observe(this) { post ->
            with(binding) {
                author.text = post.author
                published.text = post.published
                content.text = post.content
                like.setImageResource(if (post.likedByMe) R.drawable.ic_liked_24 else R.drawable.ic_unliked_24)
                likesCount.text = reformatCount(post.likes)
                share.setImageResource(if (post.sharedByMe) R.drawable.ic_shared_24 else R.drawable.ic_unshared_24)
                shareCount.text = reformatCount(post.shares)
                viewingCount.text = reformatCount(post.views)
            }
        }
        binding.like.setOnClickListener {
            viewModel.like()
        }
        binding.share.setOnClickListener {
            viewModel.share()
        }
    }

    fun reformatCount(count: Int): String {
        return when {
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
}