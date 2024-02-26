package ru.netology.nmedia.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.error.ApiError

class PostPagingSource(
    private val apiService: PostApiService
) : PagingSource<Long, Post>() {
    override fun getRefreshKey(state: PagingState<Long, Post>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> {
        try {
            val result = when (params) {
                is LoadParams.Refresh -> {
                    apiService.getLatest(params.loadSize)
                }

                is LoadParams.Append -> {
                    apiService.getBefore(id = params.key, count = params.loadSize)
                }

                is LoadParams.Prepend -> return LoadResult.Page(
                    data = emptyList(),
                    nextKey = null,
                    prevKey = params.key
                )
            }
            if (!result.isSuccessful) {
                throw ApiError(result.code(), result.message())
            }
            val data = result.body().orEmpty()
            return LoadResult.Page(data, prevKey = params.key, nextKey = data.lastOrNull()?.id)
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}