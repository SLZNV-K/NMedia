package ru.netology.nmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val post = Post(
            id = 1,
            author = "Нетология. Университет интернет профессий",
            published = "21 мая в 18:36",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            likes = 1_100,
            likedByMe = false,
            shares = 1_099_999,
            sharedByMe = false,
            views = 164
        )
        with(binding) {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            likesCount.text = reformatCount(post.likes)
            shareCount.text = reformatCount(post.shares)
            viewingCount.text = reformatCount(post.views)

            if (post.likedByMe) {
                like.setImageResource(R.drawable.ic_liked_24)
            }
            if (post.sharedByMe) {
                share.setImageResource(R.drawable.ic_shared_24)
            }

            like.setOnClickListener {
                post.likedByMe = !post.likedByMe
                post.likes += if (post.likedByMe) 1 else -1
                like.setImageResource(if (post.likedByMe) R.drawable.ic_liked_24 else R.drawable.ic_unliked_24)
                likesCount.text = reformatCount(post.likes)
            }
            share.setOnClickListener {
                post.sharedByMe = true
                share.setImageResource(R.drawable.ic_shared_24)
                post.shares += 1
                shareCount.text = reformatCount(post.shares)
            }
        }

    }


    private fun reformatCount(count: Int): String {
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