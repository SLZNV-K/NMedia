package ru.netology.nmedia.dto

enum class AttachmentType {
    IMAGE
}
data class Attachment(
    val url: String,
    val description: String?,
    val type: AttachmentType,
)