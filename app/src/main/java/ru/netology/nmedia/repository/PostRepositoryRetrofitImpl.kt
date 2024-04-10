package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.TimeSeparator
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostEntity.Companion.fromDto
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.PhotoModel
import java.io.File
import javax.inject.Inject
import kotlin.random.Random

class PostRepositoryRetrofitImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: PostApiService,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb
) : PostRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 25),
        pagingSourceFactory = dao::getPagingSource,
        remoteMediator = PostRemoteMediator(
            apiService = apiService,
            postDao = dao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb
        )
    ).flow.map {
        it.map(PostEntity::toDto)
            .insertSeparators { prev: FeedItem?, next: FeedItem? ->
                if (prev is Post && next is Post) {
                    if (timingSeparators(prev.published) != timingSeparators(next.published)) {
                        TimeSeparator(Random.nextLong(), timingSeparators(next.published))
                    } else {
                        null
                    }
                } else if (prev == null && next is Post) {
                    TimeSeparator(Random.nextLong(), timingSeparators(next.published))
                } else {
                    null
                }
            }.insertSeparators { previous, _ ->
                if (previous?.id?.rem(5) == 0L) {
                    Ad(Random.nextLong(), "figma.jpg")
                } else {
                    null
                }
            }

    }


    override suspend fun getNewerCount(): Flow<Long> = flow {
        while (true) {
            delay(10_000L)
            emit(apiService.getNewerCount(postRemoteKeyDao.max() ?: 0L).body()?.count ?: 0L)
        }
    }

    override suspend fun getPostById(id: Long) = dao.getPostById(id).toDto()

    override suspend fun likeById(id: Long) {
        dao.likeById(id)
        try {
            val response = apiService.like(id)
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
            val response = apiService.dislike(id)
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
                if (it is Post) {
                    if (it.id != id) it else it.copy(
                        sharedByMe = true,
                        shares = it.shares + 1
                    )
                } else it
            }
        }
    }

    override suspend fun removeById(id: Long) {
        dao.removeById(id)
        try {
            apiService.delete(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post, photo: PhotoModel?) {
        dao.insert(
            fromDto(
                post.copy(
                    author = "User",
                    published = 0,
                    isSaveOnService = false,
                    display = true
                )
            )
        )
        try {
            val postWithAttachment = if (photo?.file != null) {
                val media = upload(photo.file)
                post.copy(attachment = Attachment(media.id, AttachmentType.IMAGE))
            } else post

            val response = apiService.save(postWithAttachment)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            body.isSaveOnService = true
            body.display = true
            dao.updatePostId(body.id, dao.getLastId())
            dao.updatePost(fromDto(body))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun upload(file: File): Media {
        return apiService.upload(
            MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
        )
    }

    private fun timingSeparators(published: Long): String {
        val millisInDay = 24 * 60 * 60 * 1000

        val diff: Float = System.currentTimeMillis().toFloat() - published * 1000f
        val diffDays: Float = diff / millisInDay

        return if (diffDays < 1) {
            "Сегодня"
        } else if (diffDays < 2) {
            "Вчера"
        } else {
            "На прошлой неделе"
        }
    }
}