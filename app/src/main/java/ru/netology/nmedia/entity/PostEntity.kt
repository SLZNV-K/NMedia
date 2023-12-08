package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val published: String,
    val content: String,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val sharedByMe: Boolean = false,
    val views: Int = 0,
    val videoLink: String = ""
) {
    fun toDto(): Post = Post(
        id = id,
        author = author,
        authorAvatar = authorAvatar,
        published = published,
        content = content,
        likes = likes,
        likedByMe = likedByMe,
        shares = shares,
        sharedByMe = sharedByMe,
        views = views,
    )

    companion object {
        fun fromDto(dto: Post): PostEntity = with(dto) {
            PostEntity(
                id = id,
                author = author,
                authorAvatar = authorAvatar,
                published = published,
                content = content,
                likes = likes,
                likedByMe = likedByMe,
                shares = shares,
                sharedByMe = sharedByMe,
                views = views,
            )
        }
    }
}