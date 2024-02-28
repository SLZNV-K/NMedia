package ru.netology.nmedia.dto

import java.io.Serializable

data class Post(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val sharedByMe: Boolean = false,
    val views: Int = 0,
    var attachment: Attachment? = null,
    var isSaveOnService: Boolean = false,
    var display: Boolean = false,
    val ownedByMe: Boolean = false
) : Serializable

enum class AttachmentType {
    IMAGE
}

data class Attachment(
    val url: String,
    val type: AttachmentType,
)

