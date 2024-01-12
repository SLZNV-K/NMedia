package ru.netology.nmedia.repository

import androidx.lifecycle.*
import okio.IOException
import ru.netology.nmedia.api.*
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostEntity.Companion.fromDto
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError

class PostRepositoryRetrofitImpl(private val dao: PostDao) : PostRepository {
    override val data = dao.getAll().map(List<PostEntity>::toDto)
    override suspend fun getAll() {
        try {
            val posts = PostApiService.service.getAll()
            dao.insert(posts.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long) {
        dao.likeById(id)
        try {
            PostApiService.service.like(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun dislikeById(id: Long) {
        dao.likeById(id)
        try {
            PostApiService.service.dislike(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun shareById(id: Long) {
        dao.shareById(id)
        data.value?.map {
            if (it.id != id) it else it.copy(
                sharedByMe = true,
                shares = it.shares + 1
            )
        }
    }

    override suspend fun removeById(id: Long) {
        dao.removeById(id)
        try {
            PostApiService.service.delete(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        dao.save(fromDto(post))
        try {
            PostApiService.service.save(post)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}