package ru.netology.nmedia.repository

import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import ru.netology.nmedia.api.*
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostEntity.Companion.fromDto
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.PhotoModel
import java.io.File

class PostRepositoryRetrofitImpl(private val dao: PostDao) : PostRepository {
    override val data = dao.getAll().map(List<PostEntity>::toDto)

    override suspend fun getAll() {
        try {
            val response = PostApi.service.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            body.map {
                it.isSaveOnService = true
                it.display = true
            }
            dao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            try {
                val response = PostApi.service.getNewer(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                body.map {
                    it.isSaveOnService = true
                    it.display = false
                }
                dao.insertShadow(body.toEntity())
                emit(body.size)
            } catch (e: IOException) {
                throw NetworkError
            } catch (e: Exception) {
                throw UnknownError
            }
        }
    }.catch { it.printStackTrace() }

    override suspend fun updatePosts() {
        dao.updatePosts()
    }

    override suspend fun likeById(id: Long) {
        dao.likeById(id)
        try {
            val response = PostApi.service.like(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            dao.likeById(id)
            throw NetworkError
        } catch (e: Exception) {
            dao.likeById(id)
            throw UnknownError
        }
    }

    override suspend fun dislikeById(id: Long) {
        dao.likeById(id)
        try {
            val response = PostApi.service.dislike(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            dao.likeById(id)
            throw NetworkError
        } catch (e: Exception) {
            dao.likeById(id)
            throw UnknownError
        }
    }

    override suspend fun shareById(id: Long) {
        dao.shareById(id)

        data.map { list ->
            list.map {
                if (it.id != id) it else it.copy(
                    sharedByMe = true,
                    shares = it.shares + 1
                )
            }
        }
    }

    override suspend fun removeById(id: Long) {
        dao.removeById(id)
        try {
            PostApi.service.delete(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post, photo: PhotoModel?) {
//        dao.insert(fromDto(post.copy(author = "User", published = "Now", isSaveOnService = false)))
        try {
            val postWithAttachment = if (photo?.file != null) {
                val media = upload(photo.file)
                post.copy(attachment = Attachment(media.id, AttachmentType.IMAGE))
            } else post

            val response = PostApi.service.save(postWithAttachment)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            body.isSaveOnService = true
            body.display = true

//            dao.updatePostId(body.id, post.id)
//            dao.updatePost(fromDto(body))

            dao.insert(fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun upload(file: File): Media {
        return PostApi.service.upload(
            MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
        )
    }
}