package ru.netology.nmedia.dto

sealed interface FeedItem {
    val id: Long
}

data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: Long,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val sharedByMe: Boolean = false,
    val views: Int = 0,
    var attachment: Attachment? = null,
    var isSaveOnService: Boolean = false,
    var display: Boolean = false,
    val ownedByMe: Boolean = false
) : FeedItem

data class Ad(
    override val id: Long,
    val image: String
) : FeedItem

data class TimeSeparator(
    override val id: Long,
    val text: String
) : FeedItem
