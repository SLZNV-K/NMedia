package ru.netology.nmedia.dto

data class PushToken(
    val token: String
)

data class PushContent(
    val recipientId: Long?,
    val content: String
)