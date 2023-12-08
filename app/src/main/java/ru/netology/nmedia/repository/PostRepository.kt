package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAllAsync(callBack: GetAllCallBack<List<Post>>)
    fun likeById(id: Long, callBack: GetAllCallBack<Post>)
    fun dislikeById(id: Long, callBack: GetAllCallBack<Post>)
    fun shareById(id: Long)
    fun removeById(id: Long, callBack: GetAllCallBack<Unit>)
    fun save(post: Post, callBack: GetAllCallBack<Post>)

    interface GetAllCallBack<T> {
        fun onSuccess(data: T)
        fun onError(e: Exception)
    }

}