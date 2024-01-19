package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar : String,
    val content: String,
    val published: String,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val sharedByMe: Boolean = false,
    val views: Int = 0,
    var attachment: Attachment? = null,
    var isSaveOnService: Boolean = false,
    var display: Boolean = false
)

